package reallyfastmode.access;

import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import org.junit.Test;
import reallyfastmode.protocol.MapProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaNodeCatalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MapAccessTest {
    @Test
    public void mapsUnusedCoordinatesToEmpty() {
        List<List<MapRoomNode>> map = Arrays.<List<MapRoomNode>>asList(
            Arrays.asList(new MapRoomNode(0, 0), new MapRoomNode(1, 0)),
            Arrays.asList(new MapRoomNode(0, 1), new MapRoomNode(1, 1))
        );

        assertEquals(VanillaNodeCatalog.EMPTY.wireId, MapAccess.wireIdAt(map, 1, 0));
        assertEquals(VanillaNodeCatalog.EMPTY.wireId, MapAccess.wireIdAt(map, 0, 1));

        map.get(0).get(0).addEdge(new MapEdge(0, 0, 0, 1));
        assertEquals(VanillaNodeCatalog.UNKNOWN.wireId, MapAccess.wireIdAt(map, 0, 0));
    }

    @Test
    public void readsEmeraldKeyFlagsByCoordinate() {
        MapRoomNode node = new MapRoomNode(0, 0);
        node.hasEmeraldKey = true;
        MapRoomNode placeholder = new MapRoomNode(1, 0);
        placeholder.hasEmeraldKey = true;
        node.addEdge(new MapEdge(0, 0, 0, 1));
        List<List<MapRoomNode>> map =
            Arrays.<List<MapRoomNode>>asList(Arrays.asList(node, placeholder, null));

        assertEquals(true, MapAccess.hasEmeraldKeyAt(map, 0, 0));
        assertEquals(false, MapAccess.hasEmeraldKeyAt(map, 1, 0));
        assertEquals(false, MapAccess.hasEmeraldKeyAt(map, 2, 0));
    }

    @Test
    public void encodesLeftCenterAndRightConnectionsInBitsZeroThroughTwo() {
        List<List<MapRoomNode>> map = fixedMap();
        MapRoomNode node = map.get(4).get(3);
        node.addEdge(new MapEdge(3, 4, 2, 5));
        node.addEdge(new MapEdge(3, 4, 4, 5));

        assertEquals(0b101, MapAccess.connectivityAt(map, 3, 4));

        node.addEdge(new MapEdge(3, 4, 3, 5));
        assertEquals(0b111, MapAccess.connectivityAt(map, 3, 4));
    }

    @Test
    public void connectivityIsZeroForEmptyAndTopRowNodesAndClipsXBounds() {
        List<List<MapRoomNode>> map = fixedMap();
        assertEquals(0, MapAccess.connectivityAt(map, 3, 4));

        MapRoomNode left = map.get(4).get(0);
        left.addEdge(new MapEdge(0, 4, -1, 5));
        left.addEdge(new MapEdge(0, 4, 0, 5));
        left.addEdge(new MapEdge(0, 4, 1, 5));
        assertEquals(0b110, MapAccess.connectivityAt(map, 0, 4));

        MapRoomNode right = map.get(4).get(MapProtocol.MAP_WIDTH - 1);
        right.addEdge(new MapEdge(6, 4, 5, 5));
        right.addEdge(new MapEdge(6, 4, 6, 5));
        right.addEdge(new MapEdge(6, 4, 7, 5));
        assertEquals(0b011, MapAccess.connectivityAt(map, 6, 4));

        MapRoomNode top = map.get(MapProtocol.MAP_HEIGHT - 1).get(3);
        top.addEdge(new MapEdge(3, 14, 3, 15));
        assertEquals(0, MapAccess.connectivityAt(map, 3, 14));
    }

    private static List<List<MapRoomNode>> fixedMap() {
        List<List<MapRoomNode>> map =
            new ArrayList<List<MapRoomNode>>(MapProtocol.MAP_HEIGHT);
        for (int y = 0; y < MapProtocol.MAP_HEIGHT; y++) {
            List<MapRoomNode> row =
                new ArrayList<MapRoomNode>(MapProtocol.MAP_WIDTH);
            for (int x = 0; x < MapProtocol.MAP_WIDTH; x++) {
                row.add(new MapRoomNode(x, y));
            }
            map.add(row);
        }
        return map;
    }
}
