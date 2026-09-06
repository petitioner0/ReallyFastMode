package reallyfastmode.access;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import reallyfastmode.patches.access.RunIdPatches;

/**
 * Reads run-wide state directly from Slay the Spire's current global objects.
 * Callers must invoke these methods on the game render thread.
 */
public final class GameAccess {
    private GameAccess() {
    }

    public static boolean inDungeon() {
        return AbstractDungeon.isPlayerInDungeon();
    }

    public static String dungeonId() {
        return AbstractDungeon.id;
    }

    public static String dungeonName() {
        return AbstractDungeon.name;
    }

    public static int act() {
        return AbstractDungeon.actNum;
    }

    public static int floor() {
        return AbstractDungeon.floorNum;
    }

    public static Long seed() {
        return Settings.seed;
    }

    public static int runId() {
        return RunIdPatches.runId(CardCrawlGame.dungeon);
    }

    public static int ascensionLevel() {
        return AbstractDungeon.ascensionLevel;
    }

    public static boolean ascensionMode() {
        return AbstractDungeon.isAscensionMode;
    }

    public static boolean hasRubyKey() {
        return Settings.hasRubyKey;
    }

    public static boolean hasEmeraldKey() {
        return Settings.hasEmeraldKey;
    }

    public static boolean hasSapphireKey() {
        return Settings.hasSapphireKey;
    }

    public static AbstractDungeon.CurrentScreen screen() {
        return AbstractDungeon.screen;
    }

    public static boolean screenUp() {
        return AbstractDungeon.isScreenUp;
    }

    private static AbstractRoom requiredRoom() {
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null) {
            throw new IllegalStateException("No current dungeon room is available.");
        }
        return room;
    }

    public static AbstractRoom.RoomPhase roomPhase() {
        return requiredRoom().phase;
    }

    public static boolean dungeonBeaten() {
        return AbstractDungeon.isDungeonBeaten;
    }

}
