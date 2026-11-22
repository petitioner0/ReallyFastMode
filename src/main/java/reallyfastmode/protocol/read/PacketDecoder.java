package reallyfastmode.protocol.read;

import reallyfastmode.protocol.PacketHeaderProtocol;
import reallyfastmode.protocol.ProtocolSlots;

/** Reads a state packet header and dispatches its present blocks in slot order. */
public final class PacketDecoder {
    private static final BlockDecoder[] BLOCK_DECODERS = new BlockDecoder[ProtocolSlots.SIZE];

    static {
        // Each placeholder consumes zero bits until its block decoder is implemented.
        BLOCK_DECODERS[ProtocolSlots.GAME] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.PLAYER] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.COMBAT] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.DECK_CARD] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.HAND_CARD] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.DRAW_PILE_CARD] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.DISCARD_PILE_CARD] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.EXHAUST_PILE_CARD] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.MONSTER] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.POTION] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.SELECTION] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.MAP] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.REWARD] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.EVENT] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.SHOP] = (packet, bitOffset) -> bitOffset;
        BLOCK_DECODERS[ProtocolSlots.REST] = (packet, bitOffset) -> bitOffset;
    }

    private PacketDecoder() {
    }

    /** Returns the final bit offset, or {@code -1} if the header is rejected. */
    public static int decode(byte[] packet, int expectedRunId) {
        Header header = tryReadHeader(packet, expectedRunId);
        if (header == null) {
            return -1;
        }

        int bitOffset = header.bodyOffsetBytes() * Byte.SIZE;
        for (int slot = 0; slot < BLOCK_DECODERS.length; slot++) {
            if (header.hasBlock(slot)) {
                bitOffset = BLOCK_DECODERS[slot].decode(packet, bitOffset);
            }
        }
        return bitOffset;
    }

    /**
     * Reads the header without reading the body. Returns {@code null} if the
     * header is missing or truncated, or if the run ID differs from the expected one.
     *
     * @throws IllegalArgumentException if the expected run ID is outside 0–65535
     */
    public static Header tryReadHeader(byte[] packet, int expectedRunId) {
        if (!PacketHeaderProtocol.isValidRunId(expectedRunId)) {
            throw new IllegalArgumentException("expectedRunId=" + expectedRunId + " outside 0-"
                + PacketHeaderProtocol.MAX_RUN_ID);
        }
        if (packet == null || packet.length < PacketHeaderProtocol.HEADER_BYTES) {
            return null;
        }

        int runId = readUnsignedShort(packet, 0);
        if (runId != expectedRunId) {
            return null;
        }

        int presenceMask = readUnsignedShort(packet, Short.BYTES);
        return new Header(runId, presenceMask);
    }

    private static int readUnsignedShort(byte[] packet, int offset) {
        return (Byte.toUnsignedInt(packet[offset]) << Byte.SIZE)
            | Byte.toUnsignedInt(packet[offset + 1]);
    }

    @FunctionalInterface
    private interface BlockDecoder {
        /** Returns the absolute bit offset immediately after this block. */
        int decode(byte[] packet, int bitOffset);
    }

    public static final class Header {
        public final int runId;
        public final int presenceMask;

        private Header(int runId, int presenceMask) {
            this.runId = runId;
            this.presenceMask = presenceMask;
        }

        public boolean hasBlock(int slot) {
            return (presenceMask & PacketHeaderProtocol.blockBit(slot)) != 0;
        }

        public int bodyOffsetBytes() {
            return PacketHeaderProtocol.HEADER_BYTES;
        }
    }
}
