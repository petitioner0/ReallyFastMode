package reallyfastmode.protocol.build;

import org.junit.Test;
import reallyfastmode.protocol.EventProtocol;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.VanillaCatalog.VanillaEventCatalog;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class EventSnapshotBuilderTest {
    @Test
    public void capturesApplicabilityFromTopToBottom() {
        EventSnapshot event = EventSnapshotBuilder.buildFromSource(
            new FakeSource(51, true, false, true, false)
        );

        assertEquals(51, event.eventWireId);
        assertEquals(4, event.optionsEntryCount);
        assertArrayEquals(new boolean[]{true, false, true, false},
            event.optionsApplicability);
    }

    @Test
    public void acceptsTheFullConfiguredRanges() {
        EventSnapshot event = EventSnapshotBuilder.buildFromSource(
            new FakeSource(EventProtocol.MAX_EVENT_WIRE_ID,
                true, true, true, true, true, true, true)
        );

        assertEquals(EventProtocol.MAX_EVENT_WIRE_ID, event.eventWireId);
        assertEquals(EventProtocol.MAX_OPTIONS, event.optionsEntryCount);
        assertEquals(EventProtocol.MAX_OPTIONS, event.optionsApplicability.length);
    }

    @Test
    public void acceptsAnEventWithNoDisplayedOptions() {
        EventSnapshot event = EventSnapshotBuilder.buildFromSource(
            new FakeSource(0)
        );

        assertEquals(0, event.optionsEntryCount);
        assertArrayEquals(new boolean[0], event.optionsApplicability);
    }

    @Test
    public void rejectsNullAndValuesOutsideTheirBitWidths() {
        assertThrows(NullPointerException.class,
            () -> EventSnapshotBuilder.buildFromSource(null));
        assertThrows(IllegalArgumentException.class,
            () -> EventSnapshotBuilder.buildFromSource(new FakeSource(-1)));
        assertThrows(IllegalArgumentException.class,
            () -> EventSnapshotBuilder.buildFromSource(new FakeSource(64)));
        assertThrows(IllegalArgumentException.class,
            () -> EventSnapshotBuilder.buildFromSource(new SizedSource(0, -1)));
        assertThrows(IllegalArgumentException.class,
            () -> EventSnapshotBuilder.buildFromSource(new SizedSource(0, 8)));
    }

    @Test
    public void constantsAndSlotsDescribeTheThreeEventColumns() {
        assertEquals(6, EventProtocol.EVENT_WIRE_ID_BITS);
        assertEquals(3, EventProtocol.OPTIONS_ENTRY_COUNT_BITS);
        assertEquals(1, EventProtocol.OPTIONS_APPLICABILITY_BITS);
        assertEquals(9, EventProtocol.FIXED_BITS);
        assertEquals(VanillaEventCatalog.UNKNOWN.wireId,
            EventProtocol.UNKNOWN_EVENT_WIRE_ID);
        assertEquals(63, EventProtocol.UNKNOWN_EVENT_WIRE_ID);
        assertEquals(0, ProtocolSlots.Event.EVENT_WIRE_ID);
        assertEquals(1, ProtocolSlots.Event.OPTIONS_ENTRY_COUNT);
        assertEquals(2, ProtocolSlots.Event.OPTIONS_APPLICABILITY);
        assertEquals(3, ProtocolSlots.Event.SIZE);
    }

    private static final class FakeSource implements EventSnapshotBuilder.Source {
        private final int eventWireId;
        private final boolean[] applicability;

        private FakeSource(int eventWireId, boolean... applicability) {
            this.eventWireId = eventWireId;
            this.applicability = applicability;
        }

        @Override
        public int eventWireId() {
            return eventWireId;
        }

        @Override
        public int optionsEntryCount() {
            return applicability.length;
        }

        @Override
        public boolean isOptionApplicable(int optionIndex) {
            return applicability[optionIndex];
        }
    }

    private static final class SizedSource implements EventSnapshotBuilder.Source {
        private final int eventWireId;
        private final int optionCount;

        private SizedSource(int eventWireId, int optionCount) {
            this.eventWireId = eventWireId;
            this.optionCount = optionCount;
        }

        @Override
        public int eventWireId() {
            return eventWireId;
        }

        @Override
        public int optionsEntryCount() {
            return optionCount;
        }

        @Override
        public boolean isOptionApplicable(int optionIndex) {
            throw new AssertionError("invalid counts must fail before reading options");
        }
    }
}
