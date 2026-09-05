package reallyfastmode.protocol.write;

import reallyfastmode.protocol.MonsterProtocol;
import reallyfastmode.protocol.build.MonsterColumns;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes {@link MonsterColumns} in the fixed Monster SoA wire order. */
public final class MonsterBlockWriter {
    private MonsterBlockWriter() {
    }

    public static void write(BitWriter out, MonsterColumns monsters) {
        Objects.requireNonNull(out, "out");
        validate(Objects.requireNonNull(monsters, "monsters"));

        int count = monsters.size;
        out.writeBits(count, MonsterProtocol.MONSTER_COUNT_BITS);
        for (int i = 0; i < count; i++) {
            out.writeBits(monsters.instanceId4Bit[i] & 0xFF, MonsterProtocol.INSTANCE_ID_BITS);
        }
        for (int i = 0; i < count; i++) {
            out.writeBits(monsters.monsterWireId[i], MonsterProtocol.MONSTER_WIRE_ID_BITS);
        }
        for (int i = 0; i < count; i++) {
            out.writeBits(monsters.hp[i], MonsterProtocol.HP_BITS);
        }
        for (int i = 0; i < count; i++) {
            out.writeBits(monsters.maxHp[i], MonsterProtocol.HP_BITS);
        }
        for (int i = 0; i < count; i++) {
            out.writeBits(monsters.block[i], MonsterProtocol.BLOCK_BITS);
        }
        for (int i = 0; i < count; i++) {
            out.writeBits(monsters.powerEntryCount[i], MonsterProtocol.POWER_ENTRY_COUNT_BITS);
        }
        for (int i = 0; i < monsters.totalPowerEntries; i++) {
            out.writeBits(monsters.powerWireId[i], MonsterProtocol.POWER_WIRE_ID_BITS);
        }
        for (int i = 0; i < monsters.totalPowerEntries; i++) {
            out.writeBits(monsters.powerAmount[i], MonsterProtocol.POWER_AMOUNT_BITS);
        }
        for (int i = 0; i < count; i++) {
            out.writeBits(monsters.intentWireId[i], MonsterProtocol.INTENT_WIRE_ID_BITS);
        }
        for (int i = 0; i < count; i++) {
            out.writeBits(monsters.intentDamage[i], MonsterProtocol.INTENT_DAMAGE_BITS);
        }
        for (int i = 0; i < count; i++) {
            out.writeBits(monsters.intentMultiAmount[i],
                MonsterProtocol.INTENT_MULTI_AMOUNT_BITS);
        }
    }

    private static void validate(MonsterColumns monsters) {
        if (monsters.size < 0 || monsters.size > MonsterProtocol.MAX_MONSTERS) {
            throw new IllegalArgumentException("monster count=" + monsters.size);
        }
        if (monsters.totalPowerEntries < 0) {
            throw new IllegalArgumentException(
                "totalPowerEntries=" + monsters.totalPowerEntries
            );
        }

        requireLength("instanceId4Bit", monsters.instanceId4Bit, monsters.size);
        requireLength("monsterWireId", monsters.monsterWireId, monsters.size);
        requireLength("hp", monsters.hp, monsters.size);
        requireLength("maxHp", monsters.maxHp, monsters.size);
        requireLength("block", monsters.block, monsters.size);
        requireLength("powerEntryCount", monsters.powerEntryCount, monsters.size);
        requireLength("powerWireId", monsters.powerWireId, monsters.totalPowerEntries);
        requireLength("powerAmount", monsters.powerAmount, monsters.totalPowerEntries);
        requireLength("intentWireId", monsters.intentWireId, monsters.size);
        requireLength("intentDamage", monsters.intentDamage, monsters.size);
        requireLength("intentMultiAmount", monsters.intentMultiAmount, monsters.size);

        boolean[] seenInstanceIds = new boolean[MonsterProtocol.MAX_MONSTERS];
        int countedPowerEntries = 0;
        for (int i = 0; i < monsters.size; i++) {
            int instanceId = monsters.instanceId4Bit[i] & 0xFF;
            requireUnsigned("instanceId4Bit[" + i + "]", instanceId,
                MonsterProtocol.INSTANCE_ID_BITS);
            if (seenInstanceIds[instanceId]) {
                throw new IllegalArgumentException("duplicate instanceId=" + instanceId);
            }
            seenInstanceIds[instanceId] = true;

            requireUnsigned("monsterWireId[" + i + "]", monsters.monsterWireId[i],
                MonsterProtocol.MONSTER_WIRE_ID_BITS);
            requireUnsigned("hp[" + i + "]", monsters.hp[i], MonsterProtocol.HP_BITS);
            requireUnsigned("maxHp[" + i + "]", monsters.maxHp[i], MonsterProtocol.HP_BITS);
            requireUnsigned("block[" + i + "]", monsters.block[i], MonsterProtocol.BLOCK_BITS);
            requireUnsigned("powerEntryCount[" + i + "]", monsters.powerEntryCount[i],
                MonsterProtocol.POWER_ENTRY_COUNT_BITS);
            countedPowerEntries += monsters.powerEntryCount[i];
            requireUnsigned("intentWireId[" + i + "]", monsters.intentWireId[i],
                MonsterProtocol.INTENT_WIRE_ID_BITS);
            requireUnsigned("intentDamage[" + i + "]", monsters.intentDamage[i],
                MonsterProtocol.INTENT_DAMAGE_BITS);
            requireUnsigned("intentMultiAmount[" + i + "]", monsters.intentMultiAmount[i],
                MonsterProtocol.INTENT_MULTI_AMOUNT_BITS);
        }
        if (countedPowerEntries != monsters.totalPowerEntries) {
            throw new IllegalArgumentException(
                "powerEntryCount sum=" + countedPowerEntries
                    + " but totalPowerEntries=" + monsters.totalPowerEntries
            );
        }

        for (int i = 0; i < monsters.totalPowerEntries; i++) {
            requireUnsigned("powerWireId[" + i + "]", monsters.powerWireId[i],
                MonsterProtocol.POWER_WIRE_ID_BITS);
            requireSigned("powerAmount[" + i + "]", monsters.powerAmount[i],
                MonsterProtocol.POWER_AMOUNT_BITS);
        }
    }

    private static void requireLength(String name, byte[] values, int expected) {
        if (values == null || values.length != expected) {
            throw new IllegalArgumentException(
                name + " length=" + (values == null ? "null" : values.length)
                    + ", expected=" + expected
            );
        }
    }

    private static void requireLength(String name, int[] values, int expected) {
        if (values == null || values.length != expected) {
            throw new IllegalArgumentException(
                name + " length=" + (values == null ? "null" : values.length)
                    + ", expected=" + expected
            );
        }
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
}
