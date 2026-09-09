package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.rewards.RewardItem;
import org.junit.Test;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.RewardProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaRewardTypeCatalog;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RewardColumnsBuilderTest {
    @Test
    public void buildsSparsePayloadColumnsInEncounterOrder() {
        FakeSource source = new FakeSource(
            VanillaRewardTypeCatalog.CARD.wireId,
            VanillaRewardTypeCatalog.GOLD.wireId,
            VanillaRewardTypeCatalog.CARD.wireId,
            VanillaRewardTypeCatalog.POTION.wireId,
            VanillaRewardTypeCatalog.POTION.wireId
        );
        source.golds[1] = 200;
        source.potionWireId[3] = 5;
        source.potionWireId[4] = 44;

        RewardColumns rewards = RewardColumnsBuilder.buildFromSource(source);

        assertEquals(5, rewards.rewardCount);
        assertArrayEquals(new int[]{0, 1, 0, 3, 3}, rewards.typeWireId);
        assertArrayEquals(new int[0], rewards.relicWireId);
        assertArrayEquals(new int[]{5, 44}, rewards.potionWireId);
        assertArrayEquals(new int[]{200}, rewards.golds);
    }

    @Test
    public void treatsStolenGoldAsGoldAndKeysAndUnknownAsTypeOnly() {
        FakeSource source = new FakeSource(
            VanillaRewardTypeCatalog.RELIC.wireId,
            VanillaRewardTypeCatalog.STOLEN_GOLD.wireId,
            VanillaRewardTypeCatalog.EMERALD_KEY.wireId,
            VanillaRewardTypeCatalog.SAPPHIRE_KEY.wireId,
            VanillaRewardTypeCatalog.UNKNOWN.wireId,
            VanillaRewardTypeCatalog.GOLD.wireId,
            VanillaRewardTypeCatalog.POTION.wireId
        );
        source.relicWireId[0] = 190;
        source.golds[1] = 17;
        source.golds[5] = 255;
        source.potionWireId[6] = 44;

        RewardColumns rewards = RewardColumnsBuilder.buildFromSource(source);

        assertArrayEquals(new int[]{190}, rewards.relicWireId);
        assertArrayEquals(new int[]{44}, rewards.potionWireId);
        assertArrayEquals(new int[]{17, 255}, rewards.golds);
    }

    @Test
    public void acceptsEmptyAndSevenRewardInputs() {
        RewardColumns empty = RewardColumnsBuilder.build(
            Collections.<RewardItem>emptyList()
        );
        assertEquals(0, empty.rewardCount);

        FakeSource seven = new FakeSource(0, 0, 0, 0, 0, 0, 0);
        assertEquals(7, RewardColumnsBuilder.buildFromSource(seven).rewardCount);
    }

    @Test
    public void acceptsMaximumEncodablePayloadValues() {
        FakeSource source = new FakeSource(
            VanillaRewardTypeCatalog.RELIC.wireId,
            VanillaRewardTypeCatalog.POTION.wireId,
            VanillaRewardTypeCatalog.GOLD.wireId
        );
        source.relicWireId[0] = 255;
        source.potionWireId[1] = 63;
        source.golds[2] = 255;

        RewardColumns rewards = RewardColumnsBuilder.buildFromSource(source);

        assertArrayEquals(new int[]{255}, rewards.relicWireId);
        assertArrayEquals(new int[]{63}, rewards.potionWireId);
        assertArrayEquals(new int[]{255}, rewards.golds);
    }

    @Test
    public void rejectsNullsAndAnEighthReward() {
        assertThrows(NullPointerException.class,
            () -> RewardColumnsBuilder.build(null));
        assertThrows(NullPointerException.class,
            () -> RewardColumnsBuilder.build(Arrays.asList((RewardItem) null)));
        assertThrows(IllegalArgumentException.class,
            () -> RewardColumnsBuilder.buildFromSource(
                new FakeSource(0, 0, 0, 0, 0, 0, 0, 0)
            ));
        assertThrows(IllegalArgumentException.class,
            () -> RewardColumnsBuilder.buildFromSource(new SizedSource(-1)));
    }

    @Test
    public void rejectsOutOfRangeWireIdsAndGold() {
        FakeSource typeOverflow = new FakeSource(8);
        assertThrows(IllegalArgumentException.class,
            () -> RewardColumnsBuilder.buildFromSource(typeOverflow));

        FakeSource relicOverflow = new FakeSource(VanillaRewardTypeCatalog.RELIC.wireId);
        relicOverflow.relicWireId[0] = 256;
        assertThrows(IllegalArgumentException.class,
            () -> RewardColumnsBuilder.buildFromSource(relicOverflow));

        FakeSource potionOverflow = new FakeSource(VanillaRewardTypeCatalog.POTION.wireId);
        potionOverflow.potionWireId[0] = 64;
        assertThrows(IllegalArgumentException.class,
            () -> RewardColumnsBuilder.buildFromSource(potionOverflow));

        FakeSource goldOverflow = new FakeSource(VanillaRewardTypeCatalog.GOLD.wireId);
        goldOverflow.golds[0] = 256;
        assertThrows(IllegalArgumentException.class,
            () -> RewardColumnsBuilder.buildFromSource(goldOverflow));

        FakeSource negativeStolenGold =
            new FakeSource(VanillaRewardTypeCatalog.STOLEN_GOLD.wireId);
        negativeStolenGold.golds[0] = -1;
        assertThrows(IllegalArgumentException.class,
            () -> RewardColumnsBuilder.buildFromSource(negativeStolenGold));
    }

    @Test
    public void rewardSlotsMatchTheFiveWireColumns() {
        assertEquals(0, ProtocolSlots.Reward.REWARD_COUNT);
        assertEquals(1, ProtocolSlots.Reward.TYPE_WIRE_ID);
        assertEquals(2, ProtocolSlots.Reward.RELIC);
        assertEquals(3, ProtocolSlots.Reward.POTION);
        assertEquals(4, ProtocolSlots.Reward.GOLDS);
        assertEquals(5, ProtocolSlots.Reward.SIZE);
        assertEquals(7, RewardProtocol.MAX_REWARDS);
    }

    private static final class FakeSource implements RewardColumnsBuilder.Source {
        private final int[] typeWireId;
        private final int[] relicWireId;
        private final int[] potionWireId;
        private final int[] golds;

        private FakeSource(int... typeWireId) {
            this.typeWireId = typeWireId;
            this.relicWireId = new int[typeWireId.length];
            this.potionWireId = new int[typeWireId.length];
            this.golds = new int[typeWireId.length];
        }

        @Override
        public int rewardCount() {
            return typeWireId.length;
        }

        @Override
        public int typeWireId(int rewardIndex) {
            return typeWireId[rewardIndex];
        }

        @Override
        public int relicWireId(int rewardIndex) {
            return relicWireId[rewardIndex];
        }

        @Override
        public int potionWireId(int rewardIndex) {
            return potionWireId[rewardIndex];
        }

        @Override
        public int gold(int rewardIndex) {
            return golds[rewardIndex];
        }
    }

    private static final class SizedSource implements RewardColumnsBuilder.Source {
        private final int size;

        private SizedSource(int size) {
            this.size = size;
        }

        @Override
        public int rewardCount() {
            return size;
        }

        @Override
        public int typeWireId(int rewardIndex) {
            return 0;
        }

        @Override
        public int relicWireId(int rewardIndex) {
            return 0;
        }

        @Override
        public int potionWireId(int rewardIndex) {
            return 0;
        }

        @Override
        public int gold(int rewardIndex) {
            return 0;
        }
    }
}
