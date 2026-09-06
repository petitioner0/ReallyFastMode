package reallyfastmode.protocol.build;

import org.junit.Test;
import reallyfastmode.protocol.VanillaActCatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GameSnapshotBuilderTest {
    @Test
    public void capturesOneScalarGameSnapshot() {
        FakeSource source = new FakeSource();
        source.actNum = 3;
        source.floor = 63;
        source.ascensionLevel = 31;
        source.hasRubyKey = true;
        source.hasEmeraldKey = false;
        source.hasSapphireKey = true;

        GameSnapshot game = GameSnapshotBuilder.buildFromSource(source);

        assertEquals(VanillaActCatalog.ACT_3.wireId, game.actWireId);
        assertEquals(63, game.floor);
        assertEquals(31, game.ascensionLevel);
        assertTrue(game.hasRubyKey);
        assertFalse(game.hasEmeraldKey);
        assertTrue(game.hasSapphireKey);
    }

    @Test
    public void normalizesNonVanillaActNumbersToUnknown() {
        FakeSource source = new FakeSource();

        source.actNum = 0;
        assertEquals(VanillaActCatalog.UNKNOWN.wireId,
            GameSnapshotBuilder.buildFromSource(source).actWireId);

        source.actNum = 5;
        assertEquals(VanillaActCatalog.UNKNOWN.wireId,
            GameSnapshotBuilder.buildFromSource(source).actWireId);
    }

    @Test
    public void rejectsNullSourcesAndValuesOutsideTheirBitWidths() {
        assertThrows(NullPointerException.class,
            () -> GameSnapshotBuilder.buildFromSource(null));

        FakeSource source = new FakeSource();
        source.floor = -1;
        assertThrows(IllegalArgumentException.class,
            () -> GameSnapshotBuilder.buildFromSource(source));

        source.floor = 64;
        assertThrows(IllegalArgumentException.class,
            () -> GameSnapshotBuilder.buildFromSource(source));

        source.floor = 0;
        source.ascensionLevel = -1;
        assertThrows(IllegalArgumentException.class,
            () -> GameSnapshotBuilder.buildFromSource(source));

        source.ascensionLevel = 32;
        assertThrows(IllegalArgumentException.class,
            () -> GameSnapshotBuilder.buildFromSource(source));
    }

    private static final class FakeSource implements GameSnapshotBuilder.Source {
        private int actNum = 1;
        private int floor;
        private int ascensionLevel;
        private boolean hasRubyKey;
        private boolean hasEmeraldKey;
        private boolean hasSapphireKey;

        @Override
        public int actNum() {
            return actNum;
        }

        @Override
        public int floor() {
            return floor;
        }

        @Override
        public int ascensionLevel() {
            return ascensionLevel;
        }

        @Override
        public boolean hasRubyKey() {
            return hasRubyKey;
        }

        @Override
        public boolean hasEmeraldKey() {
            return hasEmeraldKey;
        }

        @Override
        public boolean hasSapphireKey() {
            return hasSapphireKey;
        }
    }
}
