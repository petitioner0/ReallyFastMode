package reallyfastmode.protocol.write;

import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaRewardTypeCatalog;
import reallyfastmode.protocol.build.RewardColumns;
import reallyfastmode.protocol.io.BitWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RewardBlockWriterTest {
    @Test
    public void writesTheRequestedSparseRewardLayout() {
        RewardColumns rewards = new RewardColumns(5, 0, 2, 1);
        rewards.typeWireId[0] = VanillaRewardTypeCatalog.CARD.wireId;
        rewards.typeWireId[1] = VanillaRewardTypeCatalog.GOLD.wireId;
        rewards.typeWireId[2] = VanillaRewardTypeCatalog.CARD.wireId;
        rewards.typeWireId[3] = VanillaRewardTypeCatalog.POTION.wireId;
        rewards.typeWireId[4] = VanillaRewardTypeCatalog.POTION.wireId;
        rewards.potionWireId[0] = 5;
        rewards.potionWireId[1] = 44;
        rewards.golds[0] = 200;
        BitWriter out = new BitWriter(0);

        RewardBlockWriter.write(out, rewards);

        assertEquals(38, out.bitPosition());
        assertArrayEquals(new byte[]{
            (byte) 0xA0, (byte) 0x86, (byte) 0xC5, (byte) 0xB3, 0x20
        }, out.toByteArray());
    }

    @Test
    public void writesRelicsPotionsAndBothGoldTypesInColumnOrder() {
        RewardColumns rewards = new RewardColumns(5, 2, 1, 2);
        rewards.typeWireId[0] = VanillaRewardTypeCatalog.RELIC.wireId;
        rewards.typeWireId[1] = VanillaRewardTypeCatalog.POTION.wireId;
        rewards.typeWireId[2] = VanillaRewardTypeCatalog.GOLD.wireId;
        rewards.typeWireId[3] = VanillaRewardTypeCatalog.RELIC.wireId;
        rewards.typeWireId[4] = VanillaRewardTypeCatalog.STOLEN_GOLD.wireId;
        rewards.relicWireId[0] = 17;
        rewards.relicWireId[1] = 190;
        rewards.potionWireId[0] = 44;
        rewards.golds[0] = 3;
        rewards.golds[1] = 255;
        BitWriter out = new BitWriter(0);

        RewardBlockWriter.write(out, rewards);

        byte[] bytes = out.toByteArray();
        int offset = 3 + 5 * 3;
        assertEquals(17, readBits(bytes, offset, 8));
        offset += 8;
        assertEquals(190, readBits(bytes, offset, 8));
        offset += 8;
        assertEquals(44, readBits(bytes, offset, 6));
        offset += 6;
        assertEquals(3, readBits(bytes, offset, 8));
        offset += 8;
        assertEquals(255, readBits(bytes, offset, 8));
    }

    @Test
    public void writesEmptyRewardBlockAsThreeZeroBits() {
        BitWriter out = new BitWriter(0);

        RewardBlockWriter.write(out, new RewardColumns(0, 0, 0, 0));

        assertEquals(3, out.bitPosition());
        assertArrayEquals(new byte[]{0}, out.toByteArray());
    }

    @Test
    public void typeOnlyRewardsDoNotWritePayload() {
        RewardColumns rewards = new RewardColumns(4, 0, 0, 0);
        rewards.typeWireId[0] = VanillaRewardTypeCatalog.CARD.wireId;
        rewards.typeWireId[1] = VanillaRewardTypeCatalog.EMERALD_KEY.wireId;
        rewards.typeWireId[2] = VanillaRewardTypeCatalog.SAPPHIRE_KEY.wireId;
        rewards.typeWireId[3] = VanillaRewardTypeCatalog.UNKNOWN.wireId;
        BitWriter out = new BitWriter(0);

        RewardBlockWriter.write(out, rewards);

        assertEquals(15, out.bitPosition());
    }

    @Test
    public void acceptsMaximumEncodableCountsAndPayloads() {
        RewardColumns rewards = new RewardColumns(7, 1, 1, 1);
        rewards.typeWireId[0] = VanillaRewardTypeCatalog.RELIC.wireId;
        rewards.typeWireId[1] = VanillaRewardTypeCatalog.POTION.wireId;
        rewards.typeWireId[2] = VanillaRewardTypeCatalog.GOLD.wireId;
        rewards.typeWireId[3] = VanillaRewardTypeCatalog.CARD.wireId;
        rewards.typeWireId[4] = VanillaRewardTypeCatalog.EMERALD_KEY.wireId;
        rewards.typeWireId[5] = VanillaRewardTypeCatalog.SAPPHIRE_KEY.wireId;
        rewards.typeWireId[6] = VanillaRewardTypeCatalog.UNKNOWN.wireId;
        rewards.relicWireId[0] = 255;
        rewards.potionWireId[0] = 63;
        rewards.golds[0] = 255;

        RewardBlockWriter.write(new BitWriter(0), rewards);
    }

    @Test
    public void validatesShapeBeforeWritingAnything() {
        RewardColumns missingRelic = new RewardColumns(1, 0, 0, 0);
        missingRelic.typeWireId[0] = VanillaRewardTypeCatalog.RELIC.wireId;
        assertRejected(missingRelic);

        RewardColumns unexpectedPotion = new RewardColumns(1, 0, 1, 0);
        unexpectedPotion.typeWireId[0] = VanillaRewardTypeCatalog.CARD.wireId;
        assertRejected(unexpectedPotion);

        RewardColumns missingGold = new RewardColumns(1, 0, 0, 0);
        missingGold.typeWireId[0] = VanillaRewardTypeCatalog.STOLEN_GOLD.wireId;
        assertRejected(missingGold);
    }

    @Test
    public void rejectsOutOfRangeValuesBeforeWritingAnything() {
        RewardColumns tooMany = new RewardColumns(8, 0, 0, 0);
        assertRejected(tooMany);

        RewardColumns invalidType = new RewardColumns(1, 0, 0, 0);
        invalidType.typeWireId[0] = 8;
        assertRejected(invalidType);

        RewardColumns invalidRelic = new RewardColumns(1, 1, 0, 0);
        invalidRelic.typeWireId[0] = VanillaRewardTypeCatalog.RELIC.wireId;
        invalidRelic.relicWireId[0] = 256;
        assertRejected(invalidRelic);

        RewardColumns invalidPotion = new RewardColumns(1, 0, 1, 0);
        invalidPotion.typeWireId[0] = VanillaRewardTypeCatalog.POTION.wireId;
        invalidPotion.potionWireId[0] = 64;
        assertRejected(invalidPotion);

        RewardColumns invalidGold = new RewardColumns(1, 0, 0, 1);
        invalidGold.typeWireId[0] = VanillaRewardTypeCatalog.GOLD.wireId;
        invalidGold.golds[0] = -1;
        assertRejected(invalidGold);
    }

    @Test
    public void rejectsNullArguments() {
        RewardColumns rewards = new RewardColumns(0, 0, 0, 0);
        assertThrows(NullPointerException.class,
            () -> RewardBlockWriter.write(null, rewards));
        assertThrows(NullPointerException.class,
            () -> RewardBlockWriter.write(new BitWriter(0), null));
    }

    private static void assertRejected(RewardColumns rewards) {
        BitWriter out = new BitWriter(0);
        out.writeBits(0b101, 3);
        assertThrows(IllegalArgumentException.class,
            () -> RewardBlockWriter.write(out, rewards));
        assertEquals(3, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0xA0}, out.toByteArray());
    }

    private static int readBits(byte[] bytes, int bitOffset, int bitCount) {
        int value = 0;
        for (int i = 0; i < bitCount; i++) {
            int absoluteBit = bitOffset + i;
            int bit = (bytes[absoluteBit >>> 3] >>> (7 - (absoluteBit & 7))) & 1;
            value = (value << 1) | bit;
        }
        return value;
    }
}
