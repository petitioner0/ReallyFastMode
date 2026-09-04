package reallyfastmode.access;

import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.stances.AbstractStance;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Reads player state without retaining a player snapshot. */
public final class PlayerAccess {
    private PlayerAccess() {
    }

    private static AbstractPlayer requiredPlayer() {
        if (AbstractDungeon.player == null) {
            throw new IllegalStateException("No current player is available.");
        }
        return AbstractDungeon.player;
    }

    public static String id() {
        return requiredPlayer().id;
    }

    public static AbstractPlayer.PlayerClass playerClass() {
        return requiredPlayer().chosenClass;
    }

    public static int hp() {
        return requiredPlayer().currentHealth;
    }

    public static int maxHp() {
        return requiredPlayer().maxHealth;
    }

    public static int block() {
        return requiredPlayer().currentBlock;
    }

    public static int energy() {
        requiredPlayer();
        return EnergyPanel.totalCount;
    }

    public static int gold() {
        return requiredPlayer().gold;
    }

    public static int gameHandSize() {
        return requiredPlayer().gameHandSize;
    }

    public static int masterHandSize() {
        return requiredPlayer().masterHandSize;
    }

    public static int potionSlots() {
        return requiredPlayer().potionSlots;
    }

    public static int maxOrbs() {
        return requiredPlayer().maxOrbs;
    }

    public static int masterMaxOrbs() {
        return requiredPlayer().masterMaxOrbs;
    }

    public static List<AbstractPower> powers() {
        return AbstractDungeon.player == null
            ? Collections.<AbstractPower>emptyList()
            : immutableCopy(AbstractDungeon.player.powers);
    }

    public static List<AbstractRelic> relics() {
        return AbstractDungeon.player == null
            ? Collections.<AbstractRelic>emptyList()
            : immutableCopy(AbstractDungeon.player.relics);
    }

    public static int relicCounter(AbstractRelic relic) {
        return Objects.requireNonNull(relic, "relic").counter;
    }

    public static List<AbstractOrb> orbs() {
        return AbstractDungeon.player == null
            ? Collections.<AbstractOrb>emptyList()
            : immutableCopy(AbstractDungeon.player.orbs);
    }

    public static AbstractStance stance() {
        return requiredPlayer().stance;
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
