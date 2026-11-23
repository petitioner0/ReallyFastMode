package reallyfastmode.patches.protocol;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.protocol.io.StatePacketSender;
import reallyfastmode.protocol.write.RewardPacketPacker;

import java.util.Collections;

/** Publishes rewards when their screen opens, changes, or processes a claim. */
public final class RewardPacketPatches {
    private static final Logger LOGGER = LogManager.getLogger(RewardPacketPatches.class.getName());

    private static AbstractRoom publishedRoom;
    private static CurrentScreen publishedScreen;
    private static AbstractRoom bossRewardRoom;
    private static AbstractRelic chosenBossRelic;
    private static boolean claimAttempted;

    private RewardPacketPatches() {
    }

    @SpirePatch2(clz = RewardItem.class, method = "claimReward")
    public static class TrackRewardClaimPatch {
        @SpirePrefixPatch
        public static void track(RewardItem __instance) {
            CombatRewardScreen rewards = AbstractDungeon.combatRewardScreen;
            if (AbstractDungeon.screen == CurrentScreen.COMBAT_REWARD
                && rewards != null && rewards.rewards.contains(__instance)) {
                claimAttempted = true;
            }
        }
    }

    @SpirePatch2(clz = BossRelicSelectScreen.class, method = "open")
    public static class TrackBossRewardOpenPatch {
        @SpirePostfixPatch
        public static void track() {
            bossRewardRoom = AbstractDungeon.getCurrRoom();
            chosenBossRelic = null;
            publishedScreen = null;
        }
    }

    @SpirePatch2(clz = AbstractRelic.class, method = "bossObtainLogic")
    public static class TrackBossRelicChoicePatch {
        @SpirePostfixPatch
        public static void track(AbstractRelic __instance) {
            BossRelicSelectScreen rewards = AbstractDungeon.bossRelicScreen;
            if (bossRewardRoom != null && rewards != null && rewards.relics.contains(__instance)) {
                chosenBossRelic = __instance;
            }
        }
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "update")
    public static class CaptureAfterUpdatePatch {
        private static boolean beganInGrid;

        @SpirePrefixPatch
        public static void trackScreen() {
            beganInGrid = AbstractDungeon.screen == CurrentScreen.GRID;
        }

        @SpirePostfixPatch
        public static void capture() {
            AbstractRoom room = AbstractDungeon.currMapNode == null ? null : AbstractDungeon.getCurrRoom();
            CombatRewardScreen rewards = AbstractDungeon.combatRewardScreen;
            CurrentScreen screen = AbstractDungeon.screen;
            boolean screenUp = AbstractDungeon.isScreenUp;
            if (bossRewardRoom != null && (bossRewardRoom != room || (screenUp && screen == CurrentScreen.COMBAT_REWARD))) {
                clearBossReward();
            }
            boolean bossReward = bossRewardRoom != null && (screenUp
                ? screen == CurrentScreen.BOSS_REWARD || (screen == CurrentScreen.GRID && chosenBossRelic != null)
                : screen == CurrentScreen.NONE);
            boolean itemReward = room != null && screenUp && rewards != null && isItemRewardScreen(screen, rewards);
            if (!bossReward && !itemReward) {
                publishedRoom = null;
                publishedScreen = null;
                claimAttempted = false;
                return;
            }
            if (room == publishedRoom && screen == publishedScreen && !claimAttempted) {
                return;
            }
            boolean awaitingSelection = screen == CurrentScreen.CARD_REWARD || screen == CurrentScreen.GRID;
            // Relics can consume the grid result on the next room update.
            if (beganInGrid && !awaitingSelection) {
                return;
            }
            if (AbstractDungeon.isFadingIn || AbstractDungeon.isFadingOut || CardCrawlGame.isPopupOpen
                || PeekButton.isPeeking
                || (screen == CurrentScreen.COMBAT_REWARD
                    && (room.phase == RoomPhase.COMBAT || room.phase == RoomPhase.INCOMPLETE))
                || (screen == CurrentScreen.BOSS_REWARD && !hasUnclaimedBossRelics())
                || (screen == CurrentScreen.NONE && isBossRelicEquipPending())
                || StatePacketEffects.hasPending()) {
                return;
            }

            publishedRoom = room;
            publishedScreen = screen;
            claimAttempted = false;
            if (screen == CurrentScreen.NONE) {
                clearBossReward();
            }
            try {
                byte[] packet;
                if (screen == CurrentScreen.BOSS_REWARD) {
                    packet = RewardPacketPacker.captureBossReward();
                } else if (bossReward) {
                    packet = RewardPacketPacker.capture(Collections.emptyList(), awaitingSelection);
                } else {
                    packet = RewardPacketPacker.capture(rewards.rewards, awaitingSelection);
                }
                StatePacketSender.send(packet);
            } catch (RuntimeException exception) {
                LOGGER.error("Unable to capture the reward packet.", exception);
            }
        }
    }

    private static boolean isItemRewardScreen(CurrentScreen screen, CombatRewardScreen rewards) {
        if (screen == CurrentScreen.CARD_REWARD) {
            CardRewardScreen cards = AbstractDungeon.cardRewardScreen;
            return cards != null && cards.rItem != null && rewards.rewards.contains(cards.rItem);
        }
        return screen == CurrentScreen.COMBAT_REWARD
            || (screen == CurrentScreen.GRID && AbstractDungeon.previousScreen == CurrentScreen.COMBAT_REWARD);
    }

    private static boolean hasUnclaimedBossRelics() {
        BossRelicSelectScreen rewards = AbstractDungeon.bossRelicScreen;
        return chosenBossRelic == null && rewards != null && !rewards.relics.isEmpty();
    }

    private static boolean isBossRelicEquipPending() {
        // Vanilla can close the Boss reward screen before the relic's onEquip runs.
        return chosenBossRelic != null && !chosenBossRelic.isDone
            && AbstractDungeon.player.relics.contains(chosenBossRelic);
    }

    private static void clearBossReward() {
        bossRewardRoom = null;
        chosenBossRelic = null;
    }
}
