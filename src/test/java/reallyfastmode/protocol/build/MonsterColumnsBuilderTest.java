package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.monsters.AbstractMonster;
import org.junit.Test;
import reallyfastmode.protocol.MonsterProtocol;
import reallyfastmode.protocol.VanillaMonsterCatalog;

import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class MonsterColumnsBuilderTest {
    @Test
    public void rejectsNullAndEmptyLists() {
        assertThrows(IllegalArgumentException.class,
            () -> MonsterColumnsBuilder.build(null));
        assertThrows(IllegalArgumentException.class,
            () -> MonsterColumnsBuilder.build(Collections.<AbstractMonster>emptyList()));
    }

    @Test
    public void unknownMonsterIdFollowsCurrentCatalogTail() {
        assertEquals(VanillaMonsterCatalog.values().length,
            MonsterProtocol.UNKNOWN_MONSTER_WIRE_ID);
    }

    @Test
    public void buildsFlattenedColumnsAndNormalizesIntents() {
        FakeSource source = new FakeSource(2);
        source.instanceId = new int[]{3, 8};
        source.monsterWireId = new int[]{1, MonsterProtocol.UNKNOWN_MONSTER_WIRE_ID};
        source.hp = new int[]{40, 0};
        source.maxHp = new int[]{50, 4095};
        source.block = new int[]{12, 0};
        source.powerAmount = new int[][]{{2, -1}, {}};
        source.attack = new boolean[]{true, false};
        source.intentDamage = new int[]{13, -1};
        source.multi = new boolean[]{false, false};
        source.intentMultiAmount = new int[]{-1, -1};

        MonsterColumns columns = MonsterColumnsBuilder.buildFromSource(2, source);

        assertEquals(2, columns.size);
        assertEquals(2, columns.totalPowerEntries);
        assertArrayEquals(new byte[]{3, 8}, columns.instanceId4Bit);
        assertArrayEquals(new int[]{1, 66}, columns.monsterWireId);
        assertArrayEquals(new int[]{2, 0}, columns.powerEntryCount);
        assertArrayEquals(new int[]{0, 0}, columns.powerWireId);
        assertArrayEquals(new int[]{2, -1}, columns.powerAmount);
        assertArrayEquals(new int[]{17, 17}, columns.intentWireId);
        assertArrayEquals(new int[]{13, 0}, columns.intentDamage);
        assertArrayEquals(new int[]{1, 0}, columns.intentMultiAmount);
    }

    @Test
    public void preservesMultiAttackAmount() {
        FakeSource source = new FakeSource(1);
        source.attack[0] = true;
        source.intentDamage[0] = 7;
        source.multi[0] = true;
        source.intentMultiAmount[0] = 4;

        MonsterColumns columns = MonsterColumnsBuilder.buildFromSource(1, source);

        assertArrayEquals(new int[]{7}, columns.intentDamage);
        assertArrayEquals(new int[]{4}, columns.intentMultiAmount);
    }

    @Test
    public void rejectsInvalidCaptureValues() {
        FakeSource duplicates = new FakeSource(2);
        duplicates.instanceId = new int[]{4, 4};
        assertThrows(IllegalArgumentException.class,
            () -> MonsterColumnsBuilder.buildFromSource(2, duplicates));

        FakeSource tooManyPowers = new FakeSource(1);
        tooManyPowers.powerAmount = new int[][]{new int[32]};
        assertThrows(IllegalArgumentException.class,
            () -> MonsterColumnsBuilder.buildFromSource(1, tooManyPowers));

        FakeSource hpOverflow = new FakeSource(1);
        hpOverflow.hp[0] = 4096;
        assertThrows(IllegalArgumentException.class,
            () -> MonsterColumnsBuilder.buildFromSource(1, hpOverflow));

        FakeSource damageOverflow = new FakeSource(1);
        damageOverflow.attack[0] = true;
        damageOverflow.intentDamage[0] = 256;
        assertThrows(IllegalArgumentException.class,
            () -> MonsterColumnsBuilder.buildFromSource(1, damageOverflow));

        assertThrows(IllegalArgumentException.class,
            () -> MonsterColumnsBuilder.buildFromSource(17, new FakeSource(17)));
    }

    private static final class FakeSource implements MonsterColumnsBuilder.Source {
        private int[] instanceId;
        private int[] monsterWireId;
        private int[] hp;
        private int[] maxHp;
        private int[] block;
        private int[][] powerAmount;
        private boolean[] attack;
        private int[] intentDamage;
        private boolean[] multi;
        private int[] intentMultiAmount;

        private FakeSource(int size) {
            instanceId = new int[size];
            monsterWireId = new int[size];
            hp = new int[size];
            maxHp = new int[size];
            block = new int[size];
            powerAmount = new int[size][0];
            attack = new boolean[size];
            intentDamage = new int[size];
            multi = new boolean[size];
            intentMultiAmount = new int[size];
        }

        @Override
        public int instanceId(int monsterIndex) {
            return instanceId[monsterIndex];
        }

        @Override
        public int monsterWireId(int monsterIndex) {
            return monsterWireId[monsterIndex];
        }

        @Override
        public int hp(int monsterIndex) {
            return hp[monsterIndex];
        }

        @Override
        public int maxHp(int monsterIndex) {
            return maxHp[monsterIndex];
        }

        @Override
        public int block(int monsterIndex) {
            return block[monsterIndex];
        }

        @Override
        public int powerEntryCount(int monsterIndex) {
            return powerAmount[monsterIndex].length;
        }

        @Override
        public int powerAmount(int monsterIndex, int powerIndex) {
            return powerAmount[monsterIndex][powerIndex];
        }

        @Override
        public boolean attackIntent(int monsterIndex) {
            return attack[monsterIndex];
        }

        @Override
        public int intentDamage(int monsterIndex) {
            return intentDamage[monsterIndex];
        }

        @Override
        public boolean multiDamageIntent(int monsterIndex) {
            return multi[monsterIndex];
        }

        @Override
        public int intentMultiAmount(int monsterIndex) {
            return intentMultiAmount[monsterIndex];
        }
    }
}
