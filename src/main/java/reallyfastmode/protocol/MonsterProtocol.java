package reallyfastmode.protocol;

/** Fixed constants for the first Monster SoA wire layout. */
public final class MonsterProtocol {
    public static final int MAX_MONSTERS = 16;
    public static final int MAX_POWERS_PER_MONSTER = 31;

    /** First value after the fixed vanilla monster catalog range 0-65. */
    public static final int UNKNOWN_MONSTER_WIRE_ID = 66;

    /** First value after the fixed vanilla power catalog range 0-158. */
    public static final int UNKNOWN_POWER_WIRE_ID = 159;

    /** First value after the 17 vanilla Intent enum values. */
    public static final int UNKNOWN_INTENT_WIRE_ID = 17;

    public static final int MONSTER_COUNT_BITS = 5;
    public static final int INSTANCE_ID_BITS = 4;
    public static final int MONSTER_WIRE_ID_BITS = 8;
    public static final int HP_BITS = 12;
    public static final int BLOCK_BITS = 16;
    public static final int POWER_ENTRY_COUNT_BITS = 5;
    public static final int POWER_WIRE_ID_BITS = 12;
    public static final int POWER_AMOUNT_BITS = 12;
    public static final int INTENT_WIRE_ID_BITS = 5;
    public static final int INTENT_DAMAGE_BITS = 8;
    public static final int INTENT_MULTI_AMOUNT_BITS = 8;

    private MonsterProtocol() {
    }
}
