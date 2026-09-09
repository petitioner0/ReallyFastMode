package reallyfastmode.protocol;

import reallyfastmode.protocol.VanillaCatalog.VanillaRewardTypeCatalog;

/** Fixed constants for the Reward wire layout. */
public final class RewardProtocol {
    public static final int REWARD_COUNT_BITS = 3;
    public static final int TYPE_WIRE_ID_BITS = 3;
    public static final int RELIC_WIRE_ID_BITS = PlayerProtocol.RELIC_WIRE_ID_BITS;
    public static final int POTION_WIRE_ID_BITS = PotionProtocol.POTION_WIRE_ID_BITS;
    public static final int GOLD_BITS = 8;

    public static final int MAX_REWARDS = (1 << REWARD_COUNT_BITS) - 1;
    public static final int UNKNOWN_TYPE_WIRE_ID = VanillaRewardTypeCatalog.UNKNOWN.wireId;

    private RewardProtocol() {
    }
}
