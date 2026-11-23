package reallyfastmode.patches.protocol;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.access.EventAccess;
import reallyfastmode.patches.access.PrivateFieldAccess;
import reallyfastmode.protocol.build.EventSnapshot;
import reallyfastmode.protocol.build.EventSnapshotBuilder;
import reallyfastmode.protocol.io.StatePacketSender;
import reallyfastmode.protocol.write.EventPacketPacker;

import java.util.List;
import java.util.Objects;

/** Publishes event state when the next set of choices is available. */
public final class EventPacketPatches {
    private static final Logger LOGGER = LogManager.getLogger(EventPacketPatches.class.getName());
    private static AbstractEvent publishedEvent;
    private static AbstractEvent pendingChoice;
    private static CurrentScreen publishedSelection;
    private static DialogOptions publishedOptions;

    private EventPacketPatches() {
    }

    @SpirePatch2(clz = RoomEventDialog.class, method = "getSelectedOption")
    public static class TrackRoomDialogChoicePatch {
        @SpirePrefixPatch
        public static void track(RoomEventDialog __instance) {
            if (!RoomEventDialog.waitForInput) {
                AbstractEvent event = currentEvent();
                if (event != null && event.roomEventText == __instance) {
                    pendingChoice = event;
                }
            }
        }
    }

    @SpirePatch2(clz = GenericEventDialog.class, method = "getSelectedOption")
    public static class TrackImageDialogChoicePatch {
        @SpirePrefixPatch
        public static void track() {
            if (!GenericEventDialog.waitForInput) {
                AbstractEvent event = currentEvent();
                if (event != null && !event.hasDialog) {
                    pendingChoice = event;
                }
            }
        }
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "update")
    public static class CaptureAfterUpdatePatch {
        @SpirePostfixPatch
        public static void capture() {
            AbstractRoom room = currentRoom();
            if (room == null || room.event == null) {
                publishedEvent = null;
                pendingChoice = null;
                publishedSelection = null;
                publishedOptions = null;
                return;
            }
            if (AbstractDungeon.isFadingIn || AbstractDungeon.isFadingOut
                || CardCrawlGame.isPopupOpen || PeekButton.isPeeking) {
                return;
            }

            AbstractEvent event = room.event;
            boolean sameEvent = event == publishedEvent;
            boolean choiceConsumed = event == pendingChoice;
            boolean followingSelection = sameEvent && publishedSelection != null;
            CurrentScreen selection = choiceConsumed || followingSelection ? selectionScreen(room) : null;
            if (selection != null) {
                if (choiceConsumed || !sameEvent || selection != publishedSelection) {
                    publish(event, selection, null);
                }
                return;
            }

            if (room.phase != AbstractRoom.RoomPhase.EVENT || AbstractDungeon.isScreenUp
                || AbstractDungeon.screen != CurrentScreen.NONE || !dialogReady(event)) {
                return;
            }
            List<LargeDialogOptionButton> options = EventAccess.options();
            if (options.isEmpty() || (!choiceConsumed && sameEvent && publishedOptions != null
                && publishedOptions.matches(options))) {
                return;
            }
            publish(event, null, options);
        }
    }

    private static void publish(AbstractEvent event, CurrentScreen selection, List<LargeDialogOptionButton> options) {
        if (StatePacketEffects.hasPending()) {
            return;
        }
        publishedEvent = event;
        pendingChoice = null;
        publishedSelection = selection;
        publishedOptions = options == null ? null : new DialogOptions(options);
        try {
            EventSnapshot snapshot = selection == null ? EventSnapshotBuilder.build()
                : new EventSnapshot(EventAccess.wireId(event), 0);
            StatePacketSender.send(EventPacketPacker.capture(snapshot, selection != null));
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to capture the event state packet.", exception);
        }
    }

    private static AbstractRoom currentRoom() {
        return AbstractDungeon.currMapNode == null ? null : AbstractDungeon.getCurrRoom();
    }

    private static AbstractEvent currentEvent() {
        AbstractRoom room = currentRoom();
        return room == null ? null : room.event;
    }

    private static boolean dialogReady(AbstractEvent event) {
        if (event.hasDialog) {
            return RoomEventDialog.waitForInput && event.roomEventText != null
                && PrivateFieldAccess.roomEventDialogVisible(event.roomEventText);
        }
        return GenericEventDialog.waitForInput && event.imageEventText != null
            && PrivateFieldAccess.imageEventDialogReady(event.imageEventText);
    }

    private static CurrentScreen selectionScreen(AbstractRoom room) {
        if (!AbstractDungeon.isScreenUp || room.phase == AbstractRoom.RoomPhase.COMBAT) {
            return null;
        }
        CurrentScreen screen = AbstractDungeon.screen;
        if (screen == CurrentScreen.GRID) {
            GridCardSelectScreen grid = AbstractDungeon.gridSelectScreen;
            if (grid != null && grid.targetGroup != null && (!grid.targetGroup.isEmpty()
                || grid.isJustForConfirming || grid.confirmScreenUp || grid.anyNumber)) {
                return screen;
            }
        }
        if (screen == CurrentScreen.CARD_REWARD) {
            CardRewardScreen cards = AbstractDungeon.cardRewardScreen;
            if (cards != null && cards.rewardGroup != null
                && (!cards.rewardGroup.isEmpty() || PrivateFieldAccess.cardRewardSkippable(cards))) {
                return screen;
            }
        }
        return null;
    }

    private static final class DialogOptions {
        private final String[] labels;
        private final boolean[] selectable;

        private DialogOptions(List<LargeDialogOptionButton> options) {
            labels = new String[options.size()];
            selectable = new boolean[options.size()];
            for (int i = 0; i < options.size(); i++) {
                LargeDialogOptionButton option = options.get(i);
                labels[i] = label(option);
                selectable[i] = EventAccess.isSelectable(option);
            }
        }

        private boolean matches(List<LargeDialogOptionButton> options) {
            if (labels.length != options.size()) {
                return false;
            }
            for (int i = 0; i < labels.length; i++) {
                LargeDialogOptionButton option = options.get(i);
                if (!Objects.equals(labels[i], label(option)) || selectable[i] != EventAccess.isSelectable(option)) {
                    return false;
                }
            }
            return true;
        }

        private static String label(LargeDialogOptionButton option) {
            return option == null ? null : option.msg;
        }
    }
}
