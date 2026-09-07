package reallyfastmode.protocol.io;

/** Append-only MSB-first bit writer. Multi-byte values are big-endian. */
public final class BitWriter {
    private byte[] buffer;
    private int bitPosition;
    private final boolean growable;

    public BitWriter(int initialCapacityBytes) {
        if (initialCapacityBytes < 0) {
            throw new IllegalArgumentException("initialCapacityBytes=" + initialCapacityBytes);
        }
        this.buffer = new byte[Math.max(initialCapacityBytes, 16)];
        this.growable = true;
    }

    private BitWriter(byte[] destination) {
        this.buffer = destination;
        this.growable = false;
    }

    /**
     * Writes directly into a caller-owned, zero-filled destination array.
     *
     * <p>The returned writer has fixed capacity: it never replaces or copies
     * {@code destination}. Writing past the end fails before that write changes
     * the array. The caller can use {@code destination} directly after writing,
     * avoiding the allocation and full-buffer copy performed by
     * {@link #toByteArray()}.</p>
     */
    public static BitWriter wrap(byte[] destination) {
        if (destination == null) {
            throw new NullPointerException("destination");
        }
        return new BitWriter(destination);
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
        if (bitCount == 0) {
            return;
        }
        ensureBits(bitCount);

        int remaining = bitCount;
        int offset = bitPosition & 7;

        if (offset != 0) {
            int available = 8 - offset;
            int take = Math.min(available, remaining);
            int shift = remaining - take;
            int chunk = (value >>> shift) & ((1 << take) - 1);

            buffer[bitPosition >>> 3] |= (byte) (chunk << (available - take));
            bitPosition += take;
            remaining -= take;

            if (remaining == 0) {
                return;
            }
        }

        int byteIndex = bitPosition >>> 3;
        while (remaining >= 8) {
            int shift = remaining - 8;
            buffer[byteIndex++] = (byte) (value >>> shift);
            bitPosition += 8;
            remaining -= 8;
        }

        if (remaining != 0) {
            int chunk = value & ((1 << remaining) - 1);
            buffer[byteIndex] = (byte) (chunk << (8 - remaining));
            bitPosition += remaining;
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
        if (!growable) {
            throw new IllegalStateException(
                "fixed destination capacity=" + buffer.length
                    + " bytes, required=" + requiredBytes + " bytes"
            );
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
