package reallyfastmode.api;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import com.megacrit.cardcrawl.vfx.cardManip.ExhaustCardEffect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Commits validated card choices to the active vanilla selection screen. */
final class CombatSelectionAdapter {
    private CombatSelectionAdapter() {
    }

    static CombatApiResult selectCards(List<String> requestedCardIds) {
        if (requestedCardIds == null) {
            return CombatApiResult.error("invalid_request", "card_ids must be an array.");
        }
        // TODO(non-combat): model master-deck GRID operations and ordinary
        // card reward Take / Skip / Singing Bowl as separate API actions.
        if (!isCombatRoom()) {
            return CombatApiResult.error("not_in_combat", "Card selection is only available in combat.");
        }

        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT) {
            return selectFromHand(requestedCardIds);
        }
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID) {
            return selectFromGrid(requestedCardIds);
        }
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.CARD_REWARD) {
            return selectFromCombatCardReward(requestedCardIds);
        }
        return CombatApiResult.error(
            "no_card_selection_pending",
            "No supported combat card selection is currently active."
        );
    }

    static CombatApiResult skipCardSelection() {
        if (!isCombatRoom()) {
            return CombatApiResult.error("not_in_combat", "Card selection is only available in combat.");
        }

        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.HAND_SELECT) {
            return cannotSkipHandSelection();
        }
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID) {
            return cannotSkipGridSelection();
        }
        if (AbstractDungeon.screen != AbstractDungeon.CurrentScreen.CARD_REWARD) {
            return CombatApiResult.error(
                "no_card_selection_pending",
                "No supported combat card selection is currently active."
            );
        }

        CardRewardScreen screen = AbstractDungeon.cardRewardScreen;
        if (!isCombatCardReward(screen)) {
            return CombatApiResult.error(
                "no_card_selection_pending",
                "The active card reward is not a combat card choice."
            );
        }

        boolean skippable = ReflectionHacks.getPrivate(
            screen,
            CardRewardScreen.class,
            "skippable"
        );
        if (!skippable) {
            return CombatApiResult.error(
                "selection_not_skippable",
                "The current combat card choice requires selecting one option."
            );
        }

        // Vanilla SkipCardButton closes the screen without assigning a chosen
        // card. The waiting action observes its already-null result field.
        AbstractDungeon.closeCurrentScreen();
        return CombatApiResult.success("Skipped the combat card choice.");
    }

    private static CombatApiResult selectFromHand(List<String> requestedCardIds) {
        HandCardSelectScreen screen = AbstractDungeon.handCardSelectScreen;
        if (screen == null || AbstractDungeon.player == null) {
            return CombatApiResult.error("no_card_selection_pending", "Hand selection is unavailable.");
        }

        List<AbstractCard> candidates = new ArrayList<AbstractCard>();
        candidates.addAll(AbstractDungeon.player.hand.group);
        for (AbstractCard selected : screen.selectedCards.group) {
            if (!candidates.contains(selected)) {
                candidates.add(selected);
            }
        }

        CardResolution resolution = resolveCards(candidates, requestedCardIds);
        if (resolution.error != null) {
            return resolution.error;
        }

        boolean anyNumber = ReflectionHacks.getPrivate(
            screen,
            HandCardSelectScreen.class,
            "anyNumber"
        );
        SelectionCountRule countRule = anyNumber || screen.upTo
            ? SelectionCountRule.range(screen.numCardsToSelect, screen.canPickZero)
            : SelectionCountRule.exact(screen.numCardsToSelect, screen.canPickZero);
        if (!countRule.allows(resolution.cards.size())) {
            return invalidCount(resolution.cards.size(), countRule);
        }

        // Restore a partially clicked vanilla selection before applying the
        // requested array. Validation above makes this mutation atomic.
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

        boolean forTransform = ReflectionHacks.getPrivate(
            screen,
            HandCardSelectScreen.class,
            "forTransform"
        );
        if (forTransform && screen.selectedCards.size() == 1) {
            AbstractDungeon.srcTransformCard(screen.selectedCards.getBottomCard());
            screen.selectedCards.clear();
        }
        return CombatApiResult.success("Selected " + resolution.cards.size() + " card(s) from hand.");
    }

    private static CombatApiResult selectFromGrid(List<String> requestedCardIds) {
        GridCardSelectScreen screen = AbstractDungeon.gridSelectScreen;
        if (screen == null || screen.targetGroup == null || screen.isJustForConfirming) {
            return CombatApiResult.error(
                "no_card_selection_pending",
                "The active grid is not waiting for a card selection."
            );
        }

        CardResolution resolution = resolveCards(screen.targetGroup.group, requestedCardIds);
        if (resolution.error != null) {
            return resolution.error;
        }

        int requiredCount = ReflectionHacks.getPrivate(
            screen,
            GridCardSelectScreen.class,
            "numCards"
        );
        boolean forClarity = ReflectionHacks.getPrivate(
            screen,
            GridCardSelectScreen.class,
            "forClarity"
        );
        SelectionCountRule countRule = forClarity
            ? SelectionCountRule.exact(1, false)
            : screen.anyNumber
                ? SelectionCountRule.range(requiredCount, true)
                : SelectionCountRule.exact(requiredCount, false);
        if (!countRule.allows(resolution.cards.size())) {
            return invalidCount(resolution.cards.size(), countRule);
        }

        for (Object selected : new ArrayList<Object>(screen.selectedCards)) {
            if (selected instanceof AbstractCard) {
                ((AbstractCard) selected).stopGlowing();
            }
        }
        screen.selectedCards.clear();
        for (AbstractCard card : resolution.cards) {
            card.unhover();
            card.untip();
            card.stopGlowing();
            screen.selectedCards.add(card);
        }
        ReflectionHacks.setPrivate(
            screen,
            GridCardSelectScreen.class,
            "cardSelectAmount",
            resolution.cards.size()
        );
        AbstractDungeon.overlayMenu.cancelButton.hide();
        AbstractDungeon.closeCurrentScreen();
        return CombatApiResult.success("Selected " + resolution.cards.size() + " card(s) from grid.");
    }

    private static CombatApiResult selectFromCombatCardReward(List<String> requestedCardIds) {
        CardRewardScreen screen = AbstractDungeon.cardRewardScreen;
        if (!isCombatCardReward(screen)) {
            return CombatApiResult.error("no_card_selection_pending", "Combat card choices are unavailable.");
        }

        boolean chooseOne = ReflectionHacks.getPrivate(screen, CardRewardScreen.class, "chooseOne");
        boolean skippable = ReflectionHacks.getPrivate(screen, CardRewardScreen.class, "skippable");
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

        AbstractCard chosen = resolution.cards.get(0);
        if (chooseOne) {
            chosen.onChoseThisOption();
            AbstractDungeon.effectList.add(new ExhaustCardEffect(chosen));
        } else {
            screen.discoveryCard = chosen;
        }
        AbstractDungeon.closeCurrentScreen();
        return CombatApiResult.success("Selected one generated card.");
    }

    private static CombatApiResult cannotSkipHandSelection() {
        HandCardSelectScreen screen = AbstractDungeon.handCardSelectScreen;
        if (screen == null) {
            return CombatApiResult.error("no_card_selection_pending", "Hand selection is unavailable.");
        }
        if (screen.canPickZero) {
            return CombatApiResult.error(
                "selection_not_skippable",
                "This hand selection can confirm zero cards with selectCards([]), but it cannot be skipped."
            );
        }
        return CombatApiResult.error(
            "selection_not_skippable",
            "The current hand selection must be completed."
        );
    }

    private static CombatApiResult cannotSkipGridSelection() {
        GridCardSelectScreen screen = AbstractDungeon.gridSelectScreen;
        if (screen == null || screen.targetGroup == null || screen.isJustForConfirming) {
            return CombatApiResult.error(
                "no_card_selection_pending",
                "The active grid is not waiting for a card selection."
            );
        }
        if (screen.anyNumber) {
            return CombatApiResult.error(
                "selection_not_skippable",
                "This grid selection can confirm zero cards with selectCards([]), but it cannot be skipped."
            );
        }
        return CombatApiResult.error(
            "selection_not_skippable",
            "The current grid selection must be completed."
        );
    }

    private static boolean isCombatCardReward(CardRewardScreen screen) {
        if (screen == null || screen.rewardGroup == null) {
            return false;
        }
        boolean discovery = ReflectionHacks.getPrivate(screen, CardRewardScreen.class, "discovery");
        boolean chooseOne = ReflectionHacks.getPrivate(screen, CardRewardScreen.class, "chooseOne");
        return discovery || chooseOne;
    }

    private static boolean isCombatRoom() {
        return AbstractDungeon.getCurrRoom() != null
            && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
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
