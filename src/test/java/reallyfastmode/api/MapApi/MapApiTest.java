package reallyfastmode.api.MapApi;

import com.megacrit.cardcrawl.map.MapRoomNode;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

public class MapApiTest {
    @Test
    public void selectsTheReachableInstanceByXThenY() {
        MapRoomNode selected = new MapRoomNode(2, 5);
        MapRoomNode other = new MapRoomNode(5, 2);

        assertSame(selected, MapApi.requireReachableNode(2, 5, Arrays.asList(other, selected)));
        assertSame(other, MapApi.requireReachableNode(5, 2, Arrays.asList(other, selected)));
        assertFalse(selected.taken);
        assertFalse(other.taken);
    }

    @Test
    public void rejectsCoordinatesOutsideTheReachableSetWithoutChangingNodes() {
        MapRoomNode available = new MapRoomNode(2, 5);
        assertThrows(IllegalArgumentException.class,
            () -> MapApi.requireReachableNode(3, 5, Collections.singletonList(available)));
        assertThrows(IllegalArgumentException.class,
            () -> MapApi.requireReachableNode(2, 6, Collections.singletonList(available)));
        assertThrows(IllegalArgumentException.class,
            () -> MapApi.requireReachableNode(Integer.MAX_VALUE, Integer.MAX_VALUE,
                Collections.singletonList(available)));
        assertFalse(available.taken);
    }

    @Test
    public void rejectsAnEmptyReachableSet() {
        assertThrows(IllegalArgumentException.class,
            () -> MapApi.requireReachableNode(0, 0, Collections.<MapRoomNode>emptyList()));
    }

    @Test
    public void rejectsNegativeCoordinatesBeforeReadingGameState() {
        assertThrows(IllegalArgumentException.class, () -> MapApi.selectNode(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> MapApi.selectNode(0, -1));
    }
}
