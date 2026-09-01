package reallyfastmode.access;

import com.megacrit.cardcrawl.actions.GameActionManager;

/** Reads the current combat turn number. */
public final class CombatAccess {
    private CombatAccess() {
    }

    public static int turn() {
        return GameActionManager.turn;
    }
}
