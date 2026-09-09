package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.rewards.RewardItem;
import reallyfastmode.access.PlayerAccess;
import reallyfastmode.access.PotionAccess;
import reallyfastmode.access.RewardAccess;
import reallyfastmode.protocol.RewardProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaRewardTypeCatalog;

import java.util.List;
import java.util.Objects;

/** Captures caller-selected rewards into semantic flattened columns. */
public final class RewardColumnsBuilder {
    private RewardColumnsBuilder() {
    }

    public static RewardColumns build(List<RewardItem> rewards) {
        return buildFromSource(new StsSource(rewards));
    }

    static RewardColumns buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int rewardCount = source.rewardCount();
        if (rewardCount < 0 || rewardCount > RewardProtocol.MAX_REWARDS) {
            throw new IllegalArgumentException(
                "rewardCount=" + rewardCount
                    + " outside 0-" + RewardProtocol.MAX_REWARDS
            );
        }

        int[] typeWireIds = new int[rewardCount];
        int relicCount = 0;
        int potionCount = 0;
        int goldCount = 0;
        for (int i = 0; i < rewardCount; i++) {
            int typeWireId = source.typeWireId(i);
            requireUnsigned("typeWireId[" + i + "]", typeWireId,
                RewardProtocol.TYPE_WIRE_ID_BITS);
            typeWireIds[i] = typeWireId;
            if (typeWireId == VanillaRewardTypeCatalog.RELIC.wireId) {
                relicCount++;
            } else if (typeWireId == VanillaRewardTypeCatalog.POTION.wireId) {
                potionCount++;
            } else if (isGold(typeWireId)) {
                goldCount++;
            }
        }

        RewardColumns result = new RewardColumns(
            rewardCount, relicCount, potionCount, goldCount
        );
        System.arraycopy(typeWireIds, 0, result.typeWireId, 0, rewardCount);

        int relicIndex = 0;
        int potionIndex = 0;
        int goldIndex = 0;
        for (int i = 0; i < rewardCount; i++) {
            int typeWireId = typeWireIds[i];
            if (typeWireId == VanillaRewardTypeCatalog.RELIC.wireId) {
                int wireId = source.relicWireId(i);
                requireUnsigned("relicWireId[" + relicIndex + "]", wireId,
                    RewardProtocol.RELIC_WIRE_ID_BITS);
                result.relicWireId[relicIndex++] = wireId;
            } else if (typeWireId == VanillaRewardTypeCatalog.POTION.wireId) {
                int wireId = source.potionWireId(i);
                requireUnsigned("potionWireId[" + potionIndex + "]", wireId,
                    RewardProtocol.POTION_WIRE_ID_BITS);
                result.potionWireId[potionIndex++] = wireId;
            } else if (isGold(typeWireId)) {
                int gold = source.gold(i);
                requireUnsigned("golds[" + goldIndex + "]", gold,
                    RewardProtocol.GOLD_BITS);
                result.golds[goldIndex++] = gold;
            }
        }
        return result;
    }

    private static boolean isGold(int typeWireId) {
        return typeWireId == VanillaRewardTypeCatalog.GOLD.wireId
            || typeWireId == VanillaRewardTypeCatalog.STOLEN_GOLD.wireId;
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }

    interface Source {
        int rewardCount();

        int typeWireId(int rewardIndex);

        int relicWireId(int rewardIndex);

        int potionWireId(int rewardIndex);

        int gold(int rewardIndex);
    }

    private static final class StsSource implements Source {
        private final List<RewardItem> rewards;

        private StsSource(List<RewardItem> rewards) {
            this.rewards = Objects.requireNonNull(rewards, "rewards");
        }

        @Override
        public int rewardCount() {
            return rewards.size();
        }

        @Override
        public int typeWireId(int rewardIndex) {
            return RewardAccess.typeWireId(reward(rewardIndex));
        }

        @Override
        public int relicWireId(int rewardIndex) {
            return PlayerAccess.relicWireId(RewardAccess.relic(reward(rewardIndex)));
        }

        @Override
        public int potionWireId(int rewardIndex) {
            return PotionAccess.wireId(RewardAccess.potion(reward(rewardIndex)));
        }

        @Override
        public int gold(int rewardIndex) {
            return RewardAccess.gold(reward(rewardIndex));
        }

        private RewardItem reward(int index) {
            return Objects.requireNonNull(rewards.get(index), "rewards[" + index + "]");
        }
    }
}
