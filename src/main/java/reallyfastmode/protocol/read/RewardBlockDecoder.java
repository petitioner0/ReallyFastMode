package reallyfastmode.protocol.read;

import reallyfastmode.protocol.RewardProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaRewardTypeCatalog;
import reallyfastmode.protocol.io.BitReader;

final class RewardBlockDecoder {
    private static final VanillaRewardTypeCatalog[] TYPES =
        new VanillaRewardTypeCatalog[1 << RewardProtocol.TYPE_WIRE_ID_BITS];

    static {
        for (VanillaRewardTypeCatalog type : VanillaRewardTypeCatalog.values()) {
            TYPES[type.wireId] = type;
        }
    }

    private RewardBlockDecoder() {
    }

    static PacketRecord.Rewards read(BitReader in) {
        int count = in.readBits(RewardProtocol.REWARD_COUNT_BITS);
        String[] rewardType = new String[count];
        int relicCount = 0;
        int potionCount = 0;
        int goldCount = 0;
        for (int i = 0; i < count; i++) {
            VanillaRewardTypeCatalog type = TYPES[in.readBits(RewardProtocol.TYPE_WIRE_ID_BITS)];
            rewardType[i] = type == VanillaRewardTypeCatalog.UNKNOWN ? WireIds.UNKNOWN_ID : type.name();
            switch (type) {
                case RELIC:
                    relicCount++;
                    break;
                case POTION:
                    potionCount++;
                    break;
                case GOLD:
                case STOLEN_GOLD:
                    goldCount++;
                    break;
                default:
                    break;
            }
        }

        String[] relicId = WireIds.readColumn(in, relicCount, RewardProtocol.RELIC_WIRE_ID_BITS, WireIds.RELICS);
        String[] potionId = WireIds.readColumn(in, potionCount, RewardProtocol.POTION_WIRE_ID_BITS, WireIds.POTIONS);
        int[] golds = new int[goldCount];
        for (int i = 0; i < golds.length; i++) {
            golds[i] = in.readBits(RewardProtocol.GOLD_BITS);
        }
        return new PacketRecord.Rewards(rewardType, relicId, potionId, golds);
    }
}
