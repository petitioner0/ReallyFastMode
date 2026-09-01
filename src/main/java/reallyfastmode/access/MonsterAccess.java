package reallyfastmode.access;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.powers.AbstractPower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Reads current-combat monster state. */
public final class MonsterAccess {
    private MonsterAccess() {
    }

    public static List<AbstractMonster> monsters() {
        MonsterGroup group = currentGroup();
        return group == null ? Collections.<AbstractMonster>emptyList() : immutableCopy(group.monsters);
    }

    public static List<AbstractMonster> livingMonsters() {
        List<AbstractMonster> result = new ArrayList<AbstractMonster>();
        for (AbstractMonster monster : monsters()) {
            if (monster != null && monster.currentHealth > 0 && !monster.isDeadOrEscaped()) {
                result.add(monster);
            }
        }
        return result.isEmpty()
            ? Collections.<AbstractMonster>emptyList()
            : Collections.unmodifiableList(result);
    }

    public static String id(AbstractMonster monster) {
        return monster(monster).id;
    }

    public static String name(AbstractMonster monster) {
        return monster(monster).name;
    }

    public static int hp(AbstractMonster monster) {
        return monster(monster).currentHealth;
    }

    public static int maxHp(AbstractMonster monster) {
        return monster(monster).maxHealth;
    }

    public static int block(AbstractMonster monster) {
        return monster(monster).currentBlock;
    }

    public static List<AbstractPower> powers(AbstractMonster monster) {
        return immutableCopy(monster(monster).powers);
    }

    public static AbstractMonster.EnemyType type(AbstractMonster monster) {
        return monster(monster).type;
    }

    public static AbstractMonster.Intent intent(AbstractMonster monster) {
        return monster(monster).intent;
    }

    public static List<DamageInfo> damage(AbstractMonster monster) {
        return immutableCopy(monster(monster).damage);
    }

    public static int intentDamage(AbstractMonster monster) {
        return privateField(monster(monster), "intentDmg");
    }

    public static int intentBaseDamage(AbstractMonster monster) {
        return privateField(monster(monster), "intentBaseDmg");
    }

    public static int intentMultiAmount(AbstractMonster monster) {
        return privateField(monster(monster), "intentMultiAmt");
    }

    public static boolean multiDamageIntent(AbstractMonster monster) {
        return privateField(monster(monster), "isMultiDmg");
    }

    private static MonsterGroup currentGroup() {
        return AbstractDungeon.getCurrRoom() == null ? null : AbstractDungeon.getCurrRoom().monsters;
    }

    private static AbstractMonster monster(AbstractMonster monster) {
        return Objects.requireNonNull(monster, "monster");
    }

    private static <T> T privateField(AbstractMonster monster, String fieldName) {
        try {
            return ReflectionHacks.getPrivate(monster, AbstractMonster.class, fieldName);
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                "Unable to read AbstractMonster." + fieldName + "; the STS field layout may be unsupported.",
                exception
            );
        }
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
