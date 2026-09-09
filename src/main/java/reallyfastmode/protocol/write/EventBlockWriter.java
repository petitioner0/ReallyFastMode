package reallyfastmode.protocol.write;

import reallyfastmode.protocol.EventProtocol;
import reallyfastmode.protocol.build.EventSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes an {@link EventSnapshot} in the fixed Event slot order. */
public final class EventBlockWriter {
    private EventBlockWriter() {
    }

    public static void write(BitWriter out, EventSnapshot event) {
        Objects.requireNonNull(out, "out");
        validate(Objects.requireNonNull(event, "event"));

        out.writeBits(event.eventWireId, EventProtocol.EVENT_WIRE_ID_BITS);
        out.writeBits(event.optionsEntryCount,
            EventProtocol.OPTIONS_ENTRY_COUNT_BITS);
        for (int i = 0; i < event.optionsEntryCount; i++) {
            out.writeBit(event.optionsApplicability[i]);
        }
    }

    private static void validate(EventSnapshot event) {
        requireUnsigned("eventWireId", event.eventWireId,
            EventProtocol.EVENT_WIRE_ID_BITS);
        if (event.optionsEntryCount < 0
            || event.optionsEntryCount > EventProtocol.MAX_OPTIONS) {
            throw new IllegalArgumentException(
                "optionsEntryCount=" + event.optionsEntryCount
                    + " outside 0-" + EventProtocol.MAX_OPTIONS
            );
        }
        if (event.optionsApplicability == null
            || event.optionsApplicability.length != event.optionsEntryCount) {
            throw new IllegalArgumentException(
                "optionsApplicability length="
                    + (event.optionsApplicability == null
                        ? "null" : event.optionsApplicability.length)
                    + ", expected=" + event.optionsEntryCount
            );
        }
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }
}
