package reallyfastmode.api.MapApi;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.map.DungeonMap;
import com.megacrit.cardcrawl.map.MapEdge;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import reallyfastmode.access.MapAccess;
import reallyfastmode.api.GameCommandResult;
import reallyfastmode.patches.access.PrivateFieldAccess;
import reallyfastmode.protocol.MapProtocol;

import java.util.List;

/** Semantic map commands. Call on the game render thread. */
public final class MapApi {
    private MapApi() {
    }

    /**
     * Selects a reachable node by its zero-based map coordinates (x, y).
     * The Boss icon uses (-1, 15) and starts its transition immediately.
     * Regular nodes start their transition on the next vanilla node update.
     */
    public static GameCommandResult selectNode(int x, int y) {
        return GameCommandResult.execute("selectNode", () -> submitNodeSelection(x, y));
    }

    private static void submitNodeSelection(int x, int y) {
        boolean bossIcon = x == -1 && y == MapProtocol.MAP_HEIGHT;
        if (!bossIcon && (x < 0 || y < 0)) {
            throw new IllegalArgumentException("Only the Boss node (-1, 15) accepts a negative coordinate.");
        }
        validateSelectionReady();
        MapRoomNode selected = requireReachableNode(x, y, MapAccess.availableMapNodes());
        if (bossIcon) {
            enterBossRoom(selected);
            return;
        }

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

    private static void enterBossRoom(MapRoomNode boss) {
        DungeonMap map = AbstractDungeon.dungeonMapScreen.map;
        if (map == null) {
            throw new IllegalStateException("The Boss map icon is unavailable.");
        }

        // Match DungeonMap.update's Boss icon branch, including path bookkeeping.
        MapRoomNode current = AbstractDungeon.getCurrMapNode();
        current.taken = true;
        for (MapEdge edge : current.getEdges()) {
            if (edge != null) {
                edge.markAsTaken();
            }
        }
        InputHelper.justClickedLeft = false;
        CardCrawlGame.music.fadeOutTempBGM();
        AbstractDungeon.nextRoom = boss;
        List<Integer> pathX = AbstractDungeon.pathX;
        List<Integer> pathY = AbstractDungeon.pathY;
        if (pathY.size() > 1) {
            pathX.add(pathX.get(pathX.size() - 1));
            pathY.add(pathY.get(pathY.size() - 1) + 1);
        } else {
            pathX.add(1);
            pathY.add(boss.y);
        }
        AbstractDungeon.nextRoomTransitionStart();
        map.bossHb.hovered = false;
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
            if (node.x == x && node.y == y && (x >= 0 || node.room instanceof MonsterRoomBoss)) {
                return node;
            }
        }
        throw new IllegalArgumentException("Map node is not currently reachable: (" + x + ", " + y + ").");
    }
}
