package reallyfastmode.protocol.read;

import reallyfastmode.protocol.PlayerProtocol;
import reallyfastmode.protocol.io.BitReader;

final class PlayerBlockDecoder {
    private PlayerBlockDecoder() {
    }

    static PacketRecord.Player read(BitReader in) {
        int hp = in.readBits(PlayerProtocol.HP_BITS);
        int maxHp = in.readBits(PlayerProtocol.HP_BITS);
        int block = in.readBits(PlayerProtocol.BLOCK_BITS);
        int energy = in.readBits(PlayerProtocol.ENERGY_BITS);
        int gold = in.readBits(PlayerProtocol.GOLD_BITS);
        int maxOrbs = in.readBits(PlayerProtocol.MAX_ORBS_BITS);
        String[] orbId = WireIds.readColumn(in, maxOrbs, PlayerProtocol.ORB_WIRE_ID_BITS, WireIds.ORBS);

        int powerSize = in.readBits(PlayerProtocol.POWER_ENTRY_COUNT_BITS);
        String[] powerId = WireIds.readColumn(in, powerSize, PlayerProtocol.POWER_WIRE_ID_BITS, WireIds.POWERS);
        int[] powerCounts = new int[powerSize];
        for (int i = 0; i < powerCounts.length; i++) {
            powerCounts[i] = in.readSignedBits(PlayerProtocol.POWER_COUNT_BITS);
        }

        int relicSize = in.readBits(PlayerProtocol.RELIC_ENTRY_COUNT_BITS);
        String[] relicId = WireIds.readColumn(in, relicSize, PlayerProtocol.RELIC_WIRE_ID_BITS, WireIds.RELICS);
        int[] relicCounts = new int[relicSize];
        for (int i = 0; i < relicCounts.length; i++) {
            relicCounts[i] = in.readBits(PlayerProtocol.RELIC_COUNT_BITS);
        }

        return new PacketRecord.Player(hp, maxHp, block, energy, gold,
            orbId, powerId, powerCounts, relicId, relicCounts);
    }
}
