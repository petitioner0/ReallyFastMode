package reallyfastmode.protocol;

import reallyfastmode.protocol.VanillaCatalog.VanillaRestOptionCatalog;

/** Campfire option IDs and Rest block layout. */
public final class RestProtocol {
    public static final int OPTION_WIRE_ID_BITS = 3;
    public static final int MAX_OPTION_WIRE_ID =
        (1 << OPTION_WIRE_ID_BITS) - 1;
    public static final int UNKNOWN_OPTION_WIRE_ID = VanillaRestOptionCatalog.UNKNOWN.wireId;
    public static final int AVAILABLE_OPTIONS_MASK_BITS = 8;

    private RestProtocol() {
    }
}
