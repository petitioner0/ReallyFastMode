package reallyfastmode.patches.protocol;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.patches.access.PrivateFieldAccess;
import reallyfastmode.protocol.io.StatePacketSender;
import reallyfastmode.protocol.write.RestPacketPacker;

/** Publishes the campfire menu, follow-up card selection, and settled room. */
public final class RestPacketPatches {
    private static final Logger LOGGER = LogManager.getLogger(RestPacketPatches.class.getName());
    private static CampfireUI trackedCampfire;
    private static Stage publishedStage;

    private RestPacketPatches() {
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "update")
    public static class CaptureAfterUpdatePatch {
        @SpirePostfixPatch
        public static void capture() {
            RestRoom room = currentRestRoom();
            CampfireUI campfire = room == null ? null : room.campfireUI;
            if (campfire != trackedCampfire) {
                trackedCampfire = campfire;
                publishedStage = null;
            }
            if (campfire == null || AbstractDungeon.isFadingIn || AbstractDungeon.isFadingOut
                || CardCrawlGame.isPopupOpen || PeekButton.isPeeking) {
                return;
            }

            Stage stage = readyStage(room, campfire);
            if (stage == null || stage == publishedStage || StatePacketEffects.hasPending()) {
                return;
            }

            publishedStage = stage;
            try {
                byte[] packet = stage == Stage.SETTLED ? RestPacketPacker.captureSettlement()
                    : RestPacketPacker.capture(stage == Stage.SELECTION);
                StatePacketSender.send(packet);
            } catch (RuntimeException exception) {
                LOGGER.error("Unable to capture the campfire state packet.", exception);
            }
        }
    }

    private static Stage readyStage(RestRoom room, CampfireUI campfire) {
        if (campfire.somethingSelected && isSelectionReady()) {
            return Stage.SELECTION;
        }
        boolean roomVisible = !AbstractDungeon.isScreenUp && AbstractDungeon.screen == CurrentScreen.NONE;
        if (room.phase == RoomPhase.COMPLETE) {
            // A DIG reward can lead straight from the reward screen to the map.
            boolean mapOpen = AbstractDungeon.isScreenUp && AbstractDungeon.screen == CurrentScreen.MAP;
            if (mapOpen || (roomVisible
                && PrivateFieldAccess.proceedButtonAcceptsClick(AbstractDungeon.overlayMenu.proceedButton))) {
                return Stage.SETTLED;
            }
        } else if (room.phase == RoomPhase.INCOMPLETE && !campfire.somethingSelected
            && !CampfireUI.hidden && roomVisible) {
            return Stage.MENU;
        }
        return null;
    }

    private static RestRoom currentRestRoom() {
        AbstractRoom room = AbstractDungeon.currMapNode == null ? null : AbstractDungeon.getCurrRoom();
        return room instanceof RestRoom ? (RestRoom) room : null;
    }

    private static boolean isSelectionReady() {
        if (!AbstractDungeon.isScreenUp) {
            return false;
        }
        if (AbstractDungeon.screen == CurrentScreen.GRID
            && AbstractDungeon.previousScreen != CurrentScreen.COMBAT_REWARD
            && AbstractDungeon.previousScreen != CurrentScreen.SHOP) {
            GridCardSelectScreen grid = AbstractDungeon.gridSelectScreen;
            return grid != null && grid.targetGroup != null && !grid.targetGroup.isEmpty();
        }
        CardRewardScreen cards = AbstractDungeon.cardRewardScreen;
        return AbstractDungeon.screen == CurrentScreen.CARD_REWARD && cards != null && cards.rItem == null
            && cards.rewardGroup != null && !cards.rewardGroup.isEmpty();
    }

    private enum Stage {
        MENU,
        SELECTION,
        SETTLED
    }
}
