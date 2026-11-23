package reallyfastmode.api.RewardApi;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoomBoss;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import reallyfastmode.patches.access.PrivateFieldAccess;

import java.util.List;

/** Semantic commands for treasure chests and reward screens. Call on the game render thread. */
public final class RewardApi {
    private RewardApi() {
    }

    /**
     * Attempts to claim one reward, numbered from top to bottom starting at zero.
     * The next CombatRewardScreen update calls RewardItem.claimReward() and keeps
     * all vanilla success, failure, removal and follow-up-screen behavior.
     *
     * @throws IllegalArgumentException if rewardIndex is outside the current list
     * @throws IllegalStateException if the reward screen cannot accept a claim
     */
    public static void claimReward(int rewardIndex) {
        if (rewardIndex < 0) {
            throw new IllegalArgumentException("Reward index must be nonnegative.");
        }
        CombatRewardScreen screen = requireActiveRewardScreen();
        queueClaim(rewardIndex, screen.rewards);
    }

    /**
     * Opens the chest in the current treasure room and displays its vanilla rewards.
     *
     * @throws IllegalStateException if the current chest cannot be opened
     */
    public static void openChest() {
        if (!rewardInteractionReady()
            || CardCrawlGame.isPopupOpen
            || AbstractDungeon.isDungeonBeaten
            || AbstractDungeon.isScreenUp
            || AbstractDungeon.screen != CurrentScreen.NONE) {
            throw new IllegalStateException("The current screen is not accepting a chest open.");
        }

        AbstractRoom room = AbstractDungeon.getCurrRoom();
        AbstractChest chest;
        if (room instanceof TreasureRoomBoss && !((TreasureRoomBoss) room).choseRelic) {
            chest = ((TreasureRoomBoss) room).chest;
        } else if (room instanceof TreasureRoom) {
            chest = ((TreasureRoom) room).chest;
        } else {
            throw new IllegalStateException("No unopened treasure room chest is available.");
        }

        if (chest == null || chest.isOpen || !PrivateFieldAccess.chestKeyRequirement(chest)) {
            throw new IllegalStateException("The current chest cannot be opened.");
        }
        InputHelper.justClickedLeft = false;
        CInputActionSet.select.unpress();
        // AbstractChest.update passes false; BossChest.open handles Boss callbacks itself.
        chest.open(false);
    }

    /**
     * Takes the Boss relic at the zero-based index in the current Boss reward list.
     * Vanilla finalizes the choice and closes the screen on its next update.
     *
     * @throws IllegalArgumentException if relicIndex is outside the current list
     * @throws IllegalStateException if the Boss reward screen cannot accept a choice
     */
    public static void claimBossRelic(int relicIndex) {
        if (relicIndex < 0) {
            throw new IllegalArgumentException("Boss relic index must be nonnegative.");
        }
        BossRelicSelectScreen screen = requireBossRelicScreen();
        List<AbstractRelic> relics = screen.relics;
        if (relicIndex >= relics.size()) {
            throw new IllegalArgumentException("Boss relic index is out of range: " + relicIndex);
        }
        for (AbstractRelic relic : relics) {
            if (relic.isObtained) {
                throw new IllegalStateException("A Boss relic choice is already pending.");
            }
        }

        // Consume any pending mouse, controller or touch choice before committing.
        for (AbstractRelic relic : relics) {
            relic.hb.clicked = false;
            relic.hb.clickStarted = false;
        }
        InputHelper.justClickedLeft = false;
        CInputActionSet.select.unpress();
        screen.touchRelic = null;
        screen.confirmButton.hb.clicked = false;
        screen.confirmButton.hb.clickStarted = false;
        screen.confirmButton.hide();

        relics.get(relicIndex).bossObtainLogic();
    }

    private static CombatRewardScreen requireActiveRewardScreen() {
        if (!rewardInteractionReady()
            || AbstractDungeon.combatRewardScreen == null
            || AbstractDungeon.screen != CurrentScreen.COMBAT_REWARD
            || !AbstractDungeon.isScreenUp) {
            throw new IllegalStateException("No reward screen is currently accepting a claim.");
        }
        return AbstractDungeon.combatRewardScreen;
    }

    private static BossRelicSelectScreen requireBossRelicScreen() {
        BossRelicSelectScreen screen = AbstractDungeon.bossRelicScreen;
        if (!rewardInteractionReady()
            || CardCrawlGame.isPopupOpen
            || AbstractDungeon.isDungeonBeaten
            || AbstractDungeon.screen != CurrentScreen.BOSS_REWARD
            || !AbstractDungeon.isScreenUp
            || screen == null
            || !screen.blights.isEmpty()
            || !(AbstractDungeon.getCurrRoom() instanceof TreasureRoomBoss)) {
            throw new IllegalStateException("No Boss reward screen is currently accepting a relic choice.");
        }
        TreasureRoomBoss room = (TreasureRoomBoss) AbstractDungeon.getCurrRoom();
        if (room.choseRelic || room.chest == null || !room.chest.isOpen) {
            throw new IllegalStateException("The current Boss chest is not accepting a relic choice.");
        }
        return screen;
    }

    private static boolean rewardInteractionReady() {
        return CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY
            && AbstractDungeon.currMapNode != null
            && AbstractDungeon.getCurrRoom() != null
            && AbstractDungeon.player != null
            && !AbstractDungeon.player.isDead
            && !AbstractDungeon.player.isDying
            && !AbstractDungeon.isFadingIn
            && !AbstractDungeon.isFadingOut;
    }

    static void queueClaim(int rewardIndex, List<RewardItem> rewards) {
        if (rewardIndex < 0 || rewardIndex >= rewards.size()) {
            throw new IllegalArgumentException("Reward index is out of range: " + rewardIndex);
        }
        for (RewardItem reward : rewards) {
            if (reward == null) {
                throw new IllegalStateException("The current reward list contains a null entry.");
            }
            if (reward.isDone) {
                throw new IllegalStateException("A reward claim is already pending.");
            }
        }

        // CombatRewardScreen.positionRewards places list index 0 at the top.
        RewardItem selected = rewards.get(rewardIndex);

        // Consume pending mouse/controller input so this update attempts only the
        // requested reward. RewardItem.update uses isDone as its click result.
        for (RewardItem reward : rewards) {
            reward.hb.clicked = false;
            reward.hb.clickStarted = false;
        }
        selected.isDone = true;
    }
}
