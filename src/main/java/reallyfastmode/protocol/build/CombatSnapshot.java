package reallyfastmode.protocol.build;

/** Immutable semantic snapshot for the single scalar Combat block. */
public final class CombatSnapshot {
    public final int turn;

    public CombatSnapshot(int turn) {
        this.turn = turn;
    }
}
