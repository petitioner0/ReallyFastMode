package reallyfastmode.protocol.write;

import org.junit.Test;
import reallyfastmode.protocol.EventProtocol;
import reallyfastmode.protocol.build.EventSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class EventBlockWriterTest {
    @Test
    public void writesFixedFieldsThenOneApplicabilityBitPerOption() {
        EventSnapshot event = new EventSnapshot(0b110011, 4);
        event.optionsApplicability[0] = true;
        event.optionsApplicability[1] = false;
        event.optionsApplicability[2] = true;
        event.optionsApplicability[3] = false;
        BitWriter out = new BitWriter(0);

        EventBlockWriter.write(out, event);

        assertEquals(EventProtocol.FIXED_BITS + 4, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0xCE, (byte) 0x50}, out.toByteArray());
    }

    @Test
    public void writesNoOptionsAsOnlyTheNineFixedBits() {
        BitWriter out = new BitWriter(0);

        EventBlockWriter.write(out, new EventSnapshot(1, 0));

        assertEquals(EventProtocol.FIXED_BITS, out.bitPosition());
        assertArrayEquals(new byte[]{0x04, 0x00}, out.toByteArray());
    }

    @Test
    public void appendsWithoutAligningTheBlock() {
        EventSnapshot event = new EventSnapshot(1, 1);
        event.optionsApplicability[0] = true;
        BitWriter out = new BitWriter(0);
        out.writeBit(true);

        EventBlockWriter.write(out, event);

        assertEquals(11, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0x82, 0x60}, out.toByteArray());
    }

    @Test
    public void rejectsInvalidValuesBeforeWritingAnything() {
        assertRejected(new EventSnapshot(-1, 0));
        assertRejected(new EventSnapshot(64, 0));
        assertRejected(new EventSnapshot(0, 8));
    }

    @Test
    public void rejectsNullArguments() {
        EventSnapshot event = new EventSnapshot(0, 0);
        assertThrows(NullPointerException.class,
            () -> EventBlockWriter.write(null, event));

        BitWriter out = new BitWriter(0);
        assertThrows(NullPointerException.class,
            () -> EventBlockWriter.write(out, null));
        assertEquals(0, out.bitPosition());
    }

    private static void assertRejected(EventSnapshot event) {
        BitWriter out = new BitWriter(0);
        out.writeBits(0b101, 3);

        assertThrows(IllegalArgumentException.class,
            () -> EventBlockWriter.write(out, event));
        assertEquals(3, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0xA0}, out.toByteArray());
    }
}
