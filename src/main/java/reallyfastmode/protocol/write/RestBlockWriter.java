package reallyfastmode.protocol.write;

import reallyfastmode.protocol.RestProtocol;
import reallyfastmode.protocol.build.RestSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes currently usable campfire options as an eight-bit mask. */
public final class RestBlockWriter {
    private RestBlockWriter() {
    }

    public static void write(BitWriter out, RestSnapshot rest) {
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(rest, "rest");

        int mask = 0;
        for (int wireId : rest.optionsWireId) {
            if (wireId < 0 || wireId >= RestProtocol.AVAILABLE_OPTIONS_MASK_BITS) {
                throw new IllegalArgumentException("Invalid rest option wire ID: " + wireId);
            }
            mask |= 1 << wireId;
        }

        out.writeBits(mask, RestProtocol.AVAILABLE_OPTIONS_MASK_BITS);
    }
}
