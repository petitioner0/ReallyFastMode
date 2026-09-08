package reallyfastmode.protocol.build;

import reallyfastmode.access.GameAccess;
import reallyfastmode.access.MapAccess;
import reallyfastmode.protocol.MapProtocol;

import java.util.Objects;

/** Captures either an Act 1-3 map scan or the hard-coded Act 4 marker. */
public final class MapSnapshotBuilder {
    private MapSnapshotBuilder() {
    }

    public static MapSnapshot build() {
        return buildFromSource(new StsSource());
    }

    static MapSnapshot buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int actNum = source.actNum();
        if (actNum == 4) {
            return new MapSnapshot(
                true,
                MapProtocol.NO_EMERALD_KEY_COORDINATE,
                MapProtocol.NO_EMERALD_KEY_COORDINATE
            );
        }
        if (actNum < 1 || actNum > 3) {
            throw new IllegalArgumentException("actNum=" + actNum + " outside 1-4");
        }

        int[] nodeWireId = new int[MapProtocol.NODE_COUNT];
        int nodeIndex = 0;
        for (int y = 0; y < MapProtocol.MAP_HEIGHT; y++) {
            for (int x = 0; x < MapProtocol.MAP_WIDTH; x++) {
                int wireId = source.nodeWireId(x, y);
                requireUnsigned("nodeWireId[" + nodeIndex + "]", wireId,
                    MapProtocol.NODE_WIRE_ID_BITS);
                nodeWireId[nodeIndex++] = wireId;
            }
        }

        int emeraldKeyX = MapProtocol.NO_EMERALD_KEY_COORDINATE;
        int emeraldKeyY = MapProtocol.NO_EMERALD_KEY_COORDINATE;
        for (int y = 0; y < MapProtocol.MAP_HEIGHT; y++) {
            for (int x = 0; x < MapProtocol.MAP_WIDTH; x++) {
                if (source.hasEmeraldKey(x, y)) {
                    if (emeraldKeyX != MapProtocol.NO_EMERALD_KEY_COORDINATE) {
                        throw new IllegalArgumentException(
                            "multiple emerald-key nodes at ("
                                + emeraldKeyX + "," + emeraldKeyY + ") and ("
                                + x + "," + y + ")"
                        );
                    }
                    emeraldKeyX = x;
                    emeraldKeyY = y;
                }
            }
        }

        MapSnapshot result = new MapSnapshot(false, emeraldKeyX, emeraldKeyY);
        System.arraycopy(nodeWireId, 0, result.nodeWireId, 0, nodeWireId.length);

        nodeIndex = 0;
        for (int y = 0; y < MapProtocol.MAP_HEIGHT; y++) {
            for (int x = 0; x < MapProtocol.MAP_WIDTH; x++) {
                int connectivity = source.connectivity(x, y);
                requireUnsigned("connectivity[" + nodeIndex + "]", connectivity,
                    MapProtocol.CONNECTIVITY_BITS);
                result.connectivity[nodeIndex++] = connectivity;
            }
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
        int actNum();

        int nodeWireId(int x, int y);

        boolean hasEmeraldKey(int x, int y);

        int connectivity(int x, int y);
    }

    private static final class StsSource implements Source {
        @Override
        public int actNum() {
            return GameAccess.act();
        }

        @Override
        public int nodeWireId(int x, int y) {
            return MapAccess.wireId(x, y);
        }

        @Override
        public boolean hasEmeraldKey(int x, int y) {
            return MapAccess.hasEmeraldKey(x, y);
        }

        @Override
        public int connectivity(int x, int y) {
            return MapAccess.connectivity(x, y);
        }
    }
}
