package reallyfastmode.access;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import reallyfastmode.protocol.VanillaCatalog.VanillaEventCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reads the event and dialog options currently presented to the player. */
public final class EventAccess {
    private EventAccess() {
    }

    public static boolean inEventRoom() {
        return AbstractDungeon.getCurrRoom() instanceof EventRoom;
    }

    public static AbstractEvent event() {
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        return room == null ? null : room.event;
    }

    public static boolean combatTime() {
        return requiredEvent().combatTime;
    }

    public static int wireId() {
        AbstractEvent event = requiredEvent();
        try {
            String eventId = (String) event.getClass().getField("ID").get(null);
            return VanillaEventCatalog.wireId(eventId);
        } catch (NoSuchFieldException exception) {
            throw new IllegalArgumentException(
                event.getClass().getName() + " has no public static ID field",
                exception
            );
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                "Cannot read " + event.getClass().getName() + ".ID",
                exception
            );
        }
    }

    public static List<LargeDialogOptionButton> options() {
        AbstractEvent event = event();
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (event == null || room == null || room.phase != AbstractRoom.RoomPhase.EVENT) {
            return Collections.emptyList();
        }
        return event.hasDialog
            ? immutableCopy(RoomEventDialog.optionList)
            : immutableCopy(event.imageEventText.optionList);
    }

    public static int selectedOption() {
        AbstractEvent event = requiredEvent();
        return event.hasDialog ? RoomEventDialog.selectedOption : GenericEventDialog.selectedOption;
    }

    public static boolean isSelectable(LargeDialogOptionButton option) {
        return option != null && !option.isDisabled;
    }

    private static AbstractEvent requiredEvent() {
        AbstractEvent event = event();
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (event == null || room == null || room.phase != AbstractRoom.RoomPhase.EVENT) {
            throw new IllegalStateException("No event input is currently active.");
        }
        return event;
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
