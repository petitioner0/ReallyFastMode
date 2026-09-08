package reallyfastmode.protocol.VanillaCatalog;

import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EmptyRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomElite;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoom;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Explicit wire mapping for vanilla dungeon-map node types. */
public enum VanillaNodeCatalog {
    EMPTY(0, EmptyRoom.class),
    MONSTER(1, MonsterRoom.class),
    ELITE(2, MonsterRoomElite.class),
    REST(3, RestRoom.class),
    EVENT(4, EventRoom.class),
    SHOP(5, ShopRoom.class),
    TREASURE(6, TreasureRoom.class),
    UNKNOWN(7, null);

    public static final Map<Class<? extends AbstractRoom>, Integer> roomTypeToWireId;

    static {
        Map<Class<? extends AbstractRoom>, Integer> ids =
            new LinkedHashMap<Class<? extends AbstractRoom>, Integer>();
        for (VanillaNodeCatalog node : values()) {
            if (node.roomType != null) {
                ids.put(node.roomType, node.wireId);
            }
        }
        roomTypeToWireId = Collections.unmodifiableMap(ids);
    }

    public final int wireId;
    public final Class<? extends AbstractRoom> roomType;

    VanillaNodeCatalog(int wireId, Class<? extends AbstractRoom> roomType) {
        this.wireId = wireId;
        this.roomType = roomType;
    }

    /** Maps an exact room type; {@code null} represents an unused map coordinate. */
    public static int wireId(Class<? extends AbstractRoom> roomType) {
        if (roomType == null) {
            return EMPTY.wireId;
        }
        Integer wireId = roomTypeToWireId.get(roomType);
        return wireId == null ? UNKNOWN.wireId : wireId;
    }
}
