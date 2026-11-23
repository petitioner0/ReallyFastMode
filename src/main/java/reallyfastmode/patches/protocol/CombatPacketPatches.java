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

/** Publishes battle snapshots when a combat command reaches the next input boundary. */
public final class CombatPacketPatches {
    private static final Logger LOGGER = LogManager.getLogger(CombatPacketPatches.class.getName());

    private static AbstractRoom trackedRoom;
    private static CurrentScreen publishedSelection;

    private CombatPacketPatches() {
    }

    /** Records a successful play, potion use or combat card selection on the render thread. */
    public static void awaitSettlement() {
        trackedRoom = AbstractDungeon.getCurrRoom();
        publishedSelection = null;
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "update")
    public static class CaptureAfterUpdatePatch {
        @SpirePostfixPatch
        public static void capture() {
            if (trackedRoom == null) {
                return;
            }
            AbstractRoom room = AbstractDungeon.currMapNode == null ? null : AbstractDungeon.getCurrRoom();
            AbstractPlayer player = AbstractDungeon.player;
            if (room != trackedRoom || room.phase != AbstractRoom.RoomPhase.COMBAT
                || room.monsters == null || room.isBattleEnding()
                || player == null || player.isDead || player.isDying || player.isEscaping) {
                trackedRoom = null;
                publishedSelection = null;
                return;
            }
            if (AbstractDungeon.isFadingIn || AbstractDungeon.isFadingOut
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
                && CombatAccess.isPlayerTurnIdle() && !AbstractDungeon.actionManager.usingCard
                && AbstractDungeon.actionManager.monsterQueue.isEmpty() && !StatePacketEffects.hasPending()) {
                trackedRoom = null;
                publish(false);
            }
        }
    }

    private static void publish(boolean awaitingSelection) {
        try {
            StatePacketSender.send(CombatPacketPacker.capture(awaitingSelection));
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to capture the combat state packet.", exception);
        }
    }
}
