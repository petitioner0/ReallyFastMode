package reallyfastmode.access;

import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.powers.AbstractPower;
import reallyfastmode.patches.access.MonsterInstanceIdPatches;
import reallyfastmode.patches.access.PrivateFieldAccess;
import reallyfastmode.protocol.MonsterProtocol;
import reallyfastmode.protocol.VanillaMonsterCatalog;

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

    /** Returns the vanilla wire id, or the protocol's fixed unknown id. */
    public static int wireId(AbstractMonster monster) {
        Integer wireId = VanillaMonsterCatalog.monsterIdToWireId.get(monster(monster).id);
        return wireId == null ? MonsterProtocol.UNKNOWN_MONSTER_WIRE_ID : wireId;
    }

    /** Returns this monster's room-local id in the inclusive range 0-15. */
    public static int instanceId(AbstractMonster monster) {
        return MonsterInstanceIdPatches.instanceId(monster(monster));
    }

    /** Returns the instance id in a byte container whose upper four bits are zero. */
    public static byte instanceId4Bit(AbstractMonster monster) {
        return (byte) (instanceId(monster) & 0xF);
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
        return monster(monster).getIntentDmg();
    }

    public static int intentBaseDamage(AbstractMonster monster) {
        return monster(monster).getIntentBaseDmg();
    }

    public static int intentMultiAmount(AbstractMonster monster) {
        return PrivateFieldAccess.monsterIntentMultiAmount(monster(monster));
    }

    public static boolean multiDamageIntent(AbstractMonster monster) {
        return PrivateFieldAccess.monsterHasMultiDamageIntent(monster(monster));
    }

    private static MonsterGroup currentGroup() {
        return AbstractDungeon.getCurrRoom() == null ? null : AbstractDungeon.getCurrRoom().monsters;
    }

    private static AbstractMonster monster(AbstractMonster monster) {
        return Objects.requireNonNull(monster, "monster");
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
