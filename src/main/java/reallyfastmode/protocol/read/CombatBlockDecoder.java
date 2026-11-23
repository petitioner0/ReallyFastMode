package reallyfastmode.protocol.read;

import reallyfastmode.protocol.CombatProtocol;
import reallyfastmode.protocol.build.CombatSnapshot;
import reallyfastmode.protocol.io.BitReader;

final class CombatBlockDecoder {
    private CombatBlockDecoder() {
    }

    static CombatSnapshot read(BitReader in) {
        return new CombatSnapshot(in.readBits(CombatProtocol.TURN_BITS));
    }
}
