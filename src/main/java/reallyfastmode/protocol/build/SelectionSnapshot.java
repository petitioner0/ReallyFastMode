package reallyfastmode.protocol.build;

/** Semantic snapshot of the cards offered by the active selection screen. */
public final class SelectionSnapshot {
    public final int candidatesCardCount;
    public final int maxSelectableCards;
    public final int[] candidatesCardWireId;
    public final boolean[] candidatesCardUpgraded;

    public SelectionSnapshot(int candidatesCardCount, int maxSelectableCards) {
        if (candidatesCardCount < 0) {
            throw new IllegalArgumentException(
                "candidatesCardCount=" + candidatesCardCount
            );
        }
        if (maxSelectableCards < 0 || maxSelectableCards > candidatesCardCount) {
            throw new IllegalArgumentException(
                "maxSelectableCards=" + maxSelectableCards + " outside 0-" + candidatesCardCount
            );
        }
        this.candidatesCardCount = candidatesCardCount;
        this.maxSelectableCards = maxSelectableCards;
        this.candidatesCardWireId = new int[candidatesCardCount];
        this.candidatesCardUpgraded = new boolean[candidatesCardCount];
    }
}
