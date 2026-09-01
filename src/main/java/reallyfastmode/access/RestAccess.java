package reallyfastmode.access;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import reallyfastmode.patches.access.PrivateFieldAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Reads the current rest-room campfire and its options. */
public final class RestAccess {
    private RestAccess() {
    }

    public static boolean inRestRoom() {
        return AbstractDungeon.getCurrRoom() instanceof RestRoom;
    }

    public static RestRoom restRoom() {
        if (!(AbstractDungeon.getCurrRoom() instanceof RestRoom)) {
            throw new IllegalStateException("No current rest room is available.");
        }
        return (RestRoom) AbstractDungeon.getCurrRoom();
    }

    public static CampfireUI campfire() {
        CampfireUI campfire = restRoom().campfireUI;
        if (campfire == null) {
            throw new IllegalStateException("The current rest room has no campfire UI.");
        }
        return campfire;
    }

    public static boolean somethingSelected() {
        return campfire().somethingSelected;
    }

    public static List<AbstractCampfireOption> options() {
        if (!inRestRoom() || ((RestRoom) AbstractDungeon.getCurrRoom()).campfireUI == null) {
            return Collections.emptyList();
        }
        return immutableCopy(PrivateFieldAccess.campfireButtons(campfire()));
    }

    public static boolean usable(AbstractCampfireOption option) {
        return option(option).usable;
    }

    private static AbstractCampfireOption option(AbstractCampfireOption option) {
        return Objects.requireNonNull(option, "option");
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
