package reallyfastmode.protocol;

/** Fixed constants for the full dungeon-map wire layout. */
public final class MapProtocol {
    public static final int IS_ACT_4_BITS = 1;
    public static final int MAP_WIDTH = 7;
    public static final int MAP_HEIGHT = 15;
    public static final int NODE_COUNT = MAP_WIDTH * MAP_HEIGHT;
    public static final int NODE_WIRE_ID_BITS = 4;
    public static final int EMERALD_KEY_COORDINATE_BITS = 4;
    public static final int EMERALD_KEY_NODE_BITS = EMERALD_KEY_COORDINATE_BITS * 2;
    public static final int NO_EMERALD_KEY_COORDINATE =
        (1 << EMERALD_KEY_COORDINATE_BITS) - 1;
    public static final int CONNECTIVITY_BITS = 3;
    public static final int CONNECTS_LEFT_BIT = 0;
    public static final int CONNECTS_CENTER_BIT = 1;
    public static final int CONNECTS_RIGHT_BIT = 2;

    private MapProtocol() {
    }
}
