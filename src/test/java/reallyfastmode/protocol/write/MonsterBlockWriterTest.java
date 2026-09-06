package reallyfastmode.protocol.write;

import org.junit.Test;
import reallyfastmode.protocol.build.MonsterColumns;
import reallyfastmode.protocol.io.BitWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class MonsterBlockWriterTest {
    @Test
    public void writesEmptyBlock() {
        BitWriter out = new BitWriter(0);

        MonsterBlockWriter.write(out, new MonsterColumns(0));

        assertEquals(5, out.bitPosition());
        assertArrayEquals(new byte[]{0}, out.toByteArray());
    }

    @Test
    public void writesGoldenNestedSoaBlock() {
        MonsterColumns monsters = new MonsterColumns(1, 1);
        monsters.instanceId4Bit[0] = 0xA;
        monsters.monsterWireId[0] = 0x42;
        monsters.hp[0] = 0xABC;
        monsters.maxHp[0] = 0xDEF;
        monsters.block[0] = 0x1234;
        monsters.powerEntryCount[0] = 1;
        monsters.powerWireId[0] = 0;
        monsters.powerAmount[0] = -1;
        monsters.intentWireId[0] = 15;
        monsters.intentDamage[0] = 0x7F;
        monsters.intentMultiAmount[0] = 2;
        BitWriter out = new BitWriter(0);

        MonsterBlockWriter.write(out, monsters);

        assertEquals(106, out.bitPosition());
        assertArrayEquals(new byte[]{
            0x0D, 0x21, 0x55, (byte) 0xE6, (byte) 0xF7, (byte) 0x89, 0x1A,
            0x04, 0x00, 0x3F, (byte) 0xFF, (byte) 0xDF, (byte) 0xC0, (byte) 0x80
        }, out.toByteArray());
    }

    @Test
    public void writesPowerCountsBeforeFlattenedPowerColumns() {
        MonsterColumns monsters = new MonsterColumns(2, 3);
        monsters.instanceId4Bit[0] = 1;
        monsters.instanceId4Bit[1] = 2;
        monsters.monsterWireId[0] = 10;
        monsters.monsterWireId[1] = 20;
        monsters.hp[0] = 100;
        monsters.hp[1] = 200;
        monsters.maxHp[0] = 101;
        monsters.maxHp[1] = 201;
        monsters.powerEntryCount[0] = 1;
        monsters.powerEntryCount[1] = 2;
        monsters.powerWireId[0] = 11;
        monsters.powerWireId[1] = 22;
        monsters.powerWireId[2] = 33;
        monsters.powerAmount[0] = -1;
        monsters.powerAmount[1] = 2;
        monsters.powerAmount[2] = -3;
        BitWriter out = new BitWriter(0);

        MonsterBlockWriter.write(out, monsters);

        byte[] bytes = out.toByteArray();
        int offset = 0;
        assertEquals(2, readBits(bytes, offset, 5));
        offset += 5 + 2 * 4 + 2 * 8 + 2 * 12 + 2 * 12 + 2 * 16;
        assertEquals(1, readBits(bytes, offset, 5));
        offset += 5;
        assertEquals(2, readBits(bytes, offset, 5));
        offset += 5;
        assertEquals(11, readBits(bytes, offset, 12));
        offset += 12;
        assertEquals(22, readBits(bytes, offset, 12));
        offset += 12;
        assertEquals(33, readBits(bytes, offset, 12));
        offset += 12;
        assertEquals(0xFFF, readBits(bytes, offset, 12));
        offset += 12;
        assertEquals(2, readBits(bytes, offset, 12));
        offset += 12;
        assertEquals(0xFFD, readBits(bytes, offset, 12));
    }

    @Test
    public void acceptsAllNumericBoundaries() {
        MonsterColumns monsters = oneMonster(0);
        monsters.monsterWireId[0] = 255;
        monsters.hp[0] = 4095;
        monsters.maxHp[0] = 4095;
        monsters.block[0] = 65535;
        monsters.intentWireId[0] = 15;
        monsters.intentDamage[0] = 255;
        monsters.intentMultiAmount[0] = 255;

        MonsterBlockWriter.write(new BitWriter(0), monsters);

        MonsterColumns withPowers = oneMonster(2);
        withPowers.powerEntryCount[0] = 2;
        withPowers.powerWireId[0] = 0;
        withPowers.powerWireId[1] = 4095;
        withPowers.powerAmount[0] = -2048;
        withPowers.powerAmount[1] = 2047;
        MonsterBlockWriter.write(new BitWriter(0), withPowers);
    }

    @Test
    public void rejectsNumericOverflowBeforeWriting() {
        assertRejected(column -> column.hp[0] = 4096);
        assertRejected(column -> column.maxHp[0] = -1);
        assertRejected(column -> column.monsterWireId[0] = 256);
        assertRejected(column -> column.intentDamage[0] = 256);
        assertRejected(column -> column.intentMultiAmount[0] = 256);
        assertRejected(column -> column.intentWireId[0] = 16);

        MonsterColumns lowPower = oneMonster(1);
        lowPower.powerEntryCount[0] = 1;
        lowPower.powerAmount[0] = -2049;
        assertThrows(IllegalArgumentException.class,
            () -> MonsterBlockWriter.write(new BitWriter(0), lowPower));

        MonsterColumns highPower = oneMonster(1);
        highPower.powerEntryCount[0] = 1;
        highPower.powerAmount[0] = 2048;
        assertThrows(IllegalArgumentException.class,
            () -> MonsterBlockWriter.write(new BitWriter(0), highPower));
    }

    @Test
    public void rejectsDuplicateIdsAndInconsistentPowerShapeBeforeWriting() {
        MonsterColumns duplicates = new MonsterColumns(2);
        duplicates.instanceId4Bit[0] = 3;
        duplicates.instanceId4Bit[1] = 3;
        assertThrows(IllegalArgumentException.class,
            () -> MonsterBlockWriter.write(new BitWriter(0), duplicates));

        MonsterColumns inconsistent = oneMonster(1);
        inconsistent.powerEntryCount[0] = 0;
        BitWriter out = new BitWriter(0);
        out.writeBit(true);
        assertThrows(IllegalArgumentException.class,
            () -> MonsterBlockWriter.write(out, inconsistent));
        assertEquals(1, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0x80}, out.toByteArray());
    }

    @Test
    public void rejectsSeventeenthMonsterAndThirtySecondPower() {
        assertThrows(IllegalArgumentException.class, () -> {
            MonsterColumns monsters = new MonsterColumns(17);
            MonsterBlockWriter.write(new BitWriter(0), monsters);
        });

        MonsterColumns monsters = oneMonster(31);
        monsters.powerEntryCount[0] = 31;
        MonsterBlockWriter.write(new BitWriter(0), monsters);
        monsters.powerEntryCount[0] = 32;
        assertThrows(IllegalArgumentException.class,
            () -> MonsterBlockWriter.write(new BitWriter(0), monsters));
    }

    private static MonsterColumns oneMonster(int powers) {
        MonsterColumns monsters = new MonsterColumns(1, powers);
        monsters.instanceId4Bit[0] = 0;
        return monsters;
    }

    private static void assertRejected(Mutation mutation) {
        MonsterColumns monsters = oneMonster(0);
        mutation.apply(monsters);
        BitWriter out = new BitWriter(0);
        out.writeBits(0b101, 3);
        assertThrows(IllegalArgumentException.class,
            () -> MonsterBlockWriter.write(out, monsters));
        assertEquals(3, out.bitPosition());
    }

    private static int readBits(byte[] bytes, int bitOffset, int bitCount) {
        int value = 0;
        for (int i = 0; i < bitCount; i++) {
            int absoluteBit = bitOffset + i;
            int bit = (bytes[absoluteBit >>> 3] >>> (7 - (absoluteBit & 7))) & 1;
            value = (value << 1) | bit;
        }
        return value;
    }

    private interface Mutation {
        void apply(MonsterColumns columns);
    }
}
