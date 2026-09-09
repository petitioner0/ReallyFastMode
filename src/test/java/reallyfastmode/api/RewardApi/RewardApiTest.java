package reallyfastmode.api.RewardApi;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.LocalizedStrings;
import com.megacrit.cardcrawl.localization.TutorialStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.rewards.RewardItem;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RewardApiTest {
    @BeforeClass
    public static void initializeRewardLocalization() throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        CardCrawlGame.languagePack =
            (LocalizedStrings) unsafe.allocateInstance(TestLocalizedStrings.class);
    }

    @Test
    public void queuesRewardsInTopToBottomListOrder() {
        List<RewardItem> rewards = rewards(3);

        RewardApi.queueClaim(0, rewards);
        assertTrue(rewards.get(0).isDone);
        assertFalse(rewards.get(1).isDone);
        assertFalse(rewards.get(2).isDone);

        rewards.get(0).isDone = false;
        RewardApi.queueClaim(2, rewards);
        assertFalse(rewards.get(0).isDone);
        assertFalse(rewards.get(1).isDone);
        assertTrue(rewards.get(2).isDone);
    }

    @Test
    public void outOfRangeIndicesThrowWithoutQueuingAReward() {
        List<RewardItem> rewards = rewards(2);

        for (int index : new int[] {-1, 2, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class,
                () -> RewardApi.queueClaim(index, rewards));
        }
        assertThrows(IllegalArgumentException.class,
            () -> RewardApi.queueClaim(0, Collections.<RewardItem>emptyList()));
        assertFalse(rewards.get(0).isDone);
        assertFalse(rewards.get(1).isDone);
        assertThrows(IllegalArgumentException.class, () -> RewardApi.claimReward(-1));
    }

    @Test
    public void pendingClaimCannotBeOverwrittenBeforeVanillaConsumesIt() {
        List<RewardItem> rewards = rewards(2);
        rewards.get(1).isDone = true;

        assertThrows(IllegalStateException.class, () -> RewardApi.queueClaim(0, rewards));
        assertFalse(rewards.get(0).isDone);
        assertTrue(rewards.get(1).isDone);
    }

    @Test
    public void successfulQueueClearsStaleHitboxInputOnEveryReward() {
        List<RewardItem> rewards = rewards(3);
        for (RewardItem reward : rewards) {
            reward.hb.clicked = true;
            reward.hb.clickStarted = true;
        }

        RewardApi.queueClaim(1, rewards);

        for (RewardItem reward : rewards) {
            assertFalse(reward.hb.clicked);
            assertFalse(reward.hb.clickStarted);
        }
        assertTrue(rewards.get(1).isDone);
    }

    @Test
    public void malformedListThrowsBeforeChangingAnyReward() {
        RewardItem reward = reward();
        List<RewardItem> rewards = Arrays.asList(reward, null);

        assertThrows(IllegalStateException.class, () -> RewardApi.queueClaim(0, rewards));
        assertFalse(reward.isDone);
    }

    private static List<RewardItem> rewards(int count) {
        RewardItem[] rewards = new RewardItem[count];
        for (int i = 0; i < count; i++) {
            rewards[i] = reward();
        }
        return Arrays.asList(rewards);
    }

    private static RewardItem reward() {
        // The theft constructor avoids dungeon/player state while producing a
        // normal RewardItem with the same public click latch and hitbox.
        return new RewardItem(1, true);
    }

    private static final class TestLocalizedStrings extends LocalizedStrings {
        private TestLocalizedStrings() {
            throw new AssertionError("Allocated without loading game localization files.");
        }

        @Override
        public UIStrings getUIString(String key) {
            UIStrings strings = new UIStrings();
            strings.TEXT = new String[] {"", "", "", "", "", "", ""};
            return strings;
        }

        @Override
        public TutorialStrings getTutorialString(String key) {
            TutorialStrings strings = new TutorialStrings();
            strings.TEXT = new String[] {""};
            strings.LABEL = new String[] {""};
            return strings;
        }
    }
}
