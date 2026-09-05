package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import reallyfastmode.access.MonsterAccess;
import reallyfastmode.protocol.MonsterProtocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Captures current STS monster objects into a semantic SoA snapshot. */
public final class MonsterColumnsBuilder {
    private MonsterColumnsBuilder() {
    }

    public static MonsterColumns build(List<AbstractMonster> monsters) {
        if (monsters == null || monsters.isEmpty()) {
            throw new IllegalArgumentException("monsters must not be null or empty");
        }
        if (monsters.size() > MonsterProtocol.MAX_MONSTERS) {
            throw new IllegalArgumentException("monster count=" + monsters.size());
        }
        return buildFromSource(monsters.size(), new StsSource(monsters));
    }

    static MonsterColumns buildFromSource(int size, Source source) {
        Objects.requireNonNull(source, "source");
        if (size < 0 || size > MonsterProtocol.MAX_MONSTERS) {
            throw new IllegalArgumentException("monster count=" + size);
        }

        int totalPowerEntries = 0;
        int[] powerEntryCounts = new int[size];
        for (int i = 0; i < size; i++) {
            int count = source.powerEntryCount(i);
            requireUnsigned("powerEntryCount[" + i + "]", count,
                MonsterProtocol.POWER_ENTRY_COUNT_BITS);
            powerEntryCounts[i] = count;
            totalPowerEntries += count;
        }

        MonsterColumns result = new MonsterColumns(size, totalPowerEntries);
        boolean[] seenInstanceIds = new boolean[MonsterProtocol.MAX_MONSTERS];
        int powerIndex = 0;

        for (int i = 0; i < size; i++) {
            int instanceId = source.instanceId(i);
            requireUnsigned("instanceId[" + i + "]", instanceId,
                MonsterProtocol.INSTANCE_ID_BITS);
            if (seenInstanceIds[instanceId]) {
                throw new IllegalArgumentException("duplicate instanceId=" + instanceId);
            }
            seenInstanceIds[instanceId] = true;

            int monsterWireId = source.monsterWireId(i);
            int hp = source.hp(i);
            int maxHp = source.maxHp(i);
            int block = source.block(i);
            requireUnsigned("monsterWireId[" + i + "]", monsterWireId,
                MonsterProtocol.MONSTER_WIRE_ID_BITS);
            requireUnsigned("hp[" + i + "]", hp, MonsterProtocol.HP_BITS);
            requireUnsigned("maxHp[" + i + "]", maxHp, MonsterProtocol.HP_BITS);
            requireUnsigned("block[" + i + "]", block, MonsterProtocol.BLOCK_BITS);

            result.instanceId4Bit[i] = (byte) instanceId;
            result.monsterWireId[i] = monsterWireId;
            result.hp[i] = hp;
            result.maxHp[i] = maxHp;
            result.block[i] = block;

            int powerCount = powerEntryCounts[i];
            result.powerEntryCount[i] = powerCount;
            for (int j = 0; j < powerCount; j++) {
                int powerAmount = source.powerAmount(i, j);
                requireSigned("powerAmount[" + powerIndex + "]", powerAmount,
                    MonsterProtocol.POWER_AMOUNT_BITS);
                result.powerWireId[powerIndex] = MonsterProtocol.UNKNOWN_POWER_WIRE_ID;
                result.powerAmount[powerIndex] = powerAmount;
                powerIndex++;
            }

            result.intentWireId[i] = MonsterProtocol.UNKNOWN_INTENT_WIRE_ID;
            if (source.attackIntent(i)) {
                int damage = source.intentDamage(i);
                int hitCount = source.multiDamageIntent(i) ? source.intentMultiAmount(i) : 1;
                requireUnsigned("intentDamage[" + i + "]", damage,
                    MonsterProtocol.INTENT_DAMAGE_BITS);
                requireUnsigned("intentMultiAmount[" + i + "]", hitCount,
                    MonsterProtocol.INTENT_MULTI_AMOUNT_BITS);
                if (hitCount == 0) {
                    throw new IllegalArgumentException("intentMultiAmount[" + i + "]=0");
                }
                result.intentDamage[i] = damage;
                result.intentMultiAmount[i] = hitCount;
            }
        }
        return result;
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }

