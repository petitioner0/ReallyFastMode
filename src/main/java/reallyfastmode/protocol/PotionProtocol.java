package reallyfastmode.protocol;

import reallyfastmode.protocol.VanillaCatalog.VanillaPotionCatalog;

/** Fixed constants for the Potion inventory wire layout. */
public final class PotionProtocol {
    public static final int POTION_SLOTS_COUNT_BITS = 3;
    public static final int POTION_WIRE_ID_BITS = 6;

    public static final int MAX_POTION_SLOTS = (1 << POTION_SLOTS_COUNT_BITS) - 1;
    public static final int EMPTY_POTION_WIRE_ID = VanillaPotionCatalog.EMPTY.wireId;
    public static final int UNKNOWN_POTION_WIRE_ID = VanillaPotionCatalog.UNKNOWN.wireId;

    private PotionProtocol() {
    }
}
