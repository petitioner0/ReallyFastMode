package reallyfastmode.protocol.build;

/** Semantic Structure-of-Arrays snapshot for a list of rewards. */
public final class RewardColumns {
    public final int rewardCount;
    public final int[] typeWireId;
    public final int[] relicWireId;
    public final int[] potionWireId;
    public final int[] golds;

    public RewardColumns(
        int rewardCount,
        int relicCount,
        int potionCount,
        int goldCount
    ) {
        if (rewardCount < 0) {
            throw new IllegalArgumentException("rewardCount=" + rewardCount);
        }
        if (relicCount < 0) {
            throw new IllegalArgumentException("relicCount=" + relicCount);
        }
        if (potionCount < 0) {
            throw new IllegalArgumentException("potionCount=" + potionCount);
        }
        if (goldCount < 0) {
            throw new IllegalArgumentException("goldCount=" + goldCount);
        }

        this.rewardCount = rewardCount;
        this.typeWireId = new int[rewardCount];
        this.relicWireId = new int[relicCount];
        this.potionWireId = new int[potionCount];
        this.golds = new int[goldCount];
    }
}
