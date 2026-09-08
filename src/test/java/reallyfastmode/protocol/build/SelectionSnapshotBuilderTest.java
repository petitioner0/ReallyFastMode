package reallyfastmode.protocol.build;

import org.junit.Test;
import reallyfastmode.protocol.CardProtocol;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.SelectionProtocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SelectionSnapshotBuilderTest {
    @Test
    public void capturesCandidateCardWireIdsInOrder() {
        SelectionSnapshot selection = SelectionSnapshotBuilder.buildFromSource(
            new FakeSource(new int[]{7, CardProtocol.UNKNOWN_CARD_WIRE_ID, 31})
        );

        assertEquals(3, selection.candidatesCardCount);
        assertArrayEquals(new int[]{7, CardProtocol.UNKNOWN_CARD_WIRE_ID, 31},
            selection.candidatesCardWireId);
    }

    @Test
    public void acceptsTheFullUnsignedEightBitCountRange() {
        assertEquals(0,
            SelectionSnapshotBuilder.buildFromSource(new FakeSource(new int[0]))
                .candidatesCardCount);
        assertEquals(255,
            SelectionSnapshotBuilder.buildFromSource(
                new FakeSource(new int[SelectionProtocol.MAX_CANDIDATES])
            ).candidatesCardCount);
    }

    @Test
    public void rejectsNullSourcesAndCountsOutsideEightBits() {
        assertThrows(NullPointerException.class,
            () -> SelectionSnapshotBuilder.buildFromSource(null));
        assertThrows(IllegalArgumentException.class,
            () -> SelectionSnapshotBuilder.buildFromSource(new SizedSource(-1)));
        assertThrows(IllegalArgumentException.class,
            () -> SelectionSnapshotBuilder.buildFromSource(new SizedSource(256)));
    }

    @Test
    public void rejectsCardWireIdsOutsideNineBits() {
        assertThrows(IllegalArgumentException.class,
            () -> SelectionSnapshotBuilder.buildFromSource(
                new FakeSource(new int[]{-1})
            ));
        assertThrows(IllegalArgumentException.class,
            () -> SelectionSnapshotBuilder.buildFromSource(
                new FakeSource(new int[]{512})
            ));
    }

    @Test
    public void selectionWireIdWidthMatchesCardWireIdWidth() {
        assertEquals(CardProtocol.CARD_WIRE_ID_BITS,
            SelectionProtocol.CANDIDATES_CARD_WIRE_ID_BITS);
    }

    @Test
    public void selectionSlotsPlaceCountBeforeWireIds() {
        assertEquals(0, ProtocolSlots.Selection.CANDIDATES_CARD_COUNT);
        assertEquals(1, ProtocolSlots.Selection.CANDIDATES_CARD_WIRE_ID);
        assertEquals(2, ProtocolSlots.Selection.SIZE);
    }

    private static final class FakeSource implements SelectionSnapshotBuilder.Source {
        private final int[] candidatesCardWireId;

        private FakeSource(int[] candidatesCardWireId) {
            this.candidatesCardWireId = candidatesCardWireId;
        }

        @Override
        public int candidatesCardCount() {
            return candidatesCardWireId.length;
        }

        @Override
        public int candidatesCardWireId(int candidateIndex) {
            return candidatesCardWireId[candidateIndex];
        }
    }

    private static final class SizedSource implements SelectionSnapshotBuilder.Source {
        private final int size;

        private SizedSource(int size) {
            this.size = size;
        }

        @Override
        public int candidatesCardCount() {
            return size;
        }

        @Override
        public int candidatesCardWireId(int candidateIndex) {
            throw new AssertionError("invalid counts must fail before reading cards");
        }
    }
}
