package reallyfastmode.protocol.write;

import reallyfastmode.protocol.MapProtocol;
import reallyfastmode.protocol.build.MapSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes the fixed full-map layout from a {@link MapSnapshot}. */
public final class MapBlockWriter {
    private MapBlockWriter() {
    }

    public static void write(BitWriter out, MapSnapshot map) {
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(map, "map");

        if (!map.isAct4) {
            validateEmeraldKey(map.emeraldKeyX, map.emeraldKeyY);
            validateColumn("nodeWireId", map.nodeWireId, MapProtocol.NODE_WIRE_ID_BITS);
            validateColumn("connectivity", map.connectivity, MapProtocol.CONNECTIVITY_BITS);
        }

        int prefix = (MapProtocol.FULL_MAP_KIND << MapProtocol.IS_ACT_4_BITS) | (map.isAct4 ? 1 : 0);
        out.writeBits(prefix, MapProtocol.BLOCK_KIND_BITS + MapProtocol.IS_ACT_4_BITS);
        if (map.isAct4) {
            return;
        }

        for (int wireId : map.nodeWireId) {
            out.writeBits(wireId, MapProtocol.NODE_WIRE_ID_BITS);
        }
        out.writeBits(map.emeraldKeyX, MapProtocol.EMERALD_KEY_COORDINATE_BITS);
        out.writeBits(map.emeraldKeyY, MapProtocol.EMERALD_KEY_COORDINATE_BITS);
        for (int connectivity : map.connectivity) {
            out.writeBits(connectivity, MapProtocol.CONNECTIVITY_BITS);
        }
    }

    private static void validateEmeraldKey(int x, int y) {
        boolean validNode = x >= 0 && x < MapProtocol.MAP_WIDTH
            && y >= 0 && y < MapProtocol.MAP_HEIGHT;
        boolean noKey = x == MapProtocol.NO_EMERALD_KEY_COORDINATE
            && y == MapProtocol.NO_EMERALD_KEY_COORDINATE;
        if (!validNode && !noKey) {
            throw new IllegalArgumentException("Invalid emerald-key coordinate (" + x + "," + y + ")");
        }
    }

    private static void validateColumn(String name, int[] values, int bits) {
        int max = (1 << bits) - 1;
        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            if (value < 0 || value > max) {
                throw new IllegalArgumentException(
                    name + "[" + i + "]=" + value + " outside 0-" + max
                );
            }
        }
    }
}
