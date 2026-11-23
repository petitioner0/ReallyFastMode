package reallyfastmode.protocol.VanillaCatalog;

/** Explicit three-bit wire mapping for the four vanilla act numbers. */
public enum VanillaActCatalog {
    ACT_1(1, 0, "Exordium"),
    ACT_2(2, 1, "TheCity"),
    ACT_3(3, 2, "TheBeyond"),
    ACT_4(4, 3, "TheEnding"),
    UNKNOWN(-1, 4, "Unknown");

    public final int actNum;
    public final int wireId;
    public final String actId;

    VanillaActCatalog(int actNum, int wireId, String actId) {
        this.actNum = actNum;
        this.wireId = wireId;
        this.actId = actId;
    }

    /** Maps the raw {@code AbstractDungeon.actNum}; all non-vanilla values are unknown. */
    public static int wireId(int actNum) {
        for (VanillaActCatalog act : values()) {
            if (act != UNKNOWN && act.actNum == actNum) {
                return act.wireId;
            }
        }
        return UNKNOWN.wireId;
    }
}
