package reallyfastmode.protocol.write;

import reallyfastmode.protocol.CombatProtocol;
import reallyfastmode.protocol.build.CombatSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes one {@link CombatSnapshot} as an unsigned eight-bit turn number. */
public final class CombatBlockWriter {
    private CombatBlockWriter() {
    }

    public static void write(BitWriter out, CombatSnapshot combat) {
        Objects.requireNonNull(out, "out");
        CombatSnapshot checkedCombat = Objects.requireNonNull(combat, "combat");
        if (checkedCombat.turn < 0 || checkedCombat.turn > CombatProtocol.MAX_TURN) {
            throw new IllegalArgumentException(
                "turn=" + checkedCombat.turn + " outside 0-" + CombatProtocol.MAX_TURN
            );
        }
        out.writeBits(checkedCombat.turn, CombatProtocol.TURN_BITS);
    }
}
