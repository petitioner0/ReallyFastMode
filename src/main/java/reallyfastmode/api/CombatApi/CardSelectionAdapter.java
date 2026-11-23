package reallyfastmode.api.CombatApi;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon.CurrentScreen;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.ui.buttons.PeekButton;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import reallyfastmode.access.SelectionAccess;
import reallyfastmode.access.ShopAccess;
import reallyfastmode.patches.access.PrivateFieldAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Commits validated card choices to the active vanilla selection screen. */
final class CardSelectionAdapter {
    private CardSelectionAdapter() {
    }

    static CombatApiResult selectCards(List<String> requestedCardIds) {
        if (requestedCardIds == null) {
            return CombatApiResult.error("invalid_request", "card_ids must be an array.");
        }
        if (!selectionReady()) {
            return noSelection("No card selection is currently accepting input.");
        }

        switch (AbstractDungeon.screen) {
            case HAND_SELECT:
                return selectFromHand(requestedCardIds);
            case GRID:
                return selectFromGrid(requestedCardIds);
            case CARD_REWARD:
                return selectFromCardReward(requestedCardIds);
            case SHOP:
                return purchaseShopCard(requestedCardIds);
            default:
                return noSelection("No supported card selection is currently active.");
        }
    }

    static CombatApiResult skipCardSelection() {
        if (!selectionReady()) {
            return noSelection("No card selection is currently accepting input.");
        }

        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT) {
            return cannotSkipHandSelection();
        }
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID) {
            return cannotSkipGridSelection();
        }
        if (AbstractDungeon.screen != AbstractDungeon.CurrentScreen.CARD_REWARD) {
            return noSelection("No card reward is currently active.");
        }

        CardRewardScreen screen = AbstractDungeon.cardRewardScreen;
        if (screen == null || screen.rewardGroup == null) {
            return noSelection("The active card reward is unavailable.");
        }

        boolean skippable = PrivateFieldAccess.cardRewardSkippable(screen);
        if (!skippable) {
            return CombatApiResult.error(
                "selection_not_skippable",
                "The current card choice requires selecting one option."
            );
        }

        // SkipCardButton closes the screen and leaves any unclaimed reward in the list.
        consumeSelectionInput();
        AbstractDungeon.closeCurrentScreen();
        return CombatApiResult.success("Skipped the card choice.");
    }

    private static boolean selectionReady() {
        return CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY
            && AbstractDungeon.currMapNode != null
            && AbstractDungeon.getCurrRoom() != null
            && AbstractDungeon.player != null
            && !AbstractDungeon.player.isDead
            && !AbstractDungeon.player.isDying
            && AbstractDungeon.screen != null
            && AbstractDungeon.isScreenUp
            && !AbstractDungeon.isFadingIn
            && !AbstractDungeon.isFadingOut
            && !CardCrawlGame.isPopupOpen
            && !PeekButton.isPeeking;
    }

    private static CombatApiResult selectFromHand(List<String> requestedCardIds) {
        HandCardSelectScreen screen = AbstractDungeon.handCardSelectScreen;
        if (screen == null || AbstractDungeon.player == null) {
            return CombatApiResult.error("no_card_selection_pending", "Hand selection is unavailable.");
        }

        CardResolution resolution = resolveCards(SelectionAccess.candidates(), requestedCardIds);
        if (resolution.error != null) {
            return resolution.error;
        }

        boolean anyNumber = PrivateFieldAccess.handSelectAnyNumber(screen);
        SelectionCountRule countRule = anyNumber || screen.upTo
            ? SelectionCountRule.range(screen.numCardsToSelect, screen.canPickZero)
            : SelectionCountRule.exact(screen.numCardsToSelect, screen.canPickZero);
        if (!countRule.allows(resolution.cards.size())) {
            return invalidCount(resolution.cards.size(), countRule);
        }

        // Restore a partially clicked vanilla selection before applying the
        // requested array. Validation above makes this mutation atomic.
        consumeSelectionInput();
        List<AbstractCard> previouslySelected = new ArrayList<AbstractCard>(screen.selectedCards.group);
        screen.selectedCards.clear();
        for (AbstractCard card : previouslySelected) {
            if (!AbstractDungeon.player.hand.contains(card)) {
                AbstractDungeon.player.hand.addToTop(card);
            }
        }
        for (AbstractCard card : resolution.cards) {
            AbstractDungeon.player.hand.removeCard(card);
            card.unhover();
            card.untip();
            screen.selectedCards.addToBottom(card);
        }
        AbstractDungeon.player.hand.refreshHandLayout();
        AbstractDungeon.closeCurrentScreen();

        boolean forTransform = PrivateFieldAccess.handSelectForTransform(screen);
        if (forTransform && screen.selectedCards.size() == 1) {
            AbstractCard card = screen.selectedCards.getBottomCard();
            if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT) {
                AbstractDungeon.srcTransformCard(card);
            } else {
                AbstractDungeon.transformCard(card);
            }
            screen.selectedCards.clear();
        }
        return CombatApiResult.success("Selected " + resolution.cards.size() + " card(s) from hand.");
    }

    private static CombatApiResult selectFromGrid(List<String> requestedCardIds) {
        GridCardSelectScreen screen = AbstractDungeon.gridSelectScreen;
        if (screen == null || screen.targetGroup == null) {
            return noSelection("The active grid is not waiting for a card selection.");
        }
        if (screen.isJustForConfirming) {
            return confirmOfferedCards(screen, requestedCardIds);
        }

        if (screen.confirmScreenUp && requestedCardIds.isEmpty()) {
            AbstractCard previewedCard = PrivateFieldAccess.gridPreviewedCard(screen);
            if (previewedCard == null) {
                return noSelection("The grid has no card to confirm.");
            }
            requestedCardIds = Collections.singletonList(CombatIdentifiers.cardId(previewedCard));
        }
        CardResolution resolution = resolveCards(screen.targetGroup.group, requestedCardIds);
        if (resolution.error != null) {
            return resolution.error;
        }

        int requiredCount = PrivateFieldAccess.gridSelectRequiredCount(screen);
        boolean forClarity = screen.forClarity;
        SelectionCountRule countRule = forClarity
            ? SelectionCountRule.exact(1, false)
            : screen.anyNumber
                ? SelectionCountRule.range(requiredCount, true)
                : SelectionCountRule.exact(requiredCount, false);
        if (!countRule.allows(resolution.cards.size())) {
            return invalidCount(resolution.cards.size(), countRule);
        }

        boolean shopRemoval = screen.forPurge && AbstractDungeon.previousScreen == CurrentScreen.SHOP;
        ShopScreen shop = AbstractDungeon.shopScreen;
        if (shopRemoval) {
            if (shop == null || !shop.isActive || !shop.purgeAvailable) {
                return noSelection("Card removal is unavailable at this shop.");
            }
            if (AbstractDungeon.player.gold < ShopScreen.actualPurgeCost) {
                return CombatApiResult.error("insufficient_gold", "Not enough gold to remove a card.");
            }
        }
        consumeSelectionInput();
        for (AbstractCard selected : screen.selectedCards) {
            selected.stopGlowing();
        }
        screen.selectedCards.clear();
        for (AbstractCard card : resolution.cards) {
            card.unhover();
            card.untip();
            card.stopGlowing();
            screen.selectedCards.add(card);
        }
        PrivateFieldAccess.setGridSelectAmount(screen, resolution.cards.size());
        screen.confirmScreenUp = false;
        screen.upgradePreviewCard = null;
        AbstractDungeon.overlayMenu.cancelButton.hide();
        AbstractDungeon.closeCurrentScreen();
        if (shopRemoval && AbstractDungeon.screen == CurrentScreen.SHOP) {
            shop.updatePurge();
        }
        return CombatApiResult.success("Selected " + resolution.cards.size() + " card(s) from grid.");
    }

    private static CombatApiResult confirmOfferedCards(
        GridCardSelectScreen screen, List<String> requestedCardIds
    ) {
        if (!requestedCardIds.isEmpty()) {
            return CombatApiResult.error("invalid_selection_count", "Confirm this offer without card IDs.");
        }
        consumeSelectionInput();
        AbstractDungeon.overlayMenu.cancelButton.hide();
        AbstractDungeon.dynamicBanner.hide();
        screen.confirmScreenUp = false;
        for (AbstractCard card : screen.targetGroup.group) {
            AbstractDungeon.topLevelEffects.add(new FastCardObtainEffect(card, card.current_x, card.current_y));
        }
        AbstractDungeon.closeCurrentScreen();
        return CombatApiResult.success("Confirmed the offered cards.");
    }

    private static CombatApiResult selectFromCardReward(List<String> requestedCardIds) {
        CardRewardScreen screen = AbstractDungeon.cardRewardScreen;
        if (screen == null || screen.rewardGroup == null) {
            return noSelection("Card reward choices are unavailable.");
        }

        boolean skippable = PrivateFieldAccess.cardRewardSkippable(screen);
        SelectionCountRule countRule = SelectionCountRule.exact(1, false);
        if (!countRule.allows(requestedCardIds.size())) {
            String suffix = skippable
                ? " Use skipCardSelection() to skip this choice."
                : "";
            return invalidCount(requestedCardIds.size(), countRule, suffix);
        }

        CardResolution resolution = resolveCards(screen.rewardGroup, requestedCardIds);
        if (resolution.error != null) {
            return resolution.error;
        }

        consumeSelectionInput();
        PrivateFieldAccess.chooseCardReward(screen, resolution.cards.get(0));
        return CombatApiResult.success("Selected one card reward.");
    }

    private static CombatApiResult purchaseShopCard(List<String> requestedCardIds) {
        ShopScreen shop = AbstractDungeon.shopScreen;
        if (shop == null || !shop.isActive) {
            return noSelection("The shop is not currently accepting purchases.");
        }
        SelectionCountRule countRule = SelectionCountRule.exact(1, false);
        if (!countRule.allows(requestedCardIds.size())) {
            return invalidCount(requestedCardIds.size(), countRule);
        }
        CardResolution resolution = resolveCards(ShopAccess.cards(), requestedCardIds);
        if (resolution.error != null) {
            return resolution.error;
        }
        AbstractCard card = resolution.cards.get(0);
        if (AbstractDungeon.player.gold < card.price) {
            return CombatApiResult.error("insufficient_gold", "Not enough gold to purchase this card.");
        }
        consumeSelectionInput();
        PrivateFieldAccess.purchaseShopCard(shop, card);
        return CombatApiResult.success("Purchased one shop card.");
    }

    private static CombatApiResult cannotSkipHandSelection() {
        HandCardSelectScreen screen = AbstractDungeon.handCardSelectScreen;
        if (screen == null) {
            return CombatApiResult.error("no_card_selection_pending", "Hand selection is unavailable.");
        }
        if (screen.canPickZero) {
            return CombatApiResult.error(
                "selection_not_skippable",
                "Use selectCards() to confirm zero cards in this hand selection."
            );
        }
        return CombatApiResult.error(
            "selection_not_skippable",
            "The current hand selection must be completed."
        );
    }

    private static CombatApiResult cannotSkipGridSelection() {
        GridCardSelectScreen screen = AbstractDungeon.gridSelectScreen;
        if (screen == null || screen.targetGroup == null) {
            return CombatApiResult.error(
                "no_card_selection_pending",
                "The active grid is not waiting for a card selection."
            );
        }
        if (screen.isJustForConfirming || screen.confirmScreenUp || screen.anyNumber) {
            return CombatApiResult.error(
                "selection_not_skippable",
                "Use selectCards() to confirm the active grid without specifying a card ID."
            );
        }
        return CombatApiResult.error(
            "selection_not_skippable",
            "The current grid selection must be completed."
        );
    }

    private static void consumeSelectionInput() {
        InputHelper.justClickedLeft = false;
        CInputActionSet.select.unpress();
    }

    private static CombatApiResult noSelection(String message) {
        return CombatApiResult.error("no_card_selection_pending", message);
    }

    private static CardResolution resolveCards(
        List<AbstractCard> candidates,
        List<String> requestedCardIds
    ) {
        Set<String> seenIds = new HashSet<String>();
        List<AbstractCard> cards = new ArrayList<AbstractCard>();
        for (String requestedId : requestedCardIds) {
            if (requestedId == null || requestedId.trim().isEmpty()) {
                return CardResolution.error(CombatApiResult.error(
                    "selection_card_not_found",
                    "A selected card id is empty."
                ));
            }
            String normalized = requestedId.trim();
            if (!seenIds.add(normalized)) {
                return CardResolution.error(CombatApiResult.error(
                    "duplicate_card",
                    "card_ids contains the same card more than once: " + normalized
                ));
            }

            AbstractCard match = null;
            for (AbstractCard candidate : candidates) {
                if (normalized.equals(CombatIdentifiers.cardId(candidate))) {
                    match = candidate;
                    break;
                }
            }
            if (match == null) {
                return CardResolution.error(CombatApiResult.error(
                    "selection_card_not_found",
                    "Card is not present in the current selection: " + normalized
                ));
            }
            cards.add(match);
        }
        return CardResolution.success(cards);
    }

    private static CombatApiResult invalidCount(int actual, SelectionCountRule rule) {
        return invalidCount(actual, rule, "");
    }

    private static CombatApiResult invalidCount(
        int actual,
        SelectionCountRule rule,
        String suffix
    ) {
        return CombatApiResult.error(
            "invalid_selection_count",
            "Selected " + actual + " card(s); this selection " + rule.describe() + "." + suffix
        );
    }

    private static final class CardResolution {
        private final List<AbstractCard> cards;
        private final CombatApiResult error;

        private CardResolution(List<AbstractCard> cards, CombatApiResult error) {
            this.cards = cards;
            this.error = error;
        }

        static CardResolution success(List<AbstractCard> cards) {
            return new CardResolution(cards, null);
        }

        static CardResolution error(CombatApiResult error) {
            return new CardResolution(new ArrayList<AbstractCard>(), error);
        }
    }
}
