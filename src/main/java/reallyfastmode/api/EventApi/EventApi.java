package reallyfastmode.api.EventApi;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import reallyfastmode.access.EventAccess;
import reallyfastmode.patches.access.PrivateFieldAccess;

import java.util.List;

/** Semantic event commands. Call on the game render thread. */
public final class EventApi {
    private EventApi() {
    }

    /**
     * Selects an event option, numbered from bottom to top starting at zero.
     * Disabled options still occupy an index. The event consumes the choice
     * through its normal update loop.
     *
     * @throws IllegalArgumentException if the index is out of range or the option is disabled
     * @throws IllegalStateException if no event dialog is accepting input
     */
    public static void selectOption(int optionIndex) {
        if (optionIndex < 0) {
            throw new IllegalArgumentException("Event option index must be nonnegative.");
        }
        AbstractEvent event = requireActiveEvent();
        if (event.hasDialog) {
            if (event.roomEventText == null
                || !PrivateFieldAccess.roomEventDialogVisible(event.roomEventText)) {
                throw new IllegalStateException("The room event dialog is not visible.");
            }
        } else if (event.imageEventText == null
            || !PrivateFieldAccess.imageEventDialogReady(event.imageEventText)) {
            throw new IllegalStateException("The image event dialog is not ready for input.");
        }

        submitSelection(optionIndex, event.hasDialog, EventAccess.options());
    }

    private static AbstractEvent requireActiveEvent() {
        if (CardCrawlGame.mode != CardCrawlGame.GameMode.GAMEPLAY
            || AbstractDungeon.currMapNode == null
            || AbstractDungeon.getCurrRoom() == null
            || AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.EVENT
            || AbstractDungeon.getCurrRoom().event == null
            || AbstractDungeon.player == null
            || AbstractDungeon.player.isDead
            || AbstractDungeon.player.isDying
            || AbstractDungeon.isScreenUp
            || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.NONE
            || AbstractDungeon.isFadingIn
            || AbstractDungeon.isFadingOut) {
            throw new IllegalStateException("No event dialog is currently accepting a selection.");
        }
        return AbstractDungeon.getCurrRoom().event;
    }

    static void submitSelection(
        int optionIndex, boolean roomDialog, List<LargeDialogOptionButton> options
    ) {
        if (!(roomDialog ? RoomEventDialog.waitForInput : GenericEventDialog.waitForInput)) {
            throw new IllegalStateException("An event option selection is already pending.");
        }
        if (optionIndex < 0 || optionIndex >= options.size()) {
            throw new IllegalArgumentException("Event option index is out of range: " + optionIndex);
        }
        // LargeDialogOptionButton.calculateY places higher slots lower on screen.
        // Keep disabled entries so they retain their bottom-to-top API indices.
        int vanillaIndex = options.size() - 1 - optionIndex;
        if (!EventAccess.isSelectable(options.get(vanillaIndex))) {
            throw new IllegalArgumentException("Event option is currently disabled: " + optionIndex);
        }

        // Validation has finished. Consume old UI input before submitting the choice.
        for (LargeDialogOptionButton option : options) {
            if (option != null) {
                option.pressed = false;
                option.hb.clicked = false;
                option.hb.clickStarted = false;
            }
        }
        // Exact commit point in RoomEventDialog.update / GenericEventDialog.update.
        // Store the list index, which is what vanilla sends to the event handler.
        if (roomDialog) {
            RoomEventDialog.selectedOption = vanillaIndex;
            RoomEventDialog.waitForInput = false;
        } else {
            GenericEventDialog.selectedOption = vanillaIndex;
            GenericEventDialog.waitForInput = false;
        }
    }
}
