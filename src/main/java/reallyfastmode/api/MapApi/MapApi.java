package reallyfastmode.api.MapApi;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import reallyfastmode.access.MapAccess;
import reallyfastmode.patches.access.PrivateFieldAccess;

import java.util.List;

/** Semantic map commands. Call on the game render thread. */
public final class MapApi {
    private MapApi() {
    }

    /**
     * Selects a reachable node by its zero-based map coordinates (x, y).
     * Vanilla MapRoomNode.update starts the room transition on its next update.
     *
     * @throws IllegalArgumentException if the coordinates are not currently reachable
     * @throws IllegalStateException if the map is not accepting a selection
     */
    public static void selectNode(int x, int y) {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Map coordinates must be nonnegative.");
        }
        validateSelectionReady();
        MapRoomNode selected = requireReachableNode(x, y, MapAccess.availableMapNodes());

        boolean firstRoom = !AbstractDungeon.firstRoomChosen;
        MapRoomNode current = AbstractDungeon.getCurrMapNode();
        AbstractRelic wingedGreaves = null;
        if (!firstRoom && !current.isConnectedTo(selected)
            && current.wingedIsConnectedTo(selected)
            && AbstractDungeon.player.hasRelic("WingedGreaves")) {
            wingedGreaves = AbstractDungeon.player.getRelic("WingedGreaves");
        }

        // All validation precedes mutations. These are the gameplay side effects
        // of MapRoomNode.update's first-floor and normal/winged click branches.
        AbstractDungeon.dungeonMapScreen.clicked = false;
        AbstractDungeon.dungeonMapScreen.clickTimer = 0.0F;
        if (firstRoom) {
            AbstractDungeon.dungeonMapScreen.dismissable = true;
            AbstractDungeon.firstRoomChosen = true;
        } else {
            if (wingedGreaves != null) {
                --wingedGreaves.counter;
                if (wingedGreaves.counter <= 0) {
                    wingedGreaves.setCounter(-2);
                }
            }
            if (selected.room instanceof EventRoom) {
                ++CardCrawlGame.mysteryMachine;
            }
        }

        // Reuse the original delayed-selection branch for path/metrics/transition
        // bookkeeping, without simulating a hitbox click or waiting for an animation.
        PrivateFieldAccess.queueMapNodeSelection(selected);
    }

    private static void validateSelectionReady() {
        if (CardCrawlGame.mode != CardCrawlGame.GameMode.GAMEPLAY
            || AbstractDungeon.currMapNode == null
            || AbstractDungeon.getCurrRoom() == null
            || AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMPLETE
            || AbstractDungeon.player == null
            || AbstractDungeon.player.isDead
            || AbstractDungeon.player.isDying
            || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.MAP
            || !AbstractDungeon.isScreenUp
            || AbstractDungeon.dungeonMapScreen == null
            || AbstractDungeon.map == null
            || AbstractDungeon.isFadingIn
            || AbstractDungeon.isFadingOut
            || AbstractDungeon.isDungeonBeaten) {
            throw new IllegalStateException("The map is not accepting a node selection.");
        }

        // Also reject a mouse/controller selection still waiting for its animation.
        for (List<MapRoomNode> row : AbstractDungeon.map) {
            if (row == null) {
                continue;
            }
            for (MapRoomNode node : row) {
                if (node != null && PrivateFieldAccess.mapNodeSelectionPending(node)) {
                    throw new IllegalStateException("A map node selection is already pending.");
                }
            }
        }
    }

    static MapRoomNode requireReachableNode(int x, int y, List<MapRoomNode> available) {
        for (MapRoomNode node : available) {
            if (node.x == x && node.y == y) {
                return node;
            }
        }
        throw new IllegalArgumentException("Map node is not currently reachable: (" + x + ", " + y + ").");
    }
}
