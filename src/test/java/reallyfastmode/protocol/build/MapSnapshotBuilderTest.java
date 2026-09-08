package reallyfastmode.protocol.build;

import org.junit.Test;
import reallyfastmode.protocol.MapProtocol;
import reallyfastmode.protocol.ProtocolSlots;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class MapSnapshotBuilderTest {
    @Test
    public void scansActOneThroughThreeLeftToRightAndBottomToTop() {
        RecordingSource source = new RecordingSource(2);

        MapSnapshot map = MapSnapshotBuilder.buildFromSource(source);

        assertFalse(map.isAct4);
        assertEquals(MapProtocol.NODE_COUNT, map.nodeWireId.length);
        assertEquals(3, map.emeraldKeyX);
        assertEquals(7, map.emeraldKeyY);
        assertEquals(MapProtocol.NODE_COUNT, map.connectivity.length);
        for (int index = 0; index < MapProtocol.NODE_COUNT; index++) {
            int x = index % MapProtocol.MAP_WIDTH;
            int y = index / MapProtocol.MAP_WIDTH;
            assertEquals((x + y) & 0xF, map.nodeWireId[index]);
            assertEquals((x + y) & 0x7, map.connectivity[index]);
            assertEquals("type:" + x + "," + y, source.calls.get(index));
            assertEquals("key:" + x + "," + y,
                source.calls.get(MapProtocol.NODE_COUNT + index));
            assertEquals("connectivity:" + x + "," + y,
                source.calls.get(MapProtocol.NODE_COUNT * 2 + index));
        }
    }

    @Test
    public void actFourOnlySetsTheHardCodedMarker() {
        MapSnapshot map = MapSnapshotBuilder.buildFromSource(new ActFourSource());

        assertTrue(map.isAct4);
        assertEquals(0, map.nodeWireId.length);
        assertEquals(MapProtocol.NO_EMERALD_KEY_COORDINATE, map.emeraldKeyX);
        assertEquals(MapProtocol.NO_EMERALD_KEY_COORDINATE, map.emeraldKeyY);
        assertEquals(0, map.connectivity.length);
    }

    @Test
    public void usesTheOutOfMapCoordinateWhenNoEmeraldKeyNodeExists() {
        MapSnapshot map = MapSnapshotBuilder.buildFromSource(new RecordingSource(1, -1));

        assertEquals(MapProtocol.NO_EMERALD_KEY_COORDINATE, map.emeraldKeyX);
        assertEquals(MapProtocol.NO_EMERALD_KEY_COORDINATE, map.emeraldKeyY);
    }

    @Test
    public void rejectsUnsupportedActsAndNodeIdsOutsideFourBits() {
        assertThrows(NullPointerException.class,
            () -> MapSnapshotBuilder.buildFromSource(null));
        assertThrows(IllegalArgumentException.class,
            () -> MapSnapshotBuilder.buildFromSource(new RecordingSource(0)));
        assertThrows(IllegalArgumentException.class,
            () -> MapSnapshotBuilder.buildFromSource(new InvalidWireIdSource(-1)));
        assertThrows(IllegalArgumentException.class,
            () -> MapSnapshotBuilder.buildFromSource(new InvalidWireIdSource(16)));
        assertThrows(IllegalArgumentException.class,
            () -> MapSnapshotBuilder.buildFromSource(new InvalidConnectivitySource(-1)));
        assertThrows(IllegalArgumentException.class,
            () -> MapSnapshotBuilder.buildFromSource(new InvalidConnectivitySource(8)));
        assertThrows(IllegalArgumentException.class,
            () -> MapSnapshotBuilder.buildFromSource(new MultipleEmeraldKeysSource()));
    }

    @Test
    public void constantsAndSlotsDescribeTheFixedMapShape() {
        assertEquals(7, MapProtocol.MAP_WIDTH);
        assertEquals(15, MapProtocol.MAP_HEIGHT);
        assertEquals(105, MapProtocol.NODE_COUNT);
        assertEquals(1, MapProtocol.IS_ACT_4_BITS);
        assertEquals(4, MapProtocol.NODE_WIRE_ID_BITS);
        assertEquals(4, MapProtocol.EMERALD_KEY_COORDINATE_BITS);
        assertEquals(8, MapProtocol.EMERALD_KEY_NODE_BITS);
        assertEquals(15, MapProtocol.NO_EMERALD_KEY_COORDINATE);
        assertEquals(3, MapProtocol.CONNECTIVITY_BITS);
        assertEquals(0, MapProtocol.CONNECTS_LEFT_BIT);
        assertEquals(1, MapProtocol.CONNECTS_CENTER_BIT);
        assertEquals(2, MapProtocol.CONNECTS_RIGHT_BIT);
        assertEquals(0, ProtocolSlots.Map.IS_ACT_4);
        assertEquals(1, ProtocolSlots.Map.FULL_MAP_TYPE);
        assertEquals(2, ProtocolSlots.Map.EMERALD_KEY_COORDINATE);
        assertEquals(3, ProtocolSlots.Map.CONNECTIVITY);
        assertEquals(4, ProtocolSlots.Map.SIZE);
    }

    private static class RecordingSource implements MapSnapshotBuilder.Source {
        private final int actNum;
        private final int emeraldKeyIndex;
        private final List<String> calls = new ArrayList<String>();

        private RecordingSource(int actNum) {
            this(actNum, 52);
        }

        private RecordingSource(int actNum, int emeraldKeyIndex) {
            this.actNum = actNum;
            this.emeraldKeyIndex = emeraldKeyIndex;
        }

        @Override
        public int actNum() {
            return actNum;
        }

        @Override
        public int nodeWireId(int x, int y) {
            calls.add("type:" + x + "," + y);
            return (x + y) & 0xF;
        }

        @Override
        public boolean hasEmeraldKey(int x, int y) {
            calls.add("key:" + x + "," + y);
            return y * MapProtocol.MAP_WIDTH + x == emeraldKeyIndex;
        }

        @Override
        public int connectivity(int x, int y) {
            calls.add("connectivity:" + x + "," + y);
            return (x + y) & 0x7;
        }
    }

    private static final class ActFourSource implements MapSnapshotBuilder.Source {
        @Override
        public int actNum() {
            return 4;
        }

        @Override
        public int nodeWireId(int x, int y) {
            throw new AssertionError("Act 4 must not scan map node types");
        }

        @Override
        public boolean hasEmeraldKey(int x, int y) {
            throw new AssertionError("Act 4 must not scan emerald-key flags");
        }

        @Override
        public int connectivity(int x, int y) {
            throw new AssertionError("Act 4 must not scan connectivity");
        }
    }

    private static final class InvalidWireIdSource extends RecordingSource {
        private final int wireId;

        private InvalidWireIdSource(int wireId) {
            super(1);
            this.wireId = wireId;
        }

        @Override
        public int nodeWireId(int x, int y) {
            return wireId;
        }
    }

    private static final class InvalidConnectivitySource extends RecordingSource {
        private final int connectivity;

        private InvalidConnectivitySource(int connectivity) {
            super(1);
            this.connectivity = connectivity;
        }

        @Override
        public int connectivity(int x, int y) {
            return connectivity;
        }
    }

    private static final class MultipleEmeraldKeysSource extends RecordingSource {
        private MultipleEmeraldKeysSource() {
            super(1);
        }

        @Override
        public boolean hasEmeraldKey(int x, int y) {
            return y == 0 && (x == 0 || x == 1);
        }
    }
}
