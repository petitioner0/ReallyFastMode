package reallyfastmode.protocol.read;

import reallyfastmode.protocol.GameProtocol;
import reallyfastmode.protocol.io.BitReader;

final class GameBlockDecoder {
    private GameBlockDecoder() {
    }

    static PacketRecord.Game read(BitReader in) {
        int actWireId = in.readBits(GameProtocol.ACT_BITS);
        return new PacketRecord.Game(
            WireIds.lookup(WireIds.ACTS, actWireId),
            in.readBits(GameProtocol.FLOOR_BITS),
            in.readBits(GameProtocol.ASCENSION_LEVEL_BITS),
            in.readBits(GameProtocol.KEY_BITS),
            in.readBits(GameProtocol.KEY_BITS),
            in.readBits(GameProtocol.KEY_BITS)
        );
    }
}
