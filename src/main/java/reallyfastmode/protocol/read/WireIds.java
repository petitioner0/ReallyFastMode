package reallyfastmode.protocol.read;

import reallyfastmode.protocol.CardProtocol;
import reallyfastmode.protocol.EventProtocol;
import reallyfastmode.protocol.MonsterProtocol;
import reallyfastmode.protocol.PlayerProtocol;
import reallyfastmode.protocol.PotionProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaActCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaCardCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaEventCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaMonsterCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaOrbCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaPotionCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaPowerCatalog;
import reallyfastmode.protocol.VanillaCatalog.VanillaRelicCatalog;
import reallyfastmode.protocol.io.BitReader;

import java.util.Arrays;
import java.util.Map;

/** Reverse lookups for the vanilla IDs carried by the decoded blocks. */
final class WireIds {
    static final String UNKNOWN_ID = VanillaActCatalog.UNKNOWN.actId;

    static final String[] ACTS = new String[VanillaActCatalog.UNKNOWN.wireId + 1];
    static final String[] ORBS = reverse(VanillaOrbCatalog.orbIdToWireId, PlayerProtocol.UNKNOWN_ORB_WIRE_ID);
    static final String[] POWERS = reverse(VanillaPowerCatalog.powerIdToWireId, PlayerProtocol.UNKNOWN_POWER_WIRE_ID);
    static final String[] RELICS = reverse(VanillaRelicCatalog.relicIdToWireId, PlayerProtocol.UNKNOWN_RELIC_WIRE_ID);
    static final String[] CARDS = reverse(VanillaCardCatalog.cardIdToWireId, CardProtocol.UNKNOWN_CARD_WIRE_ID);
    static final String[] MONSTERS = reverse(VanillaMonsterCatalog.monsterIdToWireId,
        MonsterProtocol.UNKNOWN_MONSTER_WIRE_ID);
    static final String[] POTIONS = reverse(VanillaPotionCatalog.potionIdToWireId,
        PotionProtocol.UNKNOWN_POTION_WIRE_ID + 1);
    static final String[] EVENTS = reverse(VanillaEventCatalog.eventIdToWireId,
        EventProtocol.MAX_EVENT_WIRE_ID + 1);

    static {
        Arrays.fill(ACTS, UNKNOWN_ID);
        for (VanillaActCatalog act : VanillaActCatalog.values()) {
            ACTS[act.wireId] = act.actId;
        }
    }

    private WireIds() {
    }

    static String lookup(String[] ids, int wireId) {
        return wireId < ids.length ? ids[wireId] : UNKNOWN_ID;
    }

    static String[] readColumn(BitReader in, int size, int width, String[] ids) {
        String[] result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = lookup(ids, in.readBits(width));
        }
        return result;
    }

    private static String[] reverse(Map<String, Integer> wireIds, int size) {
        String[] ids = new String[size];
        Arrays.fill(ids, UNKNOWN_ID);
        wireIds.forEach((id, wireId) -> ids[wireId] = id);
        return ids;
    }
}
