package reallyfastmode.access;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import reallyfastmode.protocol.MapProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaNodeCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Reads the current dungeon map and currently selectable nodes. */
public final class MapAccess {
    private MapAccess() {
    }

    public static List<List<MapRoomNode>> map() {
        if (AbstractDungeon.map == null || AbstractDungeon.map.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<MapRoomNode>> rows = new ArrayList<List<MapRoomNode>>(AbstractDungeon.map.size());
        for (List<MapRoomNode> row : AbstractDungeon.map) {
            rows.add(immutableCopy(row));
        }
        return Collections.unmodifiableList(rows);
    }

    public static MapRoomNode currentNode() {
        return AbstractDungeon.getCurrMapNode();
    }

    /** Returns the current map coordinate's vanilla room-type wire id. */
    public static int wireId(int x, int y) {
        return wireId(nodeAt(x, y));
    }

    static int wireIdAt(List<? extends List<MapRoomNode>> dungeonMap, int x, int y) {
        return wireId(nodeAt(dungeonMap, x, y));
    }

    private static int wireId(MapRoomNode node) {
        if (node == null || !node.hasEdges()) {
            return VanillaNodeCatalog.EMPTY.wireId;
        }
        if (node.room == null) {
            return VanillaNodeCatalog.UNKNOWN.wireId;
        }
        return VanillaNodeCatalog.wireId(node.room.getClass());
    }

    public static List<MapRoomNode> availableMapNodes() {
        MapRoomNode current = AbstractDungeon.getCurrMapNode();
        if (!mapSelectionActive(current)) {
            return Collections.emptyList();
        }

        List<MapRoomNode> available = new ArrayList<MapRoomNode>();
        for (List<MapRoomNode> row : AbstractDungeon.map) {
            if (row == null) {
                continue;
            }
            for (MapRoomNode node : row) {
                if (node == null || !node.hasEdges() || node.room == null || node.taken) {
                    continue;
                }
                if (!AbstractDungeon.firstRoomChosen) {
                    if (node.y == 0) {
                        available.add(node);
                    }
                } else if (current.isConnectedTo(node) || current.wingedIsConnectedTo(node)) {
                    available.add(node);
                }
            }
        }
        return immutableCopy(available);
    }

    public static boolean hasEmeraldKey(int x, int y) {
        MapRoomNode node = nodeAt(x, y);
        return node != null && node.hasEdges() && node.hasEmeraldKey;
    }

    static boolean hasEmeraldKeyAt(
        List<? extends List<MapRoomNode>> dungeonMap,
        int x,
        int y
    ) {
        MapRoomNode node = nodeAt(dungeonMap, x, y);
        return node != null && node.hasEdges() && node.hasEmeraldKey;
    }

    /** Returns the node's left/center/right outgoing connections as bits 0/1/2. */
    public static int connectivity(int x, int y) {
        if (AbstractDungeon.map == null) {
            throw new IllegalStateException("No current dungeon map is available.");
        }
        return connectivityAt(AbstractDungeon.map, x, y);
    }

    static int connectivityAt(
        List<? extends List<MapRoomNode>> dungeonMap,
        int x,
        int y
    ) {
        MapRoomNode node = nodeAt(dungeonMap, x, y);
        if (node == null
            || !node.hasEdges()
            || y >= MapProtocol.MAP_HEIGHT - 1) {
            return 0;
        }

        int connectivity = 0;
        if (x > 0 && connectsTo(node, nodeAt(dungeonMap, x - 1, y + 1))) {
            connectivity |= 1 << MapProtocol.CONNECTS_LEFT_BIT;
        }
        if (connectsTo(node, nodeAt(dungeonMap, x, y + 1))) {
            connectivity |= 1 << MapProtocol.CONNECTS_CENTER_BIT;
        }
        if (x < MapProtocol.MAP_WIDTH - 1
            && connectsTo(node, nodeAt(dungeonMap, x + 1, y + 1))) {
            connectivity |= 1 << MapProtocol.CONNECTS_RIGHT_BIT;
        }
        return connectivity;
    }

    private static boolean connectsTo(MapRoomNode source, MapRoomNode destination) {
        return destination != null && source.isConnectedTo(destination);
    }

    private static MapRoomNode nodeAt(int x, int y) {
        if (AbstractDungeon.map == null) {
            throw new IllegalStateException("No current dungeon map is available.");
        }
        return nodeAt(AbstractDungeon.map, x, y);
    }

    private static MapRoomNode nodeAt(
        List<? extends List<MapRoomNode>> dungeonMap,
        int x,
        int y
    ) {
        List<MapRoomNode> row = Objects.requireNonNull(dungeonMap, "dungeonMap").get(y);
        return Objects.requireNonNull(row, "map row").get(x);
    }

    private static boolean mapSelectionActive(MapRoomNode current) {
        AbstractRoom room = current == null ? null : current.room;
        return AbstractDungeon.screen == AbstractDungeon.CurrentScreen.MAP
            && room != null
            && room.phase == AbstractRoom.RoomPhase.COMPLETE
            && AbstractDungeon.map != null;
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
