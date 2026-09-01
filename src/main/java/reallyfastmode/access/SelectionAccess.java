package reallyfastmode.access;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads GRID and CARD_REWARD candidates from the active vanilla screen.
 * HAND_SELECT candidates are the current hand and are exposed by CardAccess.
 */
public final class SelectionAccess {
    private SelectionAccess() {
    }

    public static AbstractDungeon.CurrentScreen screen() {
        return AbstractDungeon.screen;
    }

    public static List<AbstractCard> candidates() {
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID) {
            GridCardSelectScreen screen = AbstractDungeon.gridSelectScreen;
            return screen == null || screen.targetGroup == null
                ? Collections.<AbstractCard>emptyList()
                : immutableCopy(screen.targetGroup.group);
        }
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.CARD_REWARD) {
            CardRewardScreen screen = AbstractDungeon.cardRewardScreen;
            return screen == null ? Collections.<AbstractCard>emptyList() : immutableCopy(screen.rewardGroup);
        }
        return Collections.emptyList();
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
