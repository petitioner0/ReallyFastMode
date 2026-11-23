package reallyfastmode.protocol.io;

import java.nio.BufferUnderflowException;
import java.util.Objects;

/** Reads MSB-first bits from a caller-owned byte array. */
public final class BitReader {
    private final byte[] buffer;
    private final int bitLimit;
    private int bitPosition;

    public BitReader(byte[] buffer, int bitPosition) {
        this.buffer = Objects.requireNonNull(buffer, "buffer");
        this.bitLimit = (int) Math.min((long) buffer.length * Byte.SIZE, Integer.MAX_VALUE);
        if (bitPosition < 0 || bitPosition > bitLimit) {
            throw new IllegalArgumentException("bitPosition=" + bitPosition);
        }
        this.bitPosition = bitPosition;
    }

    public int bitPosition() {
        return bitPosition;
    }

    public boolean readBit() {
        return readBits(1) != 0;
    }

    /** Reads 0 to 32 bits. A read past the end leaves the position unchanged. */
    public int readBits(int bitCount) {
        if (bitCount < 0 || bitCount > Integer.SIZE) {
            throw new IllegalArgumentException("bitCount=" + bitCount);
        }
        if (bitCount > bitLimit - bitPosition) {
            throw new BufferUnderflowException();
        }

        int value = 0;
        int remaining = bitCount;
        while (remaining > 0) {
            int available = Byte.SIZE - (bitPosition & (Byte.SIZE - 1));
            int take = Math.min(available, remaining);
            int nextByte = Byte.toUnsignedInt(buffer[bitPosition >>> 3]);
            int chunk = (nextByte >>> (available - take)) & ((1 << take) - 1);
            value = (value << take) | chunk;
            bitPosition += take;
            remaining -= take;
        }
        return value;
    }

    /** Reads a two's-complement value with a width of 1 to 32 bits. */
    public int readSignedBits(int bitCount) {
        if (bitCount == 0) {
            throw new IllegalArgumentException("bitCount=" + bitCount);
        }
        int value = readBits(bitCount);
        int shift = Integer.SIZE - bitCount;
        return (value << shift) >> shift;
    }
}
