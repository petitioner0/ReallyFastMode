package reallyfastmode.protocol;

import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaEventCatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class VanillaEventCatalogTest {
    @Test
    public void containsEveryGeneratedEventInWireOrder() {
        assertEquals(53, VanillaEventCatalog.values().length);
        assertEquals(Integer.valueOf(4), VanillaEventCatalog.eventIdToWireId.get("Big Fish"));

        for (VanillaEventCatalog event : VanillaEventCatalog.values()) {
            if (event == VanillaEventCatalog.UNKNOWN) {
                continue;
            }
            assertEquals(Integer.valueOf(event.wireId),
                VanillaEventCatalog.eventIdToWireId.get(event.eventId));
        }
    }

    @Test
    public void unknownAndModdedIdsUseTheFixedSixBitSentinel() {
        assertEquals(63, VanillaEventCatalog.UNKNOWN.wireId);
        assertEquals(4, VanillaEventCatalog.wireId("Big Fish"));
        assertEquals(63, VanillaEventCatalog.wireId("mod:event"));
        assertEquals(63, VanillaEventCatalog.wireId(null));
        assertEquals(52, VanillaEventCatalog.eventIdToWireId.size());
    }

    @Test
    public void idMapIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
            () -> VanillaEventCatalog.eventIdToWireId.put("mod:event", 52));
    }
}
