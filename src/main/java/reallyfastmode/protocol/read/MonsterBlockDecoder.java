package reallyfastmode.protocol.read;

import reallyfastmode.protocol.MonsterIntentCatalog;
import reallyfastmode.protocol.MonsterProtocol;
import reallyfastmode.protocol.io.BitReader;

import java.util.Arrays;

final class MonsterBlockDecoder {
    private static final String[] INTENTS = new String[1 << MonsterProtocol.INTENT_WIRE_ID_BITS];

    static {
        Arrays.fill(INTENTS, WireIds.UNKNOWN_ID);
        for (MonsterIntentCatalog intent : MonsterIntentCatalog.values()) {
            if (intent != MonsterIntentCatalog.UNKNOWN) {
                INTENTS[intent.wireId] = intent.name();
            }
        }
    }

    private MonsterBlockDecoder() {
    }

    static PacketRecord.Monsters read(BitReader in) {
        int count = in.readBits(MonsterProtocol.MONSTER_COUNT_BITS);
        if (count > MonsterProtocol.MAX_MONSTERS) {
            throw new IllegalArgumentException("monster count=" + count);
        }

        int[] instanceId = readColumn(in, count, MonsterProtocol.INSTANCE_ID_BITS);
        requireUniqueInstanceIds(instanceId);
        String[] monsterId = WireIds.readColumn(in, count, MonsterProtocol.MONSTER_WIRE_ID_BITS, WireIds.MONSTERS);
        int[] hp = readColumn(in, count, MonsterProtocol.HP_BITS);
        int[] maxHp = readColumn(in, count, MonsterProtocol.HP_BITS);
        int[] block = readColumn(in, count, MonsterProtocol.BLOCK_BITS);
        int[] powerEntryCount = readColumn(in, count, MonsterProtocol.POWER_ENTRY_COUNT_BITS);

        int totalPowerEntries = 0;
        for (int powerCount : powerEntryCount) {
            totalPowerEntries += powerCount;
        }
        String[] powerId = WireIds.readColumn(in, totalPowerEntries,
            MonsterProtocol.POWER_WIRE_ID_BITS, WireIds.POWERS);
        int[] powerAmount = new int[totalPowerEntries];
        for (int i = 0; i < powerAmount.length; i++) {
            powerAmount[i] = in.readSignedBits(MonsterProtocol.POWER_AMOUNT_BITS);
        }

        String[] intent = WireIds.readColumn(in, count, MonsterProtocol.INTENT_WIRE_ID_BITS, INTENTS);
        int[] intentDamage = readColumn(in, count, MonsterProtocol.INTENT_DAMAGE_BITS);
        int[] intentMultiAmount = readColumn(in, count, MonsterProtocol.INTENT_MULTI_AMOUNT_BITS);
        return new PacketRecord.Monsters(instanceId, monsterId, hp, maxHp, block,
            powerEntryCount, powerId, powerAmount, intent, intentDamage, intentMultiAmount);
    }

    private static int[] readColumn(BitReader in, int size, int width) {
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = in.readBits(width);
        }
        return values;
    }

    private static void requireUniqueInstanceIds(int[] ids) {
        int seen = 0;
        for (int id : ids) {
            int bit = 1 << id;
            if ((seen & bit) != 0) {
                throw new IllegalArgumentException("duplicate monster instanceId=" + id);
            }
            seen |= bit;
        }
    }
}
