package reallyfastmode.patches.protocol;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.access.GameAccess;
import reallyfastmode.protocol.io.StatePacketSender;
import reallyfastmode.protocol.write.MapPacketPacker;

/** Sends the full map once after entering a floor or loading its dungeon. */
@SpirePatch2(clz = AbstractDungeon.class, method = "update")
public final class MapPacketPatches {
    private static final Logger LOGGER = LogManager.getLogger(MapPacketPatches.class.getName());
    private static AbstractDungeon publishedDungeon;
    private static int publishedFloor;

    private MapPacketPatches() {
    }

    @SpirePostfixPatch
    public static void capture(AbstractDungeon __instance) {
        int floor = GameAccess.floor();
        if (publishedDungeon == __instance && publishedFloor == floor) {
            return;
        }
        if (CardCrawlGame.dungeon != __instance || AbstractDungeon.currMapNode == null
            || AbstractDungeon.map == null || AbstractDungeon.map.isEmpty()) {
            return;
        }

        publishedDungeon = __instance;
        publishedFloor = floor;
        try {
            StatePacketSender.send(MapPacketPacker.capture());
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to capture the full map packet for floor {}.", floor, exception);
        }
    }
}
