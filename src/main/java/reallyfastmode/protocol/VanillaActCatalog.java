package reallyfastmode.protocol;

/** Explicit three-bit wire mapping for the four vanilla act numbers. */
public enum VanillaActCatalog {
    ACT_1(1, 0),
    ACT_2(2, 1),
    ACT_3(3, 2),
    ACT_4(4, 3),
    UNKNOWN(-1, 4);

    public final int actNum;
    public final int wireId;

    VanillaActCatalog(int actNum, int wireId) {
        this.actNum = actNum;
        this.wireId = wireId;
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
