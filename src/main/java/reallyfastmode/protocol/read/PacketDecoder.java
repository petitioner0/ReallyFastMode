package reallyfastmode.protocol.read;

import reallyfastmode.protocol.PacketHeaderProtocol;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.io.BitReader;

import java.nio.BufferUnderflowException;

/** Reads a state packet header and dispatches its present blocks in slot order. */
public final class PacketDecoder {
    private static final BlockDecoder[] BLOCK_DECODERS = new BlockDecoder[ProtocolSlots.SIZE];
    private static volatile PacketRecord latestRecord;

    static {
        BLOCK_DECODERS[ProtocolSlots.GAME] = (in, record) -> record.game = GameBlockDecoder.read(in);
        BLOCK_DECODERS[ProtocolSlots.PLAYER] = (in, record) -> record.player = PlayerBlockDecoder.read(in);
        BLOCK_DECODERS[ProtocolSlots.COMBAT] = (in, record) -> record.combat = CombatBlockDecoder.read(in);
        BLOCK_DECODERS[ProtocolSlots.DECK_CARD] = (in, record) -> record.deck = CardBlockDecoder.readDeck(in);
        BLOCK_DECODERS[ProtocolSlots.HAND_CARD] = (in, record) -> record.hand = CardBlockDecoder.readCombat(in);
        BLOCK_DECODERS[ProtocolSlots.DRAW_PILE_CARD] = (in, record) -> record.drawPile = CardBlockDecoder.readCombat(in);
        BLOCK_DECODERS[ProtocolSlots.DISCARD_PILE_CARD] = (in, record) -> record.discardPile = CardBlockDecoder.readCombat(in);
        BLOCK_DECODERS[ProtocolSlots.EXHAUST_PILE_CARD] = (in, record) -> record.exhaustPile = CardBlockDecoder.readCombat(in);
        BLOCK_DECODERS[ProtocolSlots.MONSTER] = (in, record) -> record.monsters = MonsterBlockDecoder.read(in);
        BLOCK_DECODERS[ProtocolSlots.POTION] = (in, record) -> record.potions = PotionBlockDecoder.read(in);
        BLOCK_DECODERS[ProtocolSlots.SELECTION] = (in, record) -> record.selection = SelectionBlockDecoder.read(in);
        BLOCK_DECODERS[ProtocolSlots.MAP] = MapBlockDecoder::decode;
        BLOCK_DECODERS[ProtocolSlots.REWARD] = (in, record) -> record.rewards = RewardBlockDecoder.read(in);
        BLOCK_DECODERS[ProtocolSlots.EVENT] = (in, record) -> record.event = EventBlockDecoder.read(in);
        BLOCK_DECODERS[ProtocolSlots.SHOP] = (in, record) -> record.shop = ShopBlockDecoder.read(in);
        BLOCK_DECODERS[ProtocolSlots.REST] = (in, record) -> record.rest = RestBlockDecoder.read(in);
    }

    private PacketDecoder() {
    }

    /**
     * Decodes every present block, publishes the completed record, and returns the final bit offset.
     *
     * @throws IllegalArgumentException if the packet header is missing, truncated, or has the wrong run ID,
     *         if the expected run ID is outside 0–65535, or if a block contains invalid values
     * @throws BufferUnderflowException if a block extends past the end of the packet
     */
    public static int decode(byte[] packet, int expectedRunId) {
        Header header = tryReadHeader(packet, expectedRunId);
        if (header == null) {
            if (packet == null || packet.length < PacketHeaderProtocol.HEADER_BYTES) {
                throw new IllegalArgumentException("Packet header is missing or truncated");
            }
            throw new IllegalArgumentException("Packet runId does not match expectedRunId=" + expectedRunId);
        }

        PacketRecord record = new PacketRecord(header.runId);
        BitReader in = new BitReader(packet, header.bodyOffsetBytes() * Byte.SIZE);
        for (int slot = 0; slot < BLOCK_DECODERS.length; slot++) {
            if (header.hasBlock(slot)) {
                BLOCK_DECODERS[slot].decode(in, record);
            }
        }
        latestRecord = record;
        return in.bitPosition();
    }

    /** Returns the latest completed packet record, or {@code null} before the first acceptance. */
    public static PacketRecord latestRecord() {
        return latestRecord;
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
        void decode(BitReader in, PacketRecord record);
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
