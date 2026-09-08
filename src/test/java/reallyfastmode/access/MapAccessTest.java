package reallyfastmode.access;

import com.megacrit.cardcrawl.map.MapRoomNode;
import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaNodeCatalog;

import static org.junit.Assert.assertEquals;

public class MapAccessTest {
    @Test
    public void mapsUnusedCoordinatesToEmpty() {
        assertEquals(VanillaNodeCatalog.EMPTY.wireId, MapAccess.wireId(null));

        MapRoomNode node = new MapRoomNode(0, 0);
        assertEquals(VanillaNodeCatalog.EMPTY.wireId, MapAccess.wireId(node));
    }
}
