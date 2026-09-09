package reallyfastmode.protocol;

import com.megacrit.cardcrawl.rewards.RewardItem;
import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaRewardTypeCatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RewardTypeCatalogTest {
    @Test
    public void mapsEveryVanillaRewardTypeToAStableWireId() {
        assertEquals(0, VanillaRewardTypeCatalog.wireId(RewardItem.RewardType.CARD));
        assertEquals(1, VanillaRewardTypeCatalog.wireId(RewardItem.RewardType.GOLD));
        assertEquals(2, VanillaRewardTypeCatalog.wireId(RewardItem.RewardType.RELIC));
        assertEquals(3, VanillaRewardTypeCatalog.wireId(RewardItem.RewardType.POTION));
        assertEquals(4, VanillaRewardTypeCatalog.wireId(RewardItem.RewardType.STOLEN_GOLD));
        assertEquals(5, VanillaRewardTypeCatalog.wireId(RewardItem.RewardType.EMERALD_KEY));
        assertEquals(6, VanillaRewardTypeCatalog.wireId(RewardItem.RewardType.SAPPHIRE_KEY));
    }

    @Test
    public void reservesTheLastThreeBitValueForUnknownTypes() {
        assertEquals(3, RewardProtocol.TYPE_WIRE_ID_BITS);
        assertEquals(7, RewardProtocol.UNKNOWN_TYPE_WIRE_ID);
        assertEquals(RewardProtocol.UNKNOWN_TYPE_WIRE_ID,
            VanillaRewardTypeCatalog.wireId(null));
    }

    @Test
    public void typeMapIsCompleteAndImmutable() {
        assertEquals(RewardItem.RewardType.values().length,
            VanillaRewardTypeCatalog.typeToWireId.size());
        assertThrows(UnsupportedOperationException.class,
            () -> VanillaRewardTypeCatalog.typeToWireId.put(RewardItem.RewardType.CARD, 7));
    }
}
