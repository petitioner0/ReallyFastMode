package reallyfastmode.api;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.List;

/** Shared identifier rules for combat commands and future state snapshots. */
public final class CombatIdentifiers {
    private static final String MONSTER_PREFIX = "monster:";

    private CombatIdentifiers() {
    }

    public static String cardId(AbstractCard card) {
        if (card == null || card.uuid == null) {
            return null;
        }
        return card.uuid.toString();
    }

    /**
     * Returns the stable target id for the monster's position in the current
     * combat roster, for example {@code monster:0}.
     */
    public static String targetId(AbstractMonster monster) {
        if (monster == null || AbstractDungeon.getCurrRoom() == null
            || AbstractDungeon.getCurrRoom().monsters == null) {
            return null;
        }

        List<AbstractMonster> monsters = AbstractDungeon.getCurrRoom().monsters.monsters;
        for (int index = 0; index < monsters.size(); ++index) {
            if (monsters.get(index) == monster) {
                return MONSTER_PREFIX + index;
            }
        }
        return null;
    }

    static TargetResolution resolveTarget(String requestedId) {
        if (requestedId == null || requestedId.trim().isEmpty()) {
            return TargetResolution.notFound();
        }
        if (AbstractDungeon.getCurrRoom() == null
            || AbstractDungeon.getCurrRoom().monsters == null) {
            return TargetResolution.notFound();
        }

        String normalized = requestedId.trim();
        List<AbstractMonster> monsters = AbstractDungeon.getCurrRoom().monsters.monsters;
        if (normalized.startsWith(MONSTER_PREFIX)) {
            String indexText = normalized.substring(MONSTER_PREFIX.length());
            try {
                int index = Integer.parseInt(indexText);
                if (index >= 0 && index < monsters.size()) {
                    return TargetResolution.found(monsters.get(index));
                }
                return TargetResolution.notFound();
            } catch (NumberFormatException ignored) {
                return TargetResolution.notFound();
            }
        }

        AbstractMonster match = null;
        for (AbstractMonster monster : monsters) {
            if (monster != null && normalized.equals(monster.id)) {
                if (match != null) {
                    return TargetResolution.ambiguous();
                }
                match = monster;
            }
        }
        return match == null ? TargetResolution.notFound() : TargetResolution.found(match);
    }

    static final class TargetResolution {
        private final AbstractMonster monster;
        private final boolean ambiguous;

        private TargetResolution(AbstractMonster monster, boolean ambiguous) {
            this.monster = monster;
            this.ambiguous = ambiguous;
        }

        static TargetResolution found(AbstractMonster monster) {
            return new TargetResolution(monster, false);
        }

        static TargetResolution notFound() {
            return new TargetResolution(null, false);
        }

        static TargetResolution ambiguous() {
            return new TargetResolution(null, true);
        }

        AbstractMonster getMonster() {
            return monster;
        }

        boolean isAmbiguous() {
            return ambiguous;
        }
    }
}