    private static void requireSigned(String name, int value, int bits) {
        int min = -(1 << (bits - 1));
        int max = (1 << (bits - 1)) - 1;
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                name + "=" + value + " outside " + min + "-" + max
            );
        }
    }

    interface Source {
        int instanceId(int monsterIndex);

        int monsterWireId(int monsterIndex);

        int hp(int monsterIndex);

        int maxHp(int monsterIndex);

        int block(int monsterIndex);

        int powerEntryCount(int monsterIndex);

        int powerAmount(int monsterIndex, int powerIndex);

        boolean attackIntent(int monsterIndex);

        int intentDamage(int monsterIndex);

        boolean multiDamageIntent(int monsterIndex);

        int intentMultiAmount(int monsterIndex);
    }

    private static final class StsSource implements Source {
        private final List<AbstractMonster> monsters;
        private final List<List<AbstractPower>> powers;

        private StsSource(List<AbstractMonster> monsters) {
            this.monsters = new ArrayList<AbstractMonster>(monsters.size());
            this.powers = new ArrayList<List<AbstractPower>>(monsters.size());
            for (int i = 0; i < monsters.size(); i++) {
                AbstractMonster monster = Objects.requireNonNull(
                    monsters.get(i), "monsters[" + i + "]"
                );
                this.monsters.add(monster);
                List<AbstractPower> capturedPowers = MonsterAccess.powers(monster);
                this.powers.add(capturedPowers.isEmpty()
                    ? Collections.<AbstractPower>emptyList()
                    : capturedPowers);
            }
        }

        @Override
        public int instanceId(int monsterIndex) {
            return MonsterAccess.instanceId(monster(monsterIndex));
        }

        @Override
        public int monsterWireId(int monsterIndex) {
            return MonsterAccess.wireId(monster(monsterIndex));
        }

        @Override
        public int hp(int monsterIndex) {
            return MonsterAccess.hp(monster(monsterIndex));
        }

        @Override
        public int maxHp(int monsterIndex) {
            return MonsterAccess.maxHp(monster(monsterIndex));
        }

        @Override
        public int block(int monsterIndex) {
            return MonsterAccess.block(monster(monsterIndex));
        }

        @Override
        public int powerEntryCount(int monsterIndex) {
            return powers.get(monsterIndex).size();
        }

        @Override
        public int powerAmount(int monsterIndex, int powerIndex) {
            AbstractPower power = Objects.requireNonNull(
                powers.get(monsterIndex).get(powerIndex),
                "powers[" + monsterIndex + "][" + powerIndex + "]"
            );
            return power.amount;
        }

        @Override
        public boolean attackIntent(int monsterIndex) {
            AbstractMonster.Intent intent = MonsterAccess.intent(monster(monsterIndex));
            return intent == AbstractMonster.Intent.ATTACK
                || intent == AbstractMonster.Intent.ATTACK_BUFF
                || intent == AbstractMonster.Intent.ATTACK_DEBUFF
                || intent == AbstractMonster.Intent.ATTACK_DEFEND;
        }

        @Override
        public int intentDamage(int monsterIndex) {
            return MonsterAccess.intentDamage(monster(monsterIndex));
        }

        @Override
        public boolean multiDamageIntent(int monsterIndex) {
            return MonsterAccess.multiDamageIntent(monster(monsterIndex));
        }

        @Override
        public int intentMultiAmount(int monsterIndex) {
            return MonsterAccess.intentMultiAmount(monster(monsterIndex));
        }

        private AbstractMonster monster(int index) {
            return monsters.get(index);
        }
    }
}
