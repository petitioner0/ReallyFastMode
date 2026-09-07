package reallyfastmode.protocol;

import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaOrbCatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class VanillaOrbCatalogTest {
    @Test
    public void containsEveryVanillaOrbInWireOrder() {
        assertEquals(5, VanillaOrbCatalog.values().length);
        assertEquals(0, VanillaOrbCatalog.DARK.wireId);
        assertEquals(1, VanillaOrbCatalog.EMPTY.wireId);
        assertEquals(2, VanillaOrbCatalog.FROST.wireId);
        assertEquals(3, VanillaOrbCatalog.LIGHTNING.wireId);
        assertEquals(4, VanillaOrbCatalog.PLASMA.wireId);
        assertEquals(5, VanillaOrbCatalog.UNKNOWN_WIRE_ID);
        assertEquals(Integer.valueOf(0), VanillaOrbCatalog.orbIdToWireId.get("Dark"));
        assertEquals(Integer.valueOf(1), VanillaOrbCatalog.orbIdToWireId.get("Empty"));
        assertEquals(Integer.valueOf(2), VanillaOrbCatalog.orbIdToWireId.get("Frost"));
        assertEquals(Integer.valueOf(3), VanillaOrbCatalog.orbIdToWireId.get("Lightning"));
        assertEquals(Integer.valueOf(4), VanillaOrbCatalog.orbIdToWireId.get("Plasma"));

        for (VanillaOrbCatalog orb : VanillaOrbCatalog.values()) {
            assertEquals(Integer.valueOf(orb.wireId),
                VanillaOrbCatalog.orbIdToWireId.get(orb.orbId));
        }
    }

    @Test
    public void idMapIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
            () -> VanillaOrbCatalog.orbIdToWireId.put("mod:orb", 5));
    }
}
