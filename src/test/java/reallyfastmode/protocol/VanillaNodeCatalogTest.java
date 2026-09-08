package reallyfastmode.protocol;

import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EmptyRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoom;
import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaNodeCatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class VanillaNodeCatalogTest {
    @Test
    public void reservesEmptyAndUnknownAroundTheVanillaMapRooms() {
        assertEquals(8, VanillaNodeCatalog.values().length);
        assertEquals(0, VanillaNodeCatalog.EMPTY.wireId);
        assertEquals(1, VanillaNodeCatalog.MONSTER.wireId);
        assertEquals(2, VanillaNodeCatalog.ELITE.wireId);
        assertEquals(3, VanillaNodeCatalog.REST.wireId);
        assertEquals(4, VanillaNodeCatalog.EVENT.wireId);
        assertEquals(5, VanillaNodeCatalog.SHOP.wireId);
        assertEquals(6, VanillaNodeCatalog.TREASURE.wireId);
        assertEquals(7, VanillaNodeCatalog.UNKNOWN.wireId);

        assertEquals(0, VanillaNodeCatalog.wireId(null));
        assertEquals(0, VanillaNodeCatalog.wireId(EmptyRoom.class));
        assertEquals(1, VanillaNodeCatalog.wireId(MonsterRoom.class));
        assertEquals(2, VanillaNodeCatalog.wireId(MonsterRoomElite.class));
        assertEquals(3, VanillaNodeCatalog.wireId(RestRoom.class));
        assertEquals(4, VanillaNodeCatalog.wireId(EventRoom.class));
        assertEquals(5, VanillaNodeCatalog.wireId(ShopRoom.class));
        assertEquals(6, VanillaNodeCatalog.wireId(TreasureRoom.class));
    }

    @Test
    public void mapsNonVanillaRoomTypesToUnknownWithoutUsingInheritance() {
        assertEquals(VanillaNodeCatalog.UNKNOWN.wireId,
            VanillaNodeCatalog.wireId(ModdedMonsterRoom.class));
    }

    @Test
    public void roomTypeMapIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
            () -> VanillaNodeCatalog.roomTypeToWireId.put(ModdedMonsterRoom.class, 8));
    }

    private static final class ModdedMonsterRoom extends MonsterRoom {
    }
}
