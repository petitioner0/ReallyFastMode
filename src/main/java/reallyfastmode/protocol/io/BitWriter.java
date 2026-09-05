package reallyfastmode.protocol.io;

/** Append-only MSB-first bit writer. Multi-byte values are big-endian. */
public final class BitWriter {
    private byte[] buffer;
    private int bitPosition;

    public BitWriter(int initialCapacityBytes) {
        if (initialCapacityBytes < 0) {
            throw new IllegalArgumentException("initialCapacityBytes=" + initialCapacityBytes);
        }
        this.buffer = new byte[Math.max(initialCapacityBytes, 16)];
    }

    public int bitPosition() {
        return bitPosition;
    }

    public int byteSize() {
        return (bitPosition >>> 3) + ((bitPosition & 7) == 0 ? 0 : 1);
    }

    public void writeBit(boolean value) {
        writeBits(value ? 1 : 0, 1);
    }

    /**
     * Writes the low {@code bitCount} bits of {@code value}, most-significant
     * selected bit first. Negative values therefore use two's-complement.
     */
    public void writeBits(int value, int bitCount) {
        if (bitCount < 0 || bitCount > 32) {
            throw new IllegalArgumentException("bitCount=" + bitCount);
        }
        ensureBits(bitCount);

        for (int i = bitCount - 1; i >= 0; i--) {
            int bit = (value >>> i) & 1;
            int byteIndex = bitPosition >>> 3;
            int bitIndex = 7 - (bitPosition & 7);
            if (bit != 0) {
                buffer[byteIndex] |= (byte) (1 << bitIndex);
            }
            bitPosition++;
        }
    }

    public void writeByte(int value) {
        requireUnsigned("byte", value, 8);
        writeBits(value, 8);
    }

    public void writeUnsignedShort(int value) {
        requireUnsigned("unsigned short", value, 16);
        writeBits(value, 16);
    }

    public void writeInt(int value) {
        writeBits(value, 32);
    }

    public byte[] toByteArray() {
        int size = byteSize();
        byte[] result = new byte[size];
        System.arraycopy(buffer, 0, result, 0, size);
        return result;
    }

    private void ensureBits(int additionalBits) {
        if (additionalBits > Integer.MAX_VALUE - bitPosition) {
            throw new IllegalStateException("bit position overflow");
        }
        int requiredBits = bitPosition + additionalBits;
        int requiredBytes = (requiredBits >>> 3) + ((requiredBits & 7) == 0 ? 0 : 1);
        if (requiredBytes <= buffer.length) {
            return;
        }

        int doubled = buffer.length <= Integer.MAX_VALUE / 2
            ? buffer.length << 1
            : Integer.MAX_VALUE;
        int newCapacity = Math.max(requiredBytes, doubled);
        byte[] next = new byte[newCapacity];
        System.arraycopy(buffer, 0, next, 0, buffer.length);
        buffer = next;
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }
}
