package reallyfastmode.protocol;

import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaActCatalog;

import static org.junit.Assert.assertEquals;

public class VanillaActCatalogTest {
    @Test
    public void locksVanillaActNumbersToExplicitWireIds() {
        assertEquals(1, VanillaActCatalog.ACT_1.actNum);
        assertEquals(0, VanillaActCatalog.ACT_1.wireId);
        assertEquals(2, VanillaActCatalog.ACT_2.actNum);
        assertEquals(1, VanillaActCatalog.ACT_2.wireId);
        assertEquals(3, VanillaActCatalog.ACT_3.actNum);
        assertEquals(2, VanillaActCatalog.ACT_3.wireId);
        assertEquals(4, VanillaActCatalog.ACT_4.actNum);
        assertEquals(3, VanillaActCatalog.ACT_4.wireId);
        assertEquals(4, VanillaActCatalog.UNKNOWN.wireId);

        assertEquals(0, VanillaActCatalog.wireId(1));
        assertEquals(1, VanillaActCatalog.wireId(2));
        assertEquals(2, VanillaActCatalog.wireId(3));
        assertEquals(3, VanillaActCatalog.wireId(4));
    }

    @Test
    public void mapsEveryNonVanillaActNumberToUnknown() {
        assertEquals(4, VanillaActCatalog.wireId(0));
        assertEquals(4, VanillaActCatalog.wireId(-1));
        assertEquals(4, VanillaActCatalog.wireId(Integer.MIN_VALUE));
        assertEquals(4, VanillaActCatalog.wireId(5));
        assertEquals(4, VanillaActCatalog.wireId(Integer.MAX_VALUE));
    }
}
