package reallyfastmode.protocol.io;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BitWriterTest {
    @Test
    public void writesMostSignificantBitFirst() {
        BitWriter out = new BitWriter(0);

        out.writeBits(0b101, 3);
        out.writeBits(0b11010, 5);

        assertArrayEquals(new byte[]{(byte) 0xBA}, out.toByteArray());
        assertEquals(8, out.bitPosition());
        assertEquals(1, out.byteSize());
    }

    @Test
    public void writesBigEndianValuesAcrossByteBoundaries() {
        BitWriter out = new BitWriter(1);

        out.writeBit(true);
        out.writeUnsignedShort(0x1234);
        out.writeInt(0x89ABCDEF);

        assertArrayEquals(new byte[]{
            (byte) 0x89, 0x1A, 0x44, (byte) 0xD5, (byte) 0xE6, (byte) 0xF7, (byte) 0x80
        }, out.toByteArray());
        assertEquals(49, out.bitPosition());
    }

    @Test
    public void padsUnusedLowBitsWithZeroAndGrows() {
        BitWriter out = new BitWriter(0);
        for (int i = 0; i < 137; i++) {
            out.writeBit((i & 1) == 0);
        }

        byte[] bytes = out.toByteArray();
        assertEquals(18, bytes.length);
        assertEquals((byte) 0xAA, bytes[0]);
        assertEquals((byte) 0x80, bytes[17]);
    }

    @Test
    public void acceptsZeroBitsAndSignedRawBits() {
        BitWriter out = new BitWriter(0);

        out.writeBits(123, 0);
        out.writeBits(-1, 12);

        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xF0}, out.toByteArray());
    }

    @Test
    public void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new BitWriter(-1));
        BitWriter out = new BitWriter(0);
        assertThrows(IllegalArgumentException.class, () -> out.writeBits(0, 33));
        assertThrows(IllegalArgumentException.class, () -> out.writeByte(256));
        assertThrows(IllegalArgumentException.class, () -> out.writeUnsignedShort(-1));
    }
}
