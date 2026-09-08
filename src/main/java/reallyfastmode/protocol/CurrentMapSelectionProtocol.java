package reallyfastmode.protocol;

/** Fixed constants for the current map-selection wire layout. */
public final class CurrentMapSelectionProtocol {
    public static final int COORDINATE_BITS = 4;
    public static final int CURRENT_NODE_BITS = COORDINATE_BITS * 2;
    public static final int NO_PREVIOUS_ROW_WIRE_VALUE =
        (1 << COORDINATE_BITS) - 1;

    private CurrentMapSelectionProtocol() {
    }
}
