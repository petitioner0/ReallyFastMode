package reallyfastmode.protocol.build;

import org.junit.Test;
import reallyfastmode.protocol.CombatProtocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CombatSnapshotBuilderTest {
    @Test
    public void capturesTheCurrentTurn() {
        assertEquals(37, CombatSnapshotBuilder.buildFromSource(() -> 37).turn);
    }

    @Test
    public void acceptsTheFullUnsignedEightBitRange() {
        assertEquals(0, CombatSnapshotBuilder.buildFromSource(() -> 0).turn);
        assertEquals(255,
            CombatSnapshotBuilder.buildFromSource(() -> CombatProtocol.MAX_TURN).turn);
    }

    @Test
    public void rejectsNullSourcesAndOutOfRangeTurns() {
        assertThrows(NullPointerException.class,
            () -> CombatSnapshotBuilder.buildFromSource(null));
        assertThrows(IllegalArgumentException.class,
            () -> CombatSnapshotBuilder.buildFromSource(() -> -1));
        assertThrows(IllegalArgumentException.class,
            () -> CombatSnapshotBuilder.buildFromSource(() -> 256));
    }
}
