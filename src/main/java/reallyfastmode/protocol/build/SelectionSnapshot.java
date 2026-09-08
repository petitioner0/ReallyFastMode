package reallyfastmode.protocol.build;

/** Semantic snapshot of the cards offered by the active selection screen. */
public final class SelectionSnapshot {
    public final int candidatesCardCount;
    public final int[] candidatesCardWireId;

    public SelectionSnapshot(int candidatesCardCount) {
        if (candidatesCardCount < 0) {
            throw new IllegalArgumentException(
                "candidatesCardCount=" + candidatesCardCount
            );
        }
        this.candidatesCardCount = candidatesCardCount;
        this.candidatesCardWireId = new int[candidatesCardCount];
    }
}
