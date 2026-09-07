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
    public void bulkWriteMatchesBitAtATimeReferenceAtEveryAlignment() {
        int[] values = {
            0, 1, -1, Integer.MIN_VALUE, Integer.MAX_VALUE,
            0x01234567, 0x89ABCDEF, 0x55555555, 0xAAAAAAAA
        };

        for (int initialBits = 0; initialBits < 8; initialBits++) {
            for (int bitCount = 0; bitCount <= 32; bitCount++) {
                for (int value : values) {
                    byte[] actual = new byte[5];
                    byte[] expected = new byte[5];
                    BitWriter out = BitWriter.wrap(actual);

                    out.writeBits(-1, initialBits);
                    out.writeBits(value, bitCount);
                    writeBitsOneAtATime(expected, initialBits, value, bitCount);

                    assertArrayEquals(expected, actual);
                    assertEquals(initialBits + bitCount, out.bitPosition());
                }
            }
        }
    }

    @Test
    public void writesDirectlyIntoCallerOwnedDestination() {
        byte[] packet = new byte[2];
        BitWriter out = BitWriter.wrap(packet);

        out.writeBits(0xABC, 12);

        assertArrayEquals(new byte[]{(byte) 0xAB, (byte) 0xC0}, packet);
        assertEquals(2, out.byteSize());
    }

    @Test
    public void fixedDestinationRejectsOverflowBeforeChangingArray() {
        byte[] packet = new byte[1];
        BitWriter out = BitWriter.wrap(packet);
        out.writeByte(0xA5);

        assertThrows(IllegalStateException.class, () -> out.writeBit(true));
        assertArrayEquals(new byte[]{(byte) 0xA5}, packet);
        assertEquals(8, out.bitPosition());
    }

    @Test
    public void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new BitWriter(-1));
        assertThrows(NullPointerException.class, () -> BitWriter.wrap(null));
        BitWriter out = new BitWriter(0);
        assertThrows(IllegalArgumentException.class, () -> out.writeBits(0, 33));
        assertThrows(IllegalArgumentException.class, () -> out.writeByte(256));
        assertThrows(IllegalArgumentException.class, () -> out.writeUnsignedShort(-1));
    }

    private static void writeBitsOneAtATime(
        byte[] destination,
        int initialBits,
        int value,
        int bitCount
    ) {
        int position = 0;
        for (; position < initialBits; position++) {
            destination[position >>> 3] |= (byte) (1 << (7 - (position & 7)));
        }
        for (int i = bitCount - 1; i >= 0; i--, position++) {
            if (((value >>> i) & 1) != 0) {
                destination[position >>> 3] |= (byte) (1 << (7 - (position & 7)));
            }
        }
    }
}
