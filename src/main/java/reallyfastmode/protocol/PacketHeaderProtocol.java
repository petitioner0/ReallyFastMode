package reallyfastmode.protocol;

/** Fixed widths and presence-mask bits for a state packet header. */
public final class PacketHeaderProtocol {
    public static final int RUN_ID_BITS = Short.SIZE;
    public static final int MAX_RUN_ID = (1 << RUN_ID_BITS) - 1;
    public static final int PRESENCE_MASK_BITS = Short.SIZE;
    public static final int PRESENCE_MASK_BYTES = Short.BYTES;
    public static final int MAX_PRESENCE_MASK = (1 << PRESENCE_MASK_BITS) - 1;
    public static final int HEADER_BYTES = Short.BYTES + PRESENCE_MASK_BYTES;

    private PacketHeaderProtocol() {
    }

    public static boolean isValidRunId(int runId) {
        return runId >= 0 && runId <= MAX_RUN_ID;
    }

    public static int blockBit(int slot) {
        if (slot < 0 || slot >= ProtocolSlots.SIZE) {
            throw new IllegalArgumentException("Unknown protocol slot: " + slot);
        }
        return 1 << slot;
    }
}
