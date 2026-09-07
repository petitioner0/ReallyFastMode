package reallyfastmode.protocol.build;

import org.junit.Test;
import reallyfastmode.protocol.PotionProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaPotionCatalog;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PotionSnapshotBuilderTest {
    @Test
    public void capturesEveryPotionSlotInOrder() {
        FakeSource source = new FakeSource(3, new int[]{0, 43, 44});

        PotionSnapshot potions = PotionSnapshotBuilder.buildFromSource(source);

        assertEquals(3, potions.potionSlotsCount);
        assertArrayEquals(new int[]{0, 43, 44}, potions.potionWireId);
    }

    @Test
    public void sentinelWireIdsFollowTheCatalog() {
        assertEquals(VanillaPotionCatalog.EMPTY.wireId,
            PotionProtocol.EMPTY_POTION_WIRE_ID);
        assertEquals(VanillaPotionCatalog.UNKNOWN.wireId,
            PotionProtocol.UNKNOWN_POTION_WIRE_ID);
    }

    @Test
    public void acceptsZeroAndSevenSlots() {
        assertEquals(0,
            PotionSnapshotBuilder.buildFromSource(new FakeSource(0, new int[0]))
                .potionSlotsCount);
        assertEquals(7,
            PotionSnapshotBuilder.buildFromSource(new FakeSource(7, new int[7]))
                .potionSlotsCount);
    }

    @Test
    public void requiresTheInventoryLengthToMatchPlayerPotionSlots() {
        assertThrows(IllegalArgumentException.class,
            () -> PotionSnapshotBuilder.buildFromSource(
                new FakeSource(3, new int[]{1, 2})
            ));
    }

    @Test
    public void rejectsNullSourcesAndValuesOutsideTheirBitWidths() {
        assertThrows(NullPointerException.class,
            () -> PotionSnapshotBuilder.buildFromSource(null));
        assertThrows(IllegalArgumentException.class,
            () -> PotionSnapshotBuilder.buildFromSource(new FakeSource(-1, new int[0])));
        assertThrows(IllegalArgumentException.class,
            () -> PotionSnapshotBuilder.buildFromSource(new FakeSource(8, new int[8])));
        assertThrows(IllegalArgumentException.class,
            () -> PotionSnapshotBuilder.buildFromSource(new FakeSource(1, new int[]{64})));
    }

    private static final class FakeSource implements PotionSnapshotBuilder.Source {
        private final int potionSlotsCount;
        private final int[] potionWireId;

        private FakeSource(int potionSlotsCount, int[] potionWireId) {
            this.potionSlotsCount = potionSlotsCount;
            this.potionWireId = potionWireId;
        }

        @Override
        public int potionSlotsCount() {
            return potionSlotsCount;
        }

        @Override
        public int potionSize() {
            return potionWireId.length;
        }

        @Override
        public int potionWireId(int potionIndex) {
            return potionWireId[potionIndex];
        }
    }
}
