package reallyfastmode.api.ProceedApi;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.buttons.ProceedButton;
import reallyfastmode.api.GameCommandResult;
import reallyfastmode.patches.access.PrivateFieldAccess;

/** Submits a vanilla Proceed action. Call on the game render thread. */
public final class ProceedApi {
    private static ProceedRequest pending;

    private ProceedApi() {
    }

    /**
     * Requests one Proceed action for the current room and screen. Vanilla
     * handles the request on the next button update if that context is still active.
     */
    public static GameCommandResult proceed() {
        return GameCommandResult.execute("proceed", ProceedApi::queueProceed);
    }

    private static void queueProceed() {
        if (!canProceed()) {
            throw new IllegalStateException("The current screen is not accepting Proceed.");
        }
        ProceedButton button = AbstractDungeon.overlayMenu.proceedButton;
        if (pending != null && pending.matches(button)) {
            throw new IllegalStateException("A Proceed action is already pending.");
        }
        if (!PrivateFieldAccess.proceedButtonAcceptsClick(button)) {
            throw new IllegalStateException("The Proceed button is hidden or already clicked.");
        }
        pending = new ProceedRequest(button);
    }

    private static boolean canProceed() {
        if (CardCrawlGame.mode != CardCrawlGame.GameMode.GAMEPLAY
            || CardCrawlGame.isPopupOpen
            || AbstractDungeon.currMapNode == null
            || AbstractDungeon.currMapNode.room == null
            || AbstractDungeon.player == null
            || AbstractDungeon.player.isDead
            || AbstractDungeon.player.isDying
            || AbstractDungeon.overlayMenu == null
            || AbstractDungeon.overlayMenu.proceedButton == null
            || AbstractDungeon.isFadingIn
            || AbstractDungeon.isFadingOut
            || AbstractDungeon.isDungeonBeaten) {
            return false;
        }
        if (AbstractDungeon.screen == CurrentScreen.COMBAT_REWARD) {
            return AbstractDungeon.isScreenUp && AbstractDungeon.combatRewardScreen != null;
        }
        return AbstractDungeon.screen == CurrentScreen.NONE
            && !AbstractDungeon.isScreenUp
            && AbstractDungeon.currMapNode.room.phase == AbstractRoom.RoomPhase.COMPLETE;
    }

    private static final class ProceedRequest {
        private final ProceedButton button;
        private final MapRoomNode node;
        private final CurrentScreen screen;

        private ProceedRequest(ProceedButton button) {
            this.button = button;
            this.node = AbstractDungeon.currMapNode;
            this.screen = AbstractDungeon.screen;
        }

        private boolean matches(ProceedButton currentButton) {
            return button == currentButton
                && node == AbstractDungeon.currMapNode
                && screen == AbstractDungeon.screen;
        }
    }

    @SpirePatch2(clz = ProceedButton.class, method = "update")
    public static class CommitProceedPatch {
        @SpirePrefixPatch
        public static void commit(ProceedButton __instance, Hitbox ___hb) {
            ProceedRequest request = pending;
            if (request == null) {
                return;
            }
            pending = null;
            // A reward claim or another screen change can precede this update.
            if (request.matches(__instance) && canProceed()
                && PrivateFieldAccess.proceedButtonAcceptsClick(__instance)) {
                ___hb.clicked = true;
            }
        }
    }
}
