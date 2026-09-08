package reallyfastmode.protocol.build;

import reallyfastmode.protocol.MapProtocol;

/** Semantic snapshot of the fixed vanilla dungeon map. */
public final class MapSnapshot {
    public final boolean isAct4;
    public final int[] nodeWireId;
    public final int emeraldKeyX;
    public final int emeraldKeyY;
    public final int[] connectivity;

    public MapSnapshot(boolean isAct4, int emeraldKeyX, int emeraldKeyY) {
        this.isAct4 = isAct4;
        this.emeraldKeyX = emeraldKeyX;
        this.emeraldKeyY = emeraldKeyY;
        int nodeCount = isAct4 ? 0 : MapProtocol.NODE_COUNT;
        this.nodeWireId = new int[nodeCount];
        this.connectivity = new int[nodeCount];
    }
}
