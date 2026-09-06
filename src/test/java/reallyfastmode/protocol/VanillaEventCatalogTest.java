package reallyfastmode.protocol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class VanillaEventCatalogTest {
    @Test
    public void containsEveryGeneratedEventInWireOrder() {
        assertEquals(52, VanillaEventCatalog.values().length);
        assertEquals(Integer.valueOf(4), VanillaEventCatalog.eventIdToWireId.get("Big Fish"));

        for (VanillaEventCatalog event : VanillaEventCatalog.values()) {
            assertEquals(Integer.valueOf(event.wireId),
                VanillaEventCatalog.eventIdToWireId.get(event.eventId));
        }
    }

    @Test
    public void idMapIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
            () -> VanillaEventCatalog.eventIdToWireId.put("mod:event", 52));
    }
}
