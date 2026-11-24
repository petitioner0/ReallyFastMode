package reallyfastmode.api.ShopApi;

import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.shop.ShopScreen;
import reallyfastmode.api.GameCommandResult;
import reallyfastmode.patches.access.PrivateFieldAccess;

/** Semantic shop commands. Call on the game render thread. */
public final class ShopApi {
    private ShopApi() {
    }

    /** Opens the paid card removal grid. The shop charges gold when a card is selected. */
    public static GameCommandResult openCardRemoval() {
        return GameCommandResult.execute("openCardRemoval", ShopApi::openRemovalGrid);
    }

    private static void openRemovalGrid() {
        ShopScreen shop = AbstractDungeon.shopScreen;
        if (CardCrawlGame.mode != CardCrawlGame.GameMode.GAMEPLAY
            || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.SHOP
            || !AbstractDungeon.isScreenUp
            || shop == null
            || !shop.isActive
            || AbstractDungeon.player == null
            || AbstractDungeon.player.isDead
            || AbstractDungeon.player.isDying
            || AbstractDungeon.isFadingIn
            || AbstractDungeon.isFadingOut
            || CardCrawlGame.isPopupOpen) {
            throw new IllegalStateException("No shop is currently accepting card removal.");
        }
        if (!shop.purgeAvailable) {
            throw new IllegalStateException("Card removal is unavailable at this shop.");
        }
        if (AbstractDungeon.player.gold < ShopScreen.actualPurgeCost) {
            throw new IllegalStateException("Not enough gold to remove a card.");
        }
        CardGroup purgeableCards = AbstractDungeon.player.masterDeck.getPurgeableCards();
        if (CardGroup.getGroupWithoutBottledCards(purgeableCards).isEmpty()) {
            throw new IllegalStateException("The deck has no removable card.");
        }

        InputHelper.justClickedLeft = false;
        CInputActionSet.select.unpress();
        PrivateFieldAccess.openShopCardRemoval(shop);
    }
}
