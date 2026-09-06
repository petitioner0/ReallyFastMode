package reallyfastmode.protocol;

/** Fixed constants for the scalar Game wire layout. */
public final class GameProtocol {
    public static final int ACT_BITS = 3;
    public static final int FLOOR_BITS = 6;
    public static final int ASCENSION_LEVEL_BITS = 5;
    public static final int KEY_BITS = 1;

    public static final int MAX_FLOOR = (1 << FLOOR_BITS) - 1;
    public static final int MAX_ASCENSION_LEVEL = (1 << ASCENSION_LEVEL_BITS) - 1;
    public static final int MAX_ACT_WIRE_ID = VanillaActCatalog.UNKNOWN.wireId;

    public static final int GAME_BLOCK_BITS = ACT_BITS
        + FLOOR_BITS
        + ASCENSION_LEVEL_BITS
        + KEY_BITS * 3;

    private GameProtocol() {
    }
}
