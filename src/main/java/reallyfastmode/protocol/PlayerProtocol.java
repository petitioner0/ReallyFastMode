package reallyfastmode.protocol;

import reallyfastmode.protocol.VanillaCatalog.VanillaOrbCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaPowerCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaRelicCatalog;

/** Fixed constants for the mixed scalar/variable-column Player wire layout. */
public final class PlayerProtocol {
    public static final int MAX_ORBS = 10;

    public static final int UNKNOWN_ORB_WIRE_ID = VanillaOrbCatalog.UNKNOWN_WIRE_ID;
    public static final int UNKNOWN_POWER_WIRE_ID = VanillaPowerCatalog.UNKNOWN_WIRE_ID;
    public static final int UNKNOWN_RELIC_WIRE_ID = VanillaRelicCatalog.values().length;

    public static final int HP_BITS = 10;
    public static final int BLOCK_BITS = 10;
    public static final int ENERGY_BITS = 8;
    public static final int GOLD_BITS = 12;
    public static final int MAX_ORBS_BITS = 4;
    public static final int ORB_WIRE_ID_BITS = 3;
    public static final int POWER_ENTRY_COUNT_BITS = 5;
    public static final int POWER_WIRE_ID_BITS = 8;
    public static final int POWER_COUNT_BITS = 12;
    public static final int RELIC_ENTRY_COUNT_BITS = 8;
    public static final int RELIC_WIRE_ID_BITS = 8;
    public static final int RELIC_COUNT_BITS = 8;

    public static final int FIXED_SCALAR_BITS = HP_BITS * 2
        + BLOCK_BITS
        + ENERGY_BITS
        + GOLD_BITS
        + MAX_ORBS_BITS;

    private PlayerProtocol() {
    }
}
