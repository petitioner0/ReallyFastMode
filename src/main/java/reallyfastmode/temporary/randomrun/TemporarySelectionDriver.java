package reallyfastmode.temporary.randomrun;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import reallyfastmode.patches.access.PrivateFieldAccess;

import java.util.ArrayList;
import java.util.List;

/** Minimal left-to-right policy for temporary automated card selections. */
final class TemporarySelectionDriver {
    private TemporarySelectionDriver() {
    }

    static void handleHandSelection() {
        HandCardSelectScreen screen = AbstractDungeon.handCardSelectScreen;
        if (screen == null || AbstractDungeon.player == null) {
            throw new IllegalStateException("Hand selection is unavailable.");
        }

        boolean flexible = PrivateFieldAccess.handSelectAnyNumber(screen) || screen.upTo;
        int count = minimumHandSelectionCount(
            screen.canPickZero,
            flexible,
            screen.numCardsToSelect
        );

        List<AbstractCard> candidates = new ArrayList<AbstractCard>();
        candidates.addAll(AbstractDungeon.player.hand.group);
        for (AbstractCard selected : screen.selectedCards.group) {
            if (!candidates.contains(selected)) {
                candidates.add(selected);
            }
        }
        requireCandidates(count, candidates.size(), "hand");

        List<AbstractCard> previouslySelected =
            new ArrayList<AbstractCard>(screen.selectedCards.group);
        screen.selectedCards.clear();
        for (AbstractCard selected : previouslySelected) {
            if (!AbstractDungeon.player.hand.contains(selected)) {
                AbstractDungeon.player.hand.addToTop(selected);
            }
        }

        for (int index = 0; index < count; ++index) {
            AbstractCard card = candidates.get(index);
            AbstractDungeon.player.hand.removeCard(card);
            card.unhover();
            card.untip();
            screen.selectedCards.addToBottom(card);
        }
        AbstractDungeon.player.hand.refreshHandLayout();
        AbstractDungeon.closeCurrentScreen();

        if (PrivateFieldAccess.handSelectForTransform(screen)
            && screen.selectedCards.size() == 1) {
            AbstractCard selected = screen.selectedCards.getBottomCard();
            if (isCombatRoom()) {
                AbstractDungeon.srcTransformCard(selected);
            } else {
                AbstractDungeon.transformCard(selected);
            }
            screen.selectedCards.clear();
        }
    }

    static void handleGridSelection() {
        GridCardSelectScreen screen = AbstractDungeon.gridSelectScreen;
        if (screen == null || screen.targetGroup == null) {
            throw new IllegalStateException("Grid selection is unavailable.");
        }

        if (screen.isJustForConfirming || screen.confirmScreenUp) {
            screen.confirmButton.show();
            screen.confirmButton.isDisabled = false;
            screen.confirmButton.hb.clicked = true;
            return;
        }

        int count = minimumGridSelectionCount(
            screen.anyNumber,
            screen.forClarity,
            PrivateFieldAccess.gridSelectRequiredCount(screen)
        );
        requireCandidates(count, screen.targetGroup.group.size(), "grid");

        for (AbstractCard selected : new ArrayList<AbstractCard>(screen.selectedCards)) {
            selected.stopGlowing();
        }
        screen.selectedCards.clear();
        for (int index = 0; index < count; ++index) {
            AbstractCard card = screen.targetGroup.group.get(index);
            card.unhover();
            card.untip();
            card.stopGlowing();
            screen.selectedCards.add(card);
        }
        PrivateFieldAccess.setGridSelectAmount(screen, count);
        AbstractDungeon.overlayMenu.cancelButton.hide();
        AbstractDungeon.closeCurrentScreen();

        if (screen.forTransform && count == 1 && isCombatRoom()) {
            AbstractDungeon.srcTransformCard(screen.selectedCards.get(0));
            screen.selectedCards.clear();
        }
    }

    static int minimumHandSelectionCount(
        boolean canPickZero,
        boolean flexible,
        int requiredCount
    ) {
        requireNonnegative(requiredCount);
        if (canPickZero || requiredCount == 0) {
            return 0;
        }
        return flexible ? 1 : requiredCount;
    }

    static int minimumGridSelectionCount(
        boolean anyNumber,
        boolean forClarity,
        int requiredCount
    ) {
        requireNonnegative(requiredCount);
        if (anyNumber) {
            return 0;
        }
        return forClarity ? 1 : requiredCount;
    }

    private static boolean isCombatRoom() {
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        return room != null && room.phase == AbstractRoom.RoomPhase.COMBAT;
    }

    private static void requireCandidates(int required, int available, String kind) {
        if (required > available) {
            throw new IllegalStateException(
                "Forced " + kind + " selection needs " + required
                    + " card(s), but only " + available + " are available."
            );
        }
    }

    private static void requireNonnegative(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Selection count must be nonnegative.");
        }
    }
}
