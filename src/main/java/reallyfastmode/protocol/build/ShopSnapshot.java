package reallyfastmode.protocol.build;

/** Semantic Structure-of-Arrays snapshot of the shop's remaining inventory. */
public final class ShopSnapshot {
    public final int cardsCount;
    public final int[] cardsWireId;
    public final int[] cardsPrice;
    public final boolean purgeAvailable;
    /** The actual purge price after division by the protocol's 25-gold unit. */
    public final int actualPurgeCost;
    public final int relicCount;
    public final int[] relicWireId;
    public final int[] relicPrice;
    public final int potionCount;
    public final int[] potionWireId;
    public final int[] potionPrice;

    public ShopSnapshot(
        int cardsCount,
        boolean purgeAvailable,
        int actualPurgeCost,
        int relicCount,
        int potionCount
    ) {
        requireNonNegative("cardsCount", cardsCount);
        requireNonNegative("actualPurgeCost", actualPurgeCost);
        requireNonNegative("relicCount", relicCount);
        requireNonNegative("potionCount", potionCount);

        this.cardsCount = cardsCount;
        this.cardsWireId = new int[cardsCount];
        this.cardsPrice = new int[cardsCount];
        this.purgeAvailable = purgeAvailable;
        this.actualPurgeCost = actualPurgeCost;
        this.relicCount = relicCount;
        this.relicWireId = new int[relicCount];
        this.relicPrice = new int[relicCount];
        this.potionCount = potionCount;
        this.potionWireId = new int[potionCount];
        this.potionPrice = new int[potionCount];
    }

    private static void requireNonNegative(String name, int value) {
        if (value < 0) {
            throw new IllegalArgumentException(name + "=" + value);
        }
    }
}
