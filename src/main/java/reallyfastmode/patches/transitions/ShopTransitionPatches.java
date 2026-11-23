package reallyfastmode.patches.transitions;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.shop.Merchant;
import com.megacrit.cardcrawl.shop.ShopScreen;

/** Opens the merchant screen once the room has initialized the shop inventory. */
public final class ShopTransitionPatches {
    private ShopTransitionPatches() {
    }

    @SpirePatch2(clz = ShopRoom.class, method = "onPlayerEntry")
    public static class OpenMerchantOnEntryPatch {
        @SpirePostfixPatch
        public static void openMerchant(ShopRoom __instance) {
            ShopScreen shop = AbstractDungeon.shopScreen;
            if (__instance.merchant == null || shop == null || !shop.isActive
                || AbstractDungeon.isScreenUp
                || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.NONE) {
                return;
            }

            // Consume the room entry input before the new screen starts handling it.
            InputHelper.justClickedLeft = false;
            CInputActionSet.select.unpress();
            AbstractDungeon.overlayMenu.proceedButton.setLabel(Merchant.NAMES[0]);
            shop.open();
        }
    }
}
