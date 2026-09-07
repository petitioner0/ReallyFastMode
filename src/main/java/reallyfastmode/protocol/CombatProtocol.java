package reallyfastmode.protocol;

/** Fixed constants for the scalar Combat wire layout. */
public final class CombatProtocol {
    public static final int TURN_BITS = 8;
    public static final int MAX_TURN = (1 << TURN_BITS) - 1;
    public static final int COMBAT_BLOCK_BITS = TURN_BITS;

    private CombatProtocol() {
    }
}
