package reallyfastmode.protocol;

import reallyfastmode.protocol.VanillaCatalog.VanillaRestOptionCatalog;

/** Fixed constants for the Rest options wire layout. */
public final class RestProtocol {
    public static final int OPTION_WIRE_ID_BITS = 3;
    public static final int MAX_OPTION_WIRE_ID =
        (1 << OPTION_WIRE_ID_BITS) - 1;
    public static final int UNKNOWN_OPTION_WIRE_ID = VanillaRestOptionCatalog.UNKNOWN.wireId;

    private RestProtocol() {
    }
}
