package reallyfastmode.protocol;

import reallyfastmode.protocol.VanillaCatalog.VanillaEventCatalog;

/** Fixed constants for the Event wire layout. */
public final class EventProtocol {
    public static final int EVENT_WIRE_ID_BITS = 6;
    public static final int OPTIONS_ENTRY_COUNT_BITS = 3;
    public static final int OPTIONS_APPLICABILITY_BITS = 1;

    public static final int MAX_EVENT_WIRE_ID = (1 << EVENT_WIRE_ID_BITS) - 1;
    public static final int MAX_OPTIONS = (1 << OPTIONS_ENTRY_COUNT_BITS) - 1;
    public static final int UNKNOWN_EVENT_WIRE_ID = VanillaEventCatalog.UNKNOWN.wireId;
    public static final int FIXED_BITS =
        EVENT_WIRE_ID_BITS + OPTIONS_ENTRY_COUNT_BITS;

    private EventProtocol() {
    }
}
