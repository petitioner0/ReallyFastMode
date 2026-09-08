package reallyfastmode.protocol.build;

/** Semantic snapshot of the current map node. */
public final class CurrentMapSelectionSnapshot {
    public final int currentNodeX;
    public final int currentNodeY;

    public CurrentMapSelectionSnapshot(int currentNodeX, int currentNodeY) {
        this.currentNodeX = currentNodeX;
        this.currentNodeY = currentNodeY;
    }
}
