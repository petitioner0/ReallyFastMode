package reallyfastmode.protocol.read;

import reallyfastmode.protocol.CurrentMapSelectionProtocol;
import reallyfastmode.protocol.MapProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaNodeCatalog;
import reallyfastmode.protocol.build.CurrentMapSelectionSnapshot;
import reallyfastmode.protocol.io.BitReader;

import java.util.Arrays;

final class MapBlockDecoder {
    private static final String[] NODE_TYPES = new String[1 << MapProtocol.NODE_WIRE_ID_BITS];
    private static final int NO_COLUMN_WIRE_VALUE = (1 << CurrentMapSelectionProtocol.COORDINATE_BITS) - 1;

    static {
        Arrays.fill(NODE_TYPES, WireIds.UNKNOWN_ID);
        for (VanillaNodeCatalog node : VanillaNodeCatalog.values()) {
            if (node != VanillaNodeCatalog.UNKNOWN) {
                NODE_TYPES[node.wireId] = node.name();
            }
        }
    }

    private MapBlockDecoder() {
    }

    static void decode(BitReader in, PacketRecord record) {
        int kind = in.readBits(MapProtocol.BLOCK_KIND_BITS);
        if (kind == MapProtocol.FULL_MAP_KIND) {
            record.map = readFullMap(in);
        } else {
            record.currentMapSelection = readCurrentNode(in);
        }
    }

    private static PacketRecord.DungeonMap readFullMap(BitReader in) {
        int isAct4 = in.readBits(MapProtocol.IS_ACT_4_BITS);
        if (isAct4 == 1) {
            return new PacketRecord.DungeonMap(isAct4, -1, -1,
                new String[0], new int[0], new int[0], new int[0]);
        }

        String[] nodeType = WireIds.readColumn(in, MapProtocol.NODE_COUNT, MapProtocol.NODE_WIRE_ID_BITS, NODE_TYPES);
        int keyX = in.readBits(MapProtocol.EMERALD_KEY_COORDINATE_BITS);
        int keyY = in.readBits(MapProtocol.EMERALD_KEY_COORDINATE_BITS);
        if (keyX == MapProtocol.NO_EMERALD_KEY_COORDINATE && keyY == MapProtocol.NO_EMERALD_KEY_COORDINATE) {
            keyX = -1;
            keyY = -1;
        } else if (keyX >= MapProtocol.MAP_WIDTH || keyY >= MapProtocol.MAP_HEIGHT) {
            throw new IllegalArgumentException("Invalid emerald-key coordinate (" + keyX + "," + keyY + ")");
        }

        int[] connectsLeft = new int[nodeType.length];
        int[] connectsCenter = new int[nodeType.length];
        int[] connectsRight = new int[nodeType.length];
        for (int i = 0; i < nodeType.length; i++) {
            int connections = in.readBits(MapProtocol.CONNECTIVITY_BITS);
            connectsLeft[i] = (connections >>> MapProtocol.CONNECTS_LEFT_BIT) & 1;
            connectsCenter[i] = (connections >>> MapProtocol.CONNECTS_CENTER_BIT) & 1;
            connectsRight[i] = (connections >>> MapProtocol.CONNECTS_RIGHT_BIT) & 1;
        }
        return new PacketRecord.DungeonMap(isAct4, keyX, keyY, nodeType,
            connectsLeft, connectsCenter, connectsRight);
    }

    private static CurrentMapSelectionSnapshot readCurrentNode(BitReader in) {
        int x = in.readBits(CurrentMapSelectionProtocol.COORDINATE_BITS);
        int y = in.readBits(CurrentMapSelectionProtocol.COORDINATE_BITS);
        // The vanilla pre-map node is (0, -1), while the off-grid boss node is (-1, 15).
        if (x == NO_COLUMN_WIRE_VALUE) {
            x = -1;
        } else if (y == CurrentMapSelectionProtocol.NO_PREVIOUS_ROW_WIRE_VALUE) {
            y = -1;
        }
        return new CurrentMapSelectionSnapshot(x, y);
    }
}
