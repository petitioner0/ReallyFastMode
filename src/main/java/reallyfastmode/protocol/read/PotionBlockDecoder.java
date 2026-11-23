package reallyfastmode.protocol.read;

import reallyfastmode.protocol.PotionProtocol;
import reallyfastmode.protocol.io.BitReader;

final class PotionBlockDecoder {
    private PotionBlockDecoder() {
    }

    static PacketRecord.Potions read(BitReader in) {
        int count = in.readBits(PotionProtocol.POTION_SLOTS_COUNT_BITS);
        String[] potionId = WireIds.readColumn(in, count, PotionProtocol.POTION_WIRE_ID_BITS, WireIds.POTIONS);
        return new PacketRecord.Potions(potionId);
    }
}
