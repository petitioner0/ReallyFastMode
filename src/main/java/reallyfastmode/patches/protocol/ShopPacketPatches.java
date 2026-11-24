package reallyfastmode.patches.protocol;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.access.ShopAccess;
import reallyfastmode.protocol.io.StatePacketSender;
import reallyfastmode.protocol.write.ShopPacketPacker;

/** Publishes shop state when it opens, opens the removal grid, or settles an operation. */
public final class ShopPacketPatches {
    private static final Logger LOGGER = LogManager.getLogger(ShopPacketPatches.class.getName());
    private static AbstractRoom pendingRoom;
    private static int pendingOperations;
    private static AbstractRoom pendingRemovalRoom;

    private ShopPacketPatches() {
    }

    @SpirePatch2(clz = ShopScreen.class, method = "open")
    public static class TrackShopOpenPatch {
        @SpirePostfixPatch
        public static void track(ShopScreen __instance) {
            if (__instance == AbstractDungeon.shopScreen) {
                scheduleCapture(false);
            }
        }
    }

    @SpirePatch2(clz = ShopScreen.class, method = "purchasePurge")
    public static class TrackRemovalGridPatch {
        @SpirePostfixPatch
        public static void track(ShopScreen __instance) {
            if (__instance == AbstractDungeon.shopScreen && ShopAccess.isCardRemovalOpen()) {
                pendingRemovalRoom = currentRoom();
            }
        }
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "update")
    public static class CaptureRemovalGridPatch {
        @SpirePostfixPatch
        public static void capture() {
            if (pendingRemovalRoom == null) {
                return;
            }
            if (pendingRemovalRoom != currentRoom() || AbstractDungeon.screen == CurrentScreen.SHOP) {
                pendingRemovalRoom = null;
                return;
            }
            if (!ShopAccess.isCardRemovalOpen() || AbstractDungeon.gridSelectScreen.targetGroup == null
                || AbstractDungeon.isFadingIn || AbstractDungeon.isFadingOut
                || CardCrawlGame.isPopupOpen || PeekButton.isPeeking || StatePacketEffects.hasPending()) {
                return;
            }

            pendingRemovalRoom = null;
            try {
                StatePacketSender.send(ShopPacketPacker.captureRemovalSelection());
            } catch (RuntimeException exception) {
                LOGGER.error("Unable to capture the shop removal selection packet.", exception);
            }
        }
    }

    @SpirePatch2(clz = ShopScreen.class, method = "purchaseCard")
    @SpirePatch2(clz = StoreRelic.class, method = "purchaseRelic")
    @SpirePatch2(clz = StorePotion.class, method = "purchasePotion")
    @SpirePatch2(clz = ShopScreen.class, method = "purgeCard")
    public static class TrackShopOperationPatch {
        @SpirePostfixPatch
        public static void track() {
            scheduleCapture(true);
        }
    }

    @SpirePatch2(clz = ShopScreen.class, method = "update")
    public static class CaptureAfterShopUpdatePatch {
        @SpirePostfixPatch
        public static void capture(ShopScreen __instance) {
            if (pendingRoom == null || __instance != AbstractDungeon.shopScreen || !__instance.isActive
                || !AbstractDungeon.isScreenUp || AbstractDungeon.screen != CurrentScreen.SHOP
                || pendingRoom != currentRoom() || StatePacketEffects.hasPending()) {
                return;
            }

            // Reopening after an operation can use the same packet as its completion.
            int packetCount = Math.max(1, pendingOperations);
            pendingRoom = null;
            pendingOperations = 0;
            try {
                byte[] packet = ShopPacketPacker.capture();
                for (int i = 0; i < packetCount; i++) {
                    StatePacketSender.send(packet);
                }
            } catch (RuntimeException exception) {
                LOGGER.error("Unable to capture the shop packet.", exception);
            }
        }
    }

    private static void scheduleCapture(boolean operation) {
        AbstractRoom room = currentRoom();
        if (room == null) {
            return;
        }
        if (room != pendingRoom) {
            pendingRoom = room;
            pendingOperations = 0;
        }
        if (operation) {
            pendingOperations++;
        }
    }

    private static AbstractRoom currentRoom() {
        return AbstractDungeon.currMapNode == null ? null : AbstractDungeon.getCurrRoom();
    }
}
