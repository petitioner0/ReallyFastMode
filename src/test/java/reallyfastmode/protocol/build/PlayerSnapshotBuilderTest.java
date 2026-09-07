package reallyfastmode.protocol.build;

import org.junit.Test;
import reallyfastmode.protocol.PlayerProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaOrbCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaPowerCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaRelicCatalog;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PlayerSnapshotBuilderTest {
    @Test
    public void capturesScalarsAndVariableLengthColumns() {
        FakeSource source = new FakeSource();
        source.hp = 73;
        source.maxHp = 91;
        source.block = 17;
        source.energy = 4;
        source.gold = 321;
        source.maxOrbs = 3;
        source.orbWireId = new int[]{3, 1, PlayerProtocol.UNKNOWN_ORB_WIRE_ID};
        source.powerWireId = new int[]{5, PlayerProtocol.UNKNOWN_POWER_WIRE_ID};
        source.powerCounts = new int[]{2, -1};
        source.relicWireId = new int[]{0, PlayerProtocol.UNKNOWN_RELIC_WIRE_ID};
        source.relicCounts = new int[]{-1, 7};

        PlayerSnapshot player = PlayerSnapshotBuilder.buildFromSource(source);

        assertEquals(73, player.hp);
        assertEquals(91, player.maxHp);
        assertEquals(17, player.block);
        assertEquals(4, player.energy);
        assertEquals(321, player.gold);
        assertEquals(3, player.maxOrbs);
        assertArrayEquals(new int[]{3, 1, 5}, player.orbWireId);
        assertArrayEquals(new int[]{5, 159}, player.powerWireId);
        assertArrayEquals(new int[]{2, -1}, player.powerCounts);
        assertArrayEquals(new int[]{0, 190}, player.relicWireId);
        assertArrayEquals(new int[]{0, 7}, player.relicCounts);
    }

    @Test
    public void supportsAPlayerWithoutOrbsPowersOrRelics() {
        PlayerSnapshot player = PlayerSnapshotBuilder.buildFromSource(new FakeSource());

        assertEquals(0, player.maxOrbs);
        assertArrayEquals(new int[0], player.orbWireId);
        assertArrayEquals(new int[0], player.powerWireId);
        assertArrayEquals(new int[0], player.powerCounts);
        assertArrayEquals(new int[0], player.relicWireId);
        assertArrayEquals(new int[0], player.relicCounts);
    }

    @Test
    public void unknownWireIdsFollowCurrentCatalogTails() {
        assertEquals(VanillaOrbCatalog.values().length,
            PlayerProtocol.UNKNOWN_ORB_WIRE_ID);
        assertEquals(VanillaPowerCatalog.values().length,
            PlayerProtocol.UNKNOWN_POWER_WIRE_ID);
        assertEquals(VanillaRelicCatalog.values().length,
            PlayerProtocol.UNKNOWN_RELIC_WIRE_ID);
    }

    @Test
    public void requiresOneOrbEntryPerMaxOrbSlot() {
        FakeSource missingOrb = new FakeSource();
        missingOrb.maxOrbs = 1;
        assertThrows(IllegalArgumentException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(missingOrb));

        FakeSource tooManyOrbs = new FakeSource();
        tooManyOrbs.maxOrbs = PlayerProtocol.MAX_ORBS + 1;
        tooManyOrbs.orbWireId = new int[PlayerProtocol.MAX_ORBS + 1];
        assertThrows(IllegalArgumentException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(tooManyOrbs));
    }

    @Test
    public void rejectsNullSourcesAndValuesOutsideTheirBitWidths() {
        assertThrows(NullPointerException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(null));

        FakeSource hpOverflow = new FakeSource();
        hpOverflow.hp = 1024;
        assertThrows(IllegalArgumentException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(hpOverflow));

        FakeSource blockOverflow = new FakeSource();
        blockOverflow.block = 1024;
        assertThrows(IllegalArgumentException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(blockOverflow));

        FakeSource goldOverflow = new FakeSource();
        goldOverflow.gold = 4096;
        assertThrows(IllegalArgumentException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(goldOverflow));

        FakeSource orbOverflow = new FakeSource();
        orbOverflow.maxOrbs = 1;
        orbOverflow.orbWireId = new int[]{8};
        assertThrows(IllegalArgumentException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(orbOverflow));

        FakeSource powerOverflow = new FakeSource();
        powerOverflow.powerWireId = new int[]{256};
        powerOverflow.powerCounts = new int[]{0};
        assertThrows(IllegalArgumentException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(powerOverflow));

        FakeSource lowPowerCount = new FakeSource();
        lowPowerCount.powerWireId = new int[]{0};
        lowPowerCount.powerCounts = new int[]{-2049};
        assertThrows(IllegalArgumentException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(lowPowerCount));

        FakeSource relicOverflow = new FakeSource();
        relicOverflow.relicWireId = new int[]{256};
        relicOverflow.relicCounts = new int[]{0};
        assertThrows(IllegalArgumentException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(relicOverflow));

        FakeSource highRelicCount = new FakeSource();
        highRelicCount.relicWireId = new int[]{0};
        highRelicCount.relicCounts = new int[]{256};
        assertThrows(IllegalArgumentException.class,
            () -> PlayerSnapshotBuilder.buildFromSource(highRelicCount));
    }

    private static final class FakeSource implements PlayerSnapshotBuilder.Source {
        private int hp;
        private int maxHp;
        private int block;
        private int energy;
        private int gold;
        private int maxOrbs;
        private int[] orbWireId = new int[0];
        private int[] powerWireId = new int[0];
        private int[] powerCounts = new int[0];
        private int[] relicWireId = new int[0];
        private int[] relicCounts = new int[0];

        @Override
        public int hp() {
            return hp;
        }

        @Override
        public int maxHp() {
            return maxHp;
        }

        @Override
        public int block() {
            return block;
        }

        @Override
        public int energy() {
            return energy;
        }

        @Override
        public int gold() {
            return gold;
        }

        @Override
        public int maxOrbs() {
            return maxOrbs;
        }

        @Override
        public int orbSize() {
            return orbWireId.length;
        }

        @Override
        public int orbWireId(int orbIndex) {
            return orbWireId[orbIndex];
        }

        @Override
        public int powerSize() {
            return powerWireId.length;
        }

        @Override
        public int powerWireId(int powerIndex) {
            return powerWireId[powerIndex];
        }

        @Override
        public int powerCount(int powerIndex) {
            return powerCounts[powerIndex];
        }

        @Override
        public int relicSize() {
            return relicWireId.length;
        }

        @Override
        public int relicWireId(int relicIndex) {
            return relicWireId[relicIndex];
        }

        @Override
        public int relicCount(int relicIndex) {
            return relicCounts[relicIndex];
        }
    }
}
