package reallyfastmode.protocol.read;

import reallyfastmode.protocol.MapProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaNodeCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Directed graph of the dungeon's fixed map routes. */
public final class MapGraph {
    private final Node[][] rooms;
    private final Node offGridBoss;
    private final List<Node> nodes;
    private final List<Node> entryNodes;

    private MapGraph(Node[][] rooms, Node offGridBoss) {
        this.rooms = rooms;
        this.offGridBoss = offGridBoss;
        List<Node> all = new ArrayList<>();
        for (Node[] row : rooms) {
            for (Node node : row) {
                if (node != null) {
                    all.add(node);
                }
            }
        }
        if (offGridBoss != null) {
            all.add(offGridBoss);
        }
        this.nodes = Collections.unmodifiableList(all);

        List<Node> entries = new ArrayList<>();
        for (Node node : rooms[0]) {
            if (node != null) {
                entries.add(node);
            }
        }
        this.entryNodes = Collections.unmodifiableList(entries);
    }

    public List<Node> nodes() {
        return nodes;
    }

    public List<Node> entryNodes() {
        return entryNodes;
    }

    /** Returns the room at the given coordinate, or null when the coordinate has no room. */
    public Node nodeAt(int x, int y) {
        if (offGridBoss != null && x == offGridBoss.x && y == offGridBoss.y) {
            return offGridBoss;
        }
        if (x < 0 || x >= MapProtocol.MAP_WIDTH || y < 0 || y >= rooms.length) {
            return null;
        }
        return rooms[y][x];
    }

    static MapGraph fromMap(PacketRecord.DungeonMap map) {
        if (map.isAct4 == 1) {
            return act4();
        }

        int width = MapProtocol.MAP_WIDTH;
        int height = MapProtocol.MAP_HEIGHT;
        int[][] connections = {map.connectsLeft, map.connectsCenter, map.connectsRight};
        boolean[] occupied = occupiedCells(map, connections);
        Node[][] rooms = new Node[height][width];
        Node boss = null;
        // Every edge goes up one row, so each destination is built before its source.
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (!occupied[index]) {
                    continue;
                }

                List<Node> next;
                if (y == height - 1) {
                    if (boss == null) {
                        boss = new Node(-1, height, "BOSS", 0, Collections.emptyList());
                    }
                    next = Collections.singletonList(boss);
                } else {
                    next = new ArrayList<>(connections.length);
                    for (int direction = 0; direction < connections.length; direction++) {
                        if (connections[direction][index] != 0) {
                            next.add(rooms[y + 1][x + direction - 1]);
                        }
                    }
                }
                int hasEmeraldKey = x == map.emeraldKeyX && y == map.emeraldKeyY ? 1 : 0;
                rooms[y][x] = new Node(x, y, map.nodeType[index], hasEmeraldKey, next);
            }
        }
        return new MapGraph(rooms, boss);
    }

    private static boolean[] occupiedCells(PacketRecord.DungeonMap map, int[][] connections) {
        int width = MapProtocol.MAP_WIDTH;
        int height = MapProtocol.MAP_HEIGHT;
        String emptyType = VanillaNodeCatalog.EMPTY.name();
        boolean[] occupied = new boolean[map.nodeType.length];
        for (int index = 0; index < occupied.length; index++) {
            int x = index % width;
            int y = index / width;
            if (!emptyType.equals(map.nodeType[index]) || (x == map.emeraldKeyX && y == map.emeraldKeyY)) {
                occupied[index] = true;
            }
            for (int direction = 0; direction < connections.length; direction++) {
                if (connections[direction][index] == 0) {
                    continue;
                }
                int nextX = x + direction - 1;
                int nextY = y + 1;
                if (nextX < 0 || nextX >= width || nextY >= height) {
                    throw new IllegalArgumentException(
                        "Map edge from (" + x + "," + y + ") leaves the grid at (" + nextX + "," + nextY + ")"
                    );
                }
                occupied[index] = true;
                occupied[nextY * width + nextX] = true;
            }
        }
        return occupied;
    }

    private static MapGraph act4() {
        int x = 3;
        Node[][] rooms = new Node[5][MapProtocol.MAP_WIDTH];
        rooms[4][x] = new Node(x, 4, "VICTORY", 0, Collections.emptyList());
        Node next = new Node(x, 3, "BOSS", 0, Collections.emptyList());
        rooms[3][x] = next;
        String[] types = {VanillaNodeCatalog.REST.name(), VanillaNodeCatalog.SHOP.name(), VanillaNodeCatalog.ELITE.name()};
        for (int y = types.length - 1; y >= 0; y--) {
            next = new Node(x, y, types[y], 0, Collections.singletonList(next));
            rooms[y][x] = next;
        }
        return new MapGraph(rooms, null);
    }

    public static final class Node {
        public final int x;
        public final int y;
        public final String roomType;
        public final int hasEmeraldKey;
        private final List<Node> successors;

        private Node(int x, int y, String roomType, int hasEmeraldKey, List<Node> successors) {
            this.x = x;
            this.y = y;
            this.roomType = roomType;
            this.hasEmeraldKey = hasEmeraldKey;
            this.successors = Collections.unmodifiableList(successors);
        }

        public List<Node> successors() {
            return successors;
        }
    }
}
