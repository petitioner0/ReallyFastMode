package reallyfastmode.protocol.write;

import org.junit.Test;
import reallyfastmode.protocol.CombatProtocol;
import reallyfastmode.protocol.build.CombatSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CombatBlockWriterTest {
    @Test
    public void writesOneUnsignedByte() {
        BitWriter out = new BitWriter(0);

        CombatBlockWriter.write(out, new CombatSnapshot(0xA5));

        assertEquals(CombatProtocol.COMBAT_BLOCK_BITS, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0xA5}, out.toByteArray());
    }

    @Test
    public void appendsWithoutAligningTheBlock() {
        BitWriter out = new BitWriter(0);
        out.writeBit(true);

        CombatBlockWriter.write(out, new CombatSnapshot(1));

        assertEquals(9, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0x80, (byte) 0x80}, out.toByteArray());
    }

    @Test
    public void rejectsInvalidValuesBeforeWritingAnything() {
        assertRejected(-1);
        assertRejected(256);
    }

    @Test
    public void rejectsNullArguments() {
        CombatSnapshot combat = new CombatSnapshot(0);
        assertThrows(NullPointerException.class, () -> CombatBlockWriter.write(null, combat));

        BitWriter out = new BitWriter(0);
        assertThrows(NullPointerException.class, () -> CombatBlockWriter.write(out, null));
        assertEquals(0, out.bitPosition());
    }

    private static void assertRejected(int turn) {
        BitWriter out = new BitWriter(0);
        out.writeBits(0b101, 3);

        assertThrows(IllegalArgumentException.class,
            () -> CombatBlockWriter.write(out, new CombatSnapshot(turn)));
        assertEquals(3, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0xA0}, out.toByteArray());
    }
}
