package reallyfastmode.protocol;

/** Fixed constants for the Card SoA wire layouts. */
public final class CardProtocol {
    public static final int MAX_CARDS = 255;

    /** First value after the fixed vanilla card catalog range 0-431. */
    public static final int UNKNOWN_CARD_WIRE_ID = VanillaCardCatalog.UNKNOWN_WIRE_ID;

    public static final int CARD_COUNT_BITS = 8;
    public static final int CARD_WIRE_ID_BITS = 9;
    public static final int COST_BITS = 4;

    private CardProtocol() {
    }
}
