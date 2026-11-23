package reallyfastmode.patches.protocol;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.protocol.io.StatePacketSender;
import reallyfastmode.protocol.write.RewardPacketPacker;

/** Publishes post-combat rewards when their screen opens or a claim is processed. */
public final class RewardPacketPatches {
    private static final Logger LOGGER = LogManager.getLogger(RewardPacketPatches.class.getName());

    private static AbstractRoom publishedRoom;
    private static CurrentScreen publishedScreen;
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

    @SpirePatch2(clz = AbstractDungeon.class, method = "update")
    public static class CaptureAfterUpdatePatch {
        @SpirePostfixPatch
        public static void capture() {
            AbstractRoom room = AbstractDungeon.currMapNode == null ? null : AbstractDungeon.getCurrRoom();
            CombatRewardScreen rewards = AbstractDungeon.combatRewardScreen;
            CurrentScreen screen = AbstractDungeon.screen;
            if (room == null || !room.isBattleOver || !AbstractDungeon.isScreenUp || rewards == null
                || !isPostCombatRewardScreen(screen, rewards)) {
                publishedRoom = null;
                publishedScreen = null;
                claimAttempted = false;
                return;
            }
            if (room == publishedRoom && screen == publishedScreen && !claimAttempted) {
                return;
            }
            boolean awaitingSelection = screen != CurrentScreen.COMBAT_REWARD;
            if (AbstractDungeon.isFadingIn || AbstractDungeon.isFadingOut || CardCrawlGame.isPopupOpen
                || PeekButton.isPeeking || (!awaitingSelection && room.phase != AbstractRoom.RoomPhase.COMPLETE)
                || StatePacketEffects.hasPending()) {
                return;
            }

            publishedRoom = room;
            publishedScreen = screen;
            claimAttempted = false;
            try {
                StatePacketSender.send(RewardPacketPacker.capture(rewards.rewards, awaitingSelection));
            } catch (RuntimeException exception) {
                LOGGER.error("Unable to capture the post-combat reward packet.", exception);
            }
        }
    }

    private static boolean isPostCombatRewardScreen(CurrentScreen screen, CombatRewardScreen rewards) {
        if (screen == CurrentScreen.CARD_REWARD) {
            CardRewardScreen cards = AbstractDungeon.cardRewardScreen;
            return cards != null && cards.rItem != null && rewards.rewards.contains(cards.rItem);
        }
        return screen == CurrentScreen.COMBAT_REWARD
            || (screen == CurrentScreen.GRID && AbstractDungeon.previousScreen == CurrentScreen.COMBAT_REWARD);
    }
}
