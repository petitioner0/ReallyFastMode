package reallyfastmode.api.RewardApi;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;

import java.util.List;

/** Semantic commands for the current reward screen. Call on the game render thread. */
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

    private static CombatRewardScreen requireActiveRewardScreen() {
        if (CardCrawlGame.mode != CardCrawlGame.GameMode.GAMEPLAY
            || AbstractDungeon.currMapNode == null
            || AbstractDungeon.getCurrRoom() == null
            || AbstractDungeon.player == null
            || AbstractDungeon.player.isDead
            || AbstractDungeon.player.isDying
            || AbstractDungeon.combatRewardScreen == null
            || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.COMBAT_REWARD
            || !AbstractDungeon.isScreenUp
            || AbstractDungeon.isFadingIn
            || AbstractDungeon.isFadingOut) {
            throw new IllegalStateException("No reward screen is currently accepting a claim.");
        }
        return AbstractDungeon.combatRewardScreen;
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
