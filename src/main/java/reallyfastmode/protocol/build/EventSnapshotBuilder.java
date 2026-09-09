package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import reallyfastmode.access.EventAccess;
import reallyfastmode.protocol.EventProtocol;

import java.util.List;
import java.util.Objects;

/** Captures the active event and its option applicability from top to bottom. */
public final class EventSnapshotBuilder {
    private EventSnapshotBuilder() {
    }

    public static EventSnapshot build() {
        return buildFromSource(new StsSource());
    }

    static EventSnapshot buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int eventWireId = source.eventWireId();
        requireUnsigned("eventWireId", eventWireId,
            EventProtocol.EVENT_WIRE_ID_BITS);

        int optionsEntryCount = source.optionsEntryCount();
        if (optionsEntryCount < 0 || optionsEntryCount > EventProtocol.MAX_OPTIONS) {
            throw new IllegalArgumentException(
                "optionsEntryCount=" + optionsEntryCount
                    + " outside 0-" + EventProtocol.MAX_OPTIONS
            );
        }

        EventSnapshot result = new EventSnapshot(eventWireId, optionsEntryCount);
        for (int i = 0; i < optionsEntryCount; i++) {
            result.optionsApplicability[i] = source.isOptionApplicable(i);
        }
        return result;
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }

    interface Source {
        int eventWireId();

        int optionsEntryCount();

        boolean isOptionApplicable(int optionIndex);
    }

    private static final class StsSource implements Source {
        private final int eventWireId;
        private final List<LargeDialogOptionButton> options;

        private StsSource() {
            this.eventWireId = EventAccess.wireId();
            this.options = EventAccess.options();
        }

        @Override
        public int eventWireId() {
            return eventWireId;
        }

        @Override
        public int optionsEntryCount() {
            return options.size();
        }

        @Override
        public boolean isOptionApplicable(int optionIndex) {
            return EventAccess.isSelectable(options.get(optionIndex));
        }
    }
}
