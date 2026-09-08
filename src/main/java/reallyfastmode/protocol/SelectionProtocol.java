package reallyfastmode.protocol;

/** Fixed constants for the Selection candidates wire layout. */
public final class SelectionProtocol {
    public static final int CANDIDATES_CARD_COUNT_BITS = 8;
    public static final int CANDIDATES_CARD_WIRE_ID_BITS = CardProtocol.CARD_WIRE_ID_BITS;

    public static final int MAX_CANDIDATES =
        (1 << CANDIDATES_CARD_COUNT_BITS) - 1;

    private SelectionProtocol() {
    }
}
