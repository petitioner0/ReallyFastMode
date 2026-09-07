package reallyfastmode.protocol.build;

/** Semantic snapshot for one player with scalar values and variable-length columns. */
public final class PlayerSnapshot {
    public final int hp;
    public final int maxHp;
    public final int block;
    public final int energy;
    public final int gold;
    public final int maxOrbs;

    public final int[] orbWireId;
    public final int[] powerWireId;
    public final int[] powerCounts;
    public final int[] relicWireId;
    public final int[] relicCounts;

    public PlayerSnapshot(
        int hp,
        int maxHp,
        int block,
        int energy,
        int gold,
        int maxOrbs,
        int powerSize,
        int relicSize
    ) {
        if (maxOrbs < 0) {
            throw new IllegalArgumentException("maxOrbs=" + maxOrbs);
        }
        if (powerSize < 0) {
            throw new IllegalArgumentException("powerSize=" + powerSize);
        }
        if (relicSize < 0) {
            throw new IllegalArgumentException("relicSize=" + relicSize);
        }

        this.hp = hp;
        this.maxHp = maxHp;
        this.block = block;
        this.energy = energy;
        this.gold = gold;
        this.maxOrbs = maxOrbs;
        this.orbWireId = new int[maxOrbs];
        this.powerWireId = new int[powerSize];
        this.powerCounts = new int[powerSize];
        this.relicWireId = new int[relicSize];
        this.relicCounts = new int[relicSize];
    }
}
