package reallyfastmode.protocol.write;

import reallyfastmode.protocol.PacketHeaderProtocol;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Writes the run ID followed by the block presence mask, both big-endian. */
public final class PacketHeaderWriter {
    private PacketHeaderWriter() {
    }

    public static void write(BitWriter out, int runId, int presenceMask) {
        Objects.requireNonNull(out, "out");
        if (!PacketHeaderProtocol.isValidRunId(runId)) {
            throw new IllegalArgumentException("runId=" + runId + " outside 0-"
                + PacketHeaderProtocol.MAX_RUN_ID);
        }
        if (presenceMask < 0 || presenceMask > PacketHeaderProtocol.MAX_PRESENCE_MASK) {
            throw new IllegalArgumentException("Unknown bits in presenceMask=" + presenceMask);
        }

        out.writeUnsignedShort(runId);
        out.writeUnsignedShort(presenceMask);
    }
}
