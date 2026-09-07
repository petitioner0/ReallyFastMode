package reallyfastmode.protocol.build;

/** Semantic snapshot for the player's fixed potion-slot inventory. */
public final class PotionSnapshot {
    public final int potionSlotsCount;
    public final int[] potionWireId;

    public PotionSnapshot(int potionSlotsCount) {
        if (potionSlotsCount < 0) {
            throw new IllegalArgumentException("potionSlotsCount=" + potionSlotsCount);
        }
        this.potionSlotsCount = potionSlotsCount;
        this.potionWireId = new int[potionSlotsCount];
    }
}
