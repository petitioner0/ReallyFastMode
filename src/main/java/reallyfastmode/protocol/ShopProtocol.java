package reallyfastmode.protocol;

/** Fixed constants for the Shop wire layout. */
public final class ShopProtocol {
    public static final int CARDS_COUNT_BITS = 3;
    public static final int CARD_WIRE_ID_BITS = CardProtocol.CARD_WIRE_ID_BITS;
    public static final int RELIC_COUNT_BITS = 2;
    public static final int RELIC_WIRE_ID_BITS = PlayerProtocol.RELIC_WIRE_ID_BITS;
    public static final int POTION_COUNT_BITS = 2;
    public static final int POTION_WIRE_ID_BITS = PotionProtocol.POTION_WIRE_ID_BITS;
    public static final int PRICE_BITS = 9;
    public static final int PURGE_AVAILABLE_BITS = 1;
    public static final int ACTUAL_PURGE_COST_BITS = 4;
    public static final int PURGE_COST_UNIT = 25;

    public static final int MAX_CARDS = (1 << CARDS_COUNT_BITS) - 1;
    public static final int MAX_RELICS = (1 << RELIC_COUNT_BITS) - 1;
    public static final int MAX_POTIONS = (1 << POTION_COUNT_BITS) - 1;
    public static final int MAX_PRICE = (1 << PRICE_BITS) - 1;
    public static final int MAX_ACTUAL_PURGE_COST_UNITS =
        (1 << ACTUAL_PURGE_COST_BITS) - 1;
    public static final int MAX_ACTUAL_PURGE_COST =
        MAX_ACTUAL_PURGE_COST_UNITS * PURGE_COST_UNIT
            + (PURGE_COST_UNIT - 1);

    private ShopProtocol() {
    }
}
