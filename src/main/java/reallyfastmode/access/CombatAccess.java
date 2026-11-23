package reallyfastmode.access;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

/** Reads the current combat turn and action state. */
public final class CombatAccess {
    private CombatAccess() {
    }

    public static int turn() {
        return GameActionManager.turn;
    }

    public static boolean isPlayerTurnIdle() {
        GameActionManager manager = AbstractDungeon.actionManager;
        AbstractPlayer player = AbstractDungeon.player;
        return manager != null && player != null
            && manager.phase == GameActionManager.Phase.WAITING_ON_USER
            && manager.currentAction == null
            && manager.actions.isEmpty()
            && manager.preTurnActions.isEmpty()
            && manager.cardQueue.isEmpty()
            && !manager.turnHasEnded
            && !player.isEndingTurn
            && !player.endTurnQueued;
    }
}
