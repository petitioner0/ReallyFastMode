package reallyfastmode.access;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
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
        if (AbstractDungeon.map == null) {
            throw new IllegalStateException("No current dungeon map is available.");
        }
        return wireIdAt(AbstractDungeon.map, x, y);
    }

    static int wireIdAt(List<? extends List<MapRoomNode>> dungeonMap, int x, int y) {
        List<MapRoomNode> row = Objects.requireNonNull(dungeonMap, "dungeonMap").get(y);
        MapRoomNode node = Objects.requireNonNull(row, "map row").get(x);
        if (node == null || node.room == null) {
            return VanillaNodeCatalog.EMPTY.wireId;
        }
        return VanillaNodeCatalog.wireId(node.room.getClass());
    }

    public static List<MapRoomNode> availableMapNodes() {
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (AbstractDungeon.screen != AbstractDungeon.CurrentScreen.MAP
            || room == null
            || room.phase != AbstractRoom.RoomPhase.COMPLETE
            || AbstractDungeon.map == null) {
            return Collections.emptyList();
        }

        MapRoomNode current = AbstractDungeon.getCurrMapNode();
        List<MapRoomNode> available = new ArrayList<MapRoomNode>();
        for (List<MapRoomNode> row : AbstractDungeon.map) {
            if (row == null) {
                continue;
            }
            for (MapRoomNode node : row) {
                if (node == null || node.room == null || node.taken) {
                    continue;
                }
                if (!AbstractDungeon.firstRoomChosen) {
                    if (node.y == 0) {
                        available.add(node);
                    }
                } else if (current != null
                    && (current.isConnectedTo(node) || current.wingedIsConnectedTo(node))) {
                    available.add(node);
                }
            }
        }
        return immutableCopy(available);
    }

    public static boolean hasEmeraldKey(MapRoomNode node) {
        return Objects.requireNonNull(node, "node").hasEmeraldKey;
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
