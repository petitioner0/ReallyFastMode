package reallyfastmode.protocol.build;

import org.junit.Test;
import reallyfastmode.protocol.CurrentMapSelectionProtocol;
import reallyfastmode.protocol.ProtocolSlots;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CurrentMapSelectionSnapshotBuilderTest {
    @Test
    public void capturesCoordinatesInXThenYOrder() {
        CurrentMapSelectionSnapshot selection =
            CurrentMapSelectionSnapshotBuilder.buildFromSource(
                new FakeSource(3, 8)
            );

        assertEquals(3, selection.currentNodeX);
        assertEquals(8, selection.currentNodeY);
    }

    @Test
    public void encodesThePreMapMinusOneCoordinateAsFourOneBits() {
        CurrentMapSelectionSnapshot selection =
            CurrentMapSelectionSnapshotBuilder.buildFromSource(
                new FakeSource(0, -1)
            );

        assertEquals(0, selection.currentNodeX);
        assertEquals(CurrentMapSelectionProtocol.NO_PREVIOUS_ROW_WIRE_VALUE,
            selection.currentNodeY);
    }

    @Test
    public void rejectsCoordinatesOutsideFourBits() {
        assertThrows(NullPointerException.class,
            () -> CurrentMapSelectionSnapshotBuilder.buildFromSource(null));
        assertThrows(IllegalArgumentException.class,
            () -> CurrentMapSelectionSnapshotBuilder.buildFromSource(
                new FakeSource(-2, 0)
            ));
        assertThrows(IllegalArgumentException.class,
            () -> CurrentMapSelectionSnapshotBuilder.buildFromSource(
                new FakeSource(0, 16)
            ));
    }

    @Test
    public void constantsAndSlotsDescribeTheFixedSelectionShape() {
        assertEquals(4, CurrentMapSelectionProtocol.COORDINATE_BITS);
        assertEquals(8, CurrentMapSelectionProtocol.CURRENT_NODE_BITS);
        assertEquals(0, ProtocolSlots.CurrentMapSelection.CURRENT_NODE);
        assertEquals(1, ProtocolSlots.CurrentMapSelection.SIZE);
    }

    private static final class FakeSource
        implements CurrentMapSelectionSnapshotBuilder.Source {
        private final int currentNodeX;
        private final int currentNodeY;

        private FakeSource(int currentNodeX, int currentNodeY) {
            this.currentNodeX = currentNodeX;
            this.currentNodeY = currentNodeY;
        }

        @Override
        public int currentNodeX() {
            return currentNodeX;
        }

        @Override
        public int currentNodeY() {
            return currentNodeY;
        }
    }
}
