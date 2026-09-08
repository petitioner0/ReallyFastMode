package reallyfastmode.access;

import com.megacrit.cardcrawl.map.MapRoomNode;
import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaNodeCatalog;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MapAccessTest {
    @Test
    public void mapsUnusedCoordinatesToEmpty() {
        List<List<MapRoomNode>> map = Arrays.<List<MapRoomNode>>asList(
            Arrays.asList(new MapRoomNode(0, 0), new MapRoomNode(1, 0)),
            Arrays.asList(new MapRoomNode(0, 1), new MapRoomNode(1, 1))
        );

        assertEquals(VanillaNodeCatalog.EMPTY.wireId, MapAccess.wireIdAt(map, 1, 0));
        assertEquals(VanillaNodeCatalog.EMPTY.wireId, MapAccess.wireIdAt(map, 0, 1));
    }
}
