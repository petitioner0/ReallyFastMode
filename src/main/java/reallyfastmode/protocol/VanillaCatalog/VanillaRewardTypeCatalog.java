package reallyfastmode.protocol.VanillaCatalog;

import com.megacrit.cardcrawl.rewards.RewardItem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Explicit three-bit wire mapping for vanilla reward types. */
public enum VanillaRewardTypeCatalog {
    CARD(0, RewardItem.RewardType.CARD),
    GOLD(1, RewardItem.RewardType.GOLD),
    RELIC(2, RewardItem.RewardType.RELIC),
    POTION(3, RewardItem.RewardType.POTION),
    STOLEN_GOLD(4, RewardItem.RewardType.STOLEN_GOLD),
    EMERALD_KEY(5, RewardItem.RewardType.EMERALD_KEY),
    SAPPHIRE_KEY(6, RewardItem.RewardType.SAPPHIRE_KEY),
    UNKNOWN(7);

    public static final Map<RewardItem.RewardType, Integer> typeToWireId;

    static {
        Map<RewardItem.RewardType, Integer> ids =
            new EnumMap<RewardItem.RewardType, Integer>(RewardItem.RewardType.class);
        for (VanillaRewardTypeCatalog value : values()) {
            for (RewardItem.RewardType type : value.types) {
                Integer previous = ids.put(type, value.wireId);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate RewardType mapping: " + type);
                }
            }
        }
        typeToWireId = Collections.unmodifiableMap(ids);
    }

    public final int wireId;
    private final RewardItem.RewardType[] types;

    VanillaRewardTypeCatalog(int wireId, RewardItem.RewardType... types) {
        this.wireId = wireId;
        this.types = types;
    }

    /** Maps vanilla reward types explicitly; null and modded types are unknown. */
    public static int wireId(RewardItem.RewardType type) {
        if (type == null) {
            return UNKNOWN.wireId;
        }
        Integer wireId = typeToWireId.get(type);
        return wireId == null ? UNKNOWN.wireId : wireId;
    }
}
