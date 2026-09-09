package reallyfastmode.api.EventApi;

import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class EventApiTest {
    private int imageSelected;
    private int roomSelected;
    private boolean imageWaiting;
    private boolean roomWaiting;

    @Before
    public void prepareDialogs() {
        imageSelected = GenericEventDialog.selectedOption;
        roomSelected = RoomEventDialog.selectedOption;
        imageWaiting = GenericEventDialog.waitForInput;
        roomWaiting = RoomEventDialog.waitForInput;
        GenericEventDialog.selectedOption = -1;
        RoomEventDialog.selectedOption = -1;
        GenericEventDialog.waitForInput = true;
        RoomEventDialog.waitForInput = true;
    }

    @After
    public void restoreDialogs() {
        GenericEventDialog.selectedOption = imageSelected;
        RoomEventDialog.selectedOption = roomSelected;
        GenericEventDialog.waitForInput = imageWaiting;
        RoomEventDialog.waitForInput = roomWaiting;
    }

    @Test
    public void imageDialogNumbersBottomToTopIncludingDisabledEntries() {
        List<LargeDialogOptionButton> options = options();
        EventApi.submitSelection(0, false, options);
        assertEquals(2, GenericEventDialog.getSelectedOption());
        EventApi.submitSelection(2, false, options);
        assertEquals(0, GenericEventDialog.getSelectedOption());
        assertEquals(-1, RoomEventDialog.selectedOption);
        assertTrue(RoomEventDialog.waitForInput);
    }

    @Test
    public void roomDialogUsesItsOwnSelectionChannelAndVanillaListIndex() {
        List<LargeDialogOptionButton> options = options();
        // A stale slot number must not replace the index used by dialog.update().
        options.get(2).slot = 9;
        EventApi.submitSelection(0, true, options);
        assertEquals(2, RoomEventDialog.selectedOption);
        assertFalse(RoomEventDialog.waitForInput);
        assertEquals(-1, GenericEventDialog.selectedOption);
        assertTrue(GenericEventDialog.waitForInput);
    }

    @Test
    public void disabledOptionsThrowWithoutChangingSelectionOrInput() {
        List<LargeDialogOptionButton> options = options();
        options.get(1).pressed = true;
        options.get(1).hb.clicked = true;
        for (boolean roomDialog : new boolean[] {false, true}) {
            assertThrows(IllegalArgumentException.class,
                () -> EventApi.submitSelection(1, roomDialog, options));
        }
        assertUnchanged();
        assertTrue(options.get(1).pressed);
        assertTrue(options.get(1).hb.clicked);
    }

    @Test
    public void disabledBottomOptionStillOccupiesZero() {
        List<LargeDialogOptionButton> options = Arrays.asList(
            new LargeDialogOptionButton(0, "top"),
            new LargeDialogOptionButton(1, "bottom", true));
        assertThrows(IllegalArgumentException.class,
            () -> EventApi.submitSelection(0, false, options));
        EventApi.submitSelection(1, false, options);
        assertEquals(0, GenericEventDialog.selectedOption);
    }

    @Test
    public void outOfRangeAndEmptyListsThrowWithoutChangingState() {
        for (boolean roomDialog : new boolean[] {false, true}) {
            for (int index : new int[] {-1, 3, Integer.MAX_VALUE}) {
                assertThrows(IllegalArgumentException.class,
                    () -> EventApi.submitSelection(index, roomDialog, options()));
            }
            assertThrows(IllegalArgumentException.class,
                () -> EventApi.submitSelection(0, roomDialog,
                    Collections.<LargeDialogOptionButton>emptyList()));
        }
        assertThrows(IllegalArgumentException.class, () -> EventApi.selectOption(-1));
        assertUnchanged();
    }

    @Test
    public void pendingSelectionCannotBeOverwrittenBeforeConsumption() {
        List<LargeDialogOptionButton> options = options();
        for (boolean roomDialog : new boolean[] {false, true}) {
            EventApi.submitSelection(0, roomDialog, options);
            assertThrows(IllegalStateException.class,
                () -> EventApi.submitSelection(2, roomDialog, options));
        }
        assertEquals(2, GenericEventDialog.selectedOption);
        assertEquals(2, RoomEventDialog.selectedOption);
    }

    @Test
    public void successfulSelectionClearsStaleButtonInput() {
        List<LargeDialogOptionButton> options = options();
        for (LargeDialogOptionButton option : options) {
            option.pressed = true;
            option.hb.clicked = true;
            option.hb.clickStarted = true;
        }
        EventApi.submitSelection(0, false, options);
        for (LargeDialogOptionButton option : options) {
            assertFalse(option.pressed);
            assertFalse(option.hb.clicked);
            assertFalse(option.hb.clickStarted);
        }
        assertFalse(GenericEventDialog.waitForInput);
    }

    private static List<LargeDialogOptionButton> options() {
        return Arrays.asList(new LargeDialogOptionButton(0, "top"),
            new LargeDialogOptionButton(1, "middle", true),
            new LargeDialogOptionButton(2, "bottom"));
    }

    private static void assertUnchanged() {
        assertEquals(-1, GenericEventDialog.selectedOption);
        assertEquals(-1, RoomEventDialog.selectedOption);
        assertTrue(GenericEventDialog.waitForInput);
        assertTrue(RoomEventDialog.waitForInput);
    }
}
