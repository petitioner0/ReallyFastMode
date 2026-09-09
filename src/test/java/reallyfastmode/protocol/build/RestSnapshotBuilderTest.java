package reallyfastmode.protocol.build;

import org.junit.Test;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.VanillaCatalog.VanillaRestOptionCatalog;
import reallyfastmode.protocol.RestProtocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RestSnapshotBuilderTest {
    @Test
    public void capturesOnlyUsableOptionsInDisplayOrder() {
        RestSnapshot rest = RestSnapshotBuilder.buildFromSource(
            new FakeSource(
                new int[]{0, 1, 4, 5, 7},
                new boolean[]{true, false, true, false, true}
            )
        );

        assertEquals(3, rest.optionsCount);
        assertArrayEquals(new int[]{0, 4, 7}, rest.optionsWireId);
    }

    @Test
    public void acceptsNoOptionsAndMoreThanEightDisplayedEntries() {
        assertEquals(0, RestSnapshotBuilder.buildFromSource(
            new FakeSource(new int[0], new boolean[0])
        ).optionsCount);

        RestSnapshot repeated = RestSnapshotBuilder.buildFromSource(
            new FakeSource(
                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 0},
                new boolean[]{true, true, true, true, true, true, true, true, true}
            )
        );
        assertEquals(9, repeated.optionsCount);
    }

    @Test
    public void rejectsNullAndNegativeDisplayedCounts() {
        assertThrows(NullPointerException.class,
            () -> RestSnapshotBuilder.buildFromSource(null));
        assertThrows(IllegalArgumentException.class,
            () -> RestSnapshotBuilder.buildFromSource(new SizedSource(-1)));
    }

    @Test
    public void rejectsUsableWireIdsOutsideThreeBits() {
        assertThrows(IllegalArgumentException.class,
            () -> RestSnapshotBuilder.buildFromSource(
                new FakeSource(new int[]{-1}, new boolean[]{true})
            ));
        assertThrows(IllegalArgumentException.class,
            () -> RestSnapshotBuilder.buildFromSource(
                new FakeSource(new int[]{8}, new boolean[]{true})
            ));
    }

    @Test
    public void ignoresTheWireIdOfAnUnusableOption() {
        RestSnapshot rest = RestSnapshotBuilder.buildFromSource(
            new FakeSource(new int[]{99}, new boolean[]{false})
        );

        assertEquals(0, rest.optionsCount);
    }

    @Test
    public void constantsAndSlotsDescribeTheOptionWireIds() {
        assertEquals(3, RestProtocol.OPTION_WIRE_ID_BITS);
        assertEquals(7, RestProtocol.MAX_OPTION_WIRE_ID);
        assertEquals(VanillaRestOptionCatalog.UNKNOWN.wireId,
            RestProtocol.UNKNOWN_OPTION_WIRE_ID);
        assertEquals(0, ProtocolSlots.Rest.OPTIONS_WIRE_ID);
        assertEquals(1, ProtocolSlots.Rest.SIZE);
    }

    private static final class FakeSource implements RestSnapshotBuilder.Source {
        private final int[] optionWireId;
        private final boolean[] usable;

        private FakeSource(int[] optionWireId, boolean[] usable) {
            this.optionWireId = optionWireId;
            this.usable = usable;
        }

        @Override
        public int optionEntryCount() {
            return optionWireId.length;
        }

        @Override
        public int optionWireId(int optionIndex) {
            return optionWireId[optionIndex];
        }

        @Override
        public boolean usable(int optionIndex) {
            return usable[optionIndex];
        }
    }

    private static final class SizedSource implements RestSnapshotBuilder.Source {
        private final int size;

        private SizedSource(int size) {
            this.size = size;
        }

        @Override
        public int optionEntryCount() {
            return size;
        }

        @Override
        public int optionWireId(int optionIndex) {
            throw new AssertionError("invalid counts must fail before reading options");
        }

        @Override
        public boolean usable(int optionIndex) {
            throw new AssertionError("invalid counts must fail before reading options");
        }
    }
}
