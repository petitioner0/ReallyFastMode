package reallyfastmode.protocol.write;

import org.junit.Test;
import reallyfastmode.protocol.PotionProtocol;
import reallyfastmode.protocol.build.PotionSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PotionBlockWriterTest {
    @Test
    public void writesTheSlotCountAndPotionWireIds() {
        PotionSnapshot potions = new PotionSnapshot(3);
        potions.potionWireId[0] = 0;
        potions.potionWireId[1] = PotionProtocol.EMPTY_POTION_WIRE_ID;
        potions.potionWireId[2] = PotionProtocol.UNKNOWN_POTION_WIRE_ID;
        BitWriter out = new BitWriter(0);

        PotionBlockWriter.write(out, potions);

        assertEquals(21, out.bitPosition());
        assertArrayEquals(new byte[]{0x60, 0x57, 0x60}, out.toByteArray());
    }

    @Test
    public void writesAnEmptyInventoryAsOnlyTheThreeBitCount() {
        BitWriter out = new BitWriter(0);

        PotionBlockWriter.write(out, new PotionSnapshot(0));

        assertEquals(PotionProtocol.POTION_SLOTS_COUNT_BITS, out.bitPosition());
        assertArrayEquals(new byte[]{0}, out.toByteArray());
    }

    @Test
    public void acceptsConfiguredNumericBoundaries() {
        PotionSnapshot potions = new PotionSnapshot(PotionProtocol.MAX_POTION_SLOTS);
        potions.potionWireId[0] = 63;

        PotionBlockWriter.write(new BitWriter(0), potions);
    }

    @Test
    public void rejectsInvalidValuesBeforeWritingAnything() {
        assertRejected(new PotionSnapshot(8));

        PotionSnapshot invalidWireId = new PotionSnapshot(1);
        invalidWireId.potionWireId[0] = 64;
        assertRejected(invalidWireId);

        assertThrows(IllegalArgumentException.class, () -> new PotionSnapshot(-1));
    }

    @Test
    public void rejectsNullArguments() {
        PotionSnapshot potions = new PotionSnapshot(0);
        assertThrows(NullPointerException.class, () -> PotionBlockWriter.write(null, potions));

        BitWriter out = new BitWriter(0);
        assertThrows(NullPointerException.class, () -> PotionBlockWriter.write(out, null));
        assertEquals(0, out.bitPosition());
    }

    private static void assertRejected(PotionSnapshot potions) {
        BitWriter out = new BitWriter(0);
        out.writeBits(0b101, 3);

        assertThrows(IllegalArgumentException.class,
            () -> PotionBlockWriter.write(out, potions));
        assertEquals(3, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0xA0}, out.toByteArray());
    }
}
