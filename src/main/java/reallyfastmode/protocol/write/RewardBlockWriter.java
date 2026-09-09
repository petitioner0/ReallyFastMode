package reallyfastmode.protocol.write;

import reallyfastmode.protocol.RewardProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaRewardTypeCatalog;
import reallyfastmode.protocol.build.RewardColumns;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes {@link RewardColumns} in the fixed Reward SoA wire order. */
public final class RewardBlockWriter {
    private RewardBlockWriter() {
    }

    public static void write(BitWriter out, RewardColumns rewards) {
        Objects.requireNonNull(out, "out");
        validate(Objects.requireNonNull(rewards, "rewards"));

        out.writeBits(rewards.rewardCount, RewardProtocol.REWARD_COUNT_BITS);
        for (int i = 0; i < rewards.rewardCount; i++) {
            out.writeBits(rewards.typeWireId[i], RewardProtocol.TYPE_WIRE_ID_BITS);
        }
        for (int wireId : rewards.relicWireId) {
            out.writeBits(wireId, RewardProtocol.RELIC_WIRE_ID_BITS);
        }
        for (int wireId : rewards.potionWireId) {
            out.writeBits(wireId, RewardProtocol.POTION_WIRE_ID_BITS);
        }
        for (int gold : rewards.golds) {
            out.writeBits(gold, RewardProtocol.GOLD_BITS);
        }
    }

    private static void validate(RewardColumns rewards) {
        if (rewards.rewardCount < 0 || rewards.rewardCount > RewardProtocol.MAX_REWARDS) {
            throw new IllegalArgumentException(
                "rewardCount=" + rewards.rewardCount
                    + " outside 0-" + RewardProtocol.MAX_REWARDS
            );
        }
        requireLength("typeWireId", rewards.typeWireId, rewards.rewardCount);

        int expectedRelics = 0;
        int expectedPotions = 0;
        int expectedGolds = 0;
        for (int i = 0; i < rewards.rewardCount; i++) {
            int typeWireId = rewards.typeWireId[i];
            requireUnsigned("typeWireId[" + i + "]", typeWireId,
                RewardProtocol.TYPE_WIRE_ID_BITS);
            if (typeWireId == VanillaRewardTypeCatalog.RELIC.wireId) {
                expectedRelics++;
            } else if (typeWireId == VanillaRewardTypeCatalog.POTION.wireId) {
                expectedPotions++;
            } else if (isGold(typeWireId)) {
                expectedGolds++;
            }
        }

        requireLength("relicWireId", rewards.relicWireId, expectedRelics);
        requireLength("potionWireId", rewards.potionWireId, expectedPotions);
        requireLength("golds", rewards.golds, expectedGolds);
        requireValues("relicWireId", rewards.relicWireId,
            RewardProtocol.RELIC_WIRE_ID_BITS);
        requireValues("potionWireId", rewards.potionWireId,
            RewardProtocol.POTION_WIRE_ID_BITS);
        requireValues("golds", rewards.golds, RewardProtocol.GOLD_BITS);
    }

    private static boolean isGold(int typeWireId) {
        return typeWireId == VanillaRewardTypeCatalog.GOLD.wireId
            || typeWireId == VanillaRewardTypeCatalog.STOLEN_GOLD.wireId;
    }

    private static void requireLength(String name, int[] values, int expected) {
        if (values == null || values.length != expected) {
            throw new IllegalArgumentException(
                name + " length=" + (values == null ? "null" : values.length)
                    + ", expected=" + expected
            );
        }
    }

    private static void requireValues(String name, int[] values, int bits) {
        for (int i = 0; i < values.length; i++) {
            requireUnsigned(name + "[" + i + "]", values[i], bits);
        }
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }
}
