package reallyfastmode.patches.protocol;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.relics.DreamCatcher;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import com.megacrit.cardcrawl.ui.campfire.RestOption;
import com.megacrit.cardcrawl.ui.campfire.SmithOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.protocol.io.StatePacketSender;
import reallyfastmode.protocol.write.RestPacketPacker;

/** Publishes the campfire menu and the card selections triggered by Smith or Dream Catcher. */
public final class RestPacketPatches {
    private static final Logger LOGGER = LogManager.getLogger(RestPacketPatches.class.getName());
    private static CampfireUI publishedCampfire;
    private static CampfireUI pendingCampfire;
    private static CurrentScreen pendingSelection;

    private RestPacketPatches() {
    }

    @SpirePatch2(clz = SmithOption.class, method = "useOption")
    public static class TrackSmithPatch {
        @SpirePostfixPatch
        public static void track(SmithOption __instance) {
            if (__instance.usable) {
                awaitSelection(CurrentScreen.GRID);
            }
        }
    }

    @SpirePatch2(clz = RestOption.class, method = "useOption")
    public static class TrackDreamCatcherPatch {
        @SpirePostfixPatch
        public static void track(RestOption __instance) {
            if (__instance.usable && AbstractDungeon.player.hasRelic(DreamCatcher.ID)) {
                awaitSelection(CurrentScreen.CARD_REWARD);
            }
        }
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "update")
    public static class CaptureAfterUpdatePatch {
        @SpirePostfixPatch
        public static void capture() {
            CampfireUI campfire = currentCampfire();
            if (campfire == null) {
                publishedCampfire = null;
                pendingCampfire = null;
                pendingSelection = null;
                return;
            }
            if (AbstractDungeon.isFadingIn || AbstractDungeon.isFadingOut
                || CardCrawlGame.isPopupOpen || PeekButton.isPeeking) {
                return;
            }

            boolean menuReady = campfire != publishedCampfire && !campfire.somethingSelected
                && !CampfireUI.hidden && !AbstractDungeon.isScreenUp && AbstractDungeon.screen == CurrentScreen.NONE;
            boolean selectionReady = campfire == pendingCampfire && isSelectionReady();
            if ((!menuReady && !selectionReady) || StatePacketEffects.hasPending()) {
                return;
            }

            if (selectionReady) {
                pendingCampfire = null;
                pendingSelection = null;
            } else {
                publishedCampfire = campfire;
            }
            try {
                StatePacketSender.send(RestPacketPacker.capture(selectionReady));
            } catch (RuntimeException exception) {
                LOGGER.error("Unable to capture the campfire state packet.", exception);
            }
        }
    }

    private static void awaitSelection(CurrentScreen screen) {
        CampfireUI campfire = currentCampfire();
        if (campfire != null) {
            pendingCampfire = campfire;
            pendingSelection = screen;
        }
    }

    private static CampfireUI currentCampfire() {
        AbstractRoom room = AbstractDungeon.currMapNode == null ? null : AbstractDungeon.getCurrRoom();
        return room instanceof RestRoom ? ((RestRoom) room).campfireUI : null;
    }

    private static boolean isSelectionReady() {
        if (!AbstractDungeon.isScreenUp || AbstractDungeon.screen != pendingSelection) {
            return false;
        }
        if (pendingSelection == CurrentScreen.GRID) {
            GridCardSelectScreen grid = AbstractDungeon.gridSelectScreen;
            return grid != null && grid.forUpgrade && grid.targetGroup != null && !grid.targetGroup.isEmpty();
        }
        CardRewardScreen cards = AbstractDungeon.cardRewardScreen;
        return cards != null && cards.rItem == null && cards.rewardGroup != null && !cards.rewardGroup.isEmpty();
    }
}
