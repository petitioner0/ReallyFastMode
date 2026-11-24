package reallyfastmode.patches.protocol;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.access.CombatAccess;
import reallyfastmode.protocol.io.StatePacketSender;
import reallyfastmode.protocol.write.CombatPacketPacker;

/** Publishes battle snapshots at player turn start and after combat commands settle. */
public final class CombatPacketPatches {
    private static final Logger LOGGER = LogManager.getLogger(CombatPacketPatches.class.getName());

    private static AbstractRoom trackedRoom;
    private static int publishedTurn = -1;
    private static boolean settlementPending;
    private static CurrentScreen publishedSelection;

    private CombatPacketPatches() {
    }

    /** Records a successful play, potion use or combat card selection on the render thread. */
    public static void awaitSettlement() {
        trackRoom(AbstractDungeon.getCurrRoom());
        settlementPending = true;
        publishedSelection = null;
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "update")
    public static class CaptureAfterUpdatePatch {
        @SpirePostfixPatch
        public static void capture() {
            AbstractRoom room = AbstractDungeon.currMapNode == null ? null : AbstractDungeon.getCurrRoom();
            AbstractPlayer player = AbstractDungeon.player;
            if (room == null || room.phase != AbstractRoom.RoomPhase.COMBAT
                || room.monsters == null || room.isBattleEnding()
                || player == null || player.isDead || player.isDying || player.isEscaping) {
                trackRoom(null);
                return;
            }
            trackRoom(room);
            int turn = CombatAccess.turn();
            boolean turnStartPending = turn != publishedTurn;
            if ((!turnStartPending && !settlementPending)
                || AbstractDungeon.isFadingIn || AbstractDungeon.isFadingOut
                || CardCrawlGame.isPopupOpen || PeekButton.isPeeking) {
                return;
            }

            CurrentScreen screen = AbstractDungeon.screen;
            if (AbstractDungeon.isScreenUp && (screen == CurrentScreen.HAND_SELECT
                || screen == CurrentScreen.GRID || screen == CurrentScreen.CARD_REWARD)) {
                if (publishedSelection != screen) {
                    publishedSelection = screen;
                    publish(true);
                }
                return;
            }

            publishedSelection = null;
            if (!AbstractDungeon.isScreenUp && screen == CurrentScreen.NONE
                && (!turnStartPending || isTurnStartReady())
                && CombatAccess.isPlayerTurnIdle() && !AbstractDungeon.actionManager.usingCard
                && AbstractDungeon.actionManager.monsterQueue.isEmpty() && !StatePacketEffects.hasPending()) {
                publishedTurn = turn;
                settlementPending = false;
                publish(false);
            }
        }
    }

    private static void trackRoom(AbstractRoom room) {
        if (room != trackedRoom) {
            trackedRoom = room;
            publishedTurn = -1;
            settlementPending = false;
            publishedSelection = null;
        }
    }

    private static boolean isTurnStartReady() {
        // The first turn number is set before the opening draw and the energy-gain callbacks.
        return !AbstractDungeon.turnPhaseEffectActive && AbstractDungeon.overlayMenu != null
            && AbstractDungeon.overlayMenu.endTurnButton != null && AbstractDungeon.overlayMenu.endTurnButton.enabled;
    }

    private static void publish(boolean awaitingSelection) {
        try {
            StatePacketSender.send(CombatPacketPacker.capture(awaitingSelection));
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to capture the combat state packet.", exception);
        }
    }
}
