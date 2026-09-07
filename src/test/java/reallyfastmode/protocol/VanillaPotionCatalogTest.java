package reallyfastmode.protocol;

import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaPotionCatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class VanillaPotionCatalogTest {
    @Test
    public void appendsEmptyAndUnknownAfterTheVanillaCatalog() {
        assertEquals(45, VanillaPotionCatalog.values().length);
        assertEquals(42, VanillaPotionCatalog.WEAK_POTION.wireId);
        assertEquals(43, VanillaPotionCatalog.EMPTY.wireId);
        assertEquals(44, VanillaPotionCatalog.UNKNOWN.wireId);
        assertEquals(Integer.valueOf(43),
            VanillaPotionCatalog.potionIdToWireId.get("Empty"));
        assertEquals(Integer.valueOf(44),
            VanillaPotionCatalog.potionIdToWireId.get("Unknown"));
    }

    @Test
    public void idMapIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
            () -> VanillaPotionCatalog.potionIdToWireId.put("mod:potion", 45));
    }
}
