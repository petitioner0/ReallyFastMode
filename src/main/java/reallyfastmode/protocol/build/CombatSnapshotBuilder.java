package reallyfastmode.protocol.build;

import reallyfastmode.access.CombatAccess;
import reallyfastmode.protocol.CombatProtocol;

import java.util.Objects;

/** Captures the current STS combat turn into one scalar snapshot. */
public final class CombatSnapshotBuilder {
    private CombatSnapshotBuilder() {
    }

    public static CombatSnapshot build() {
        return buildFromSource(new StsSource());
    }

    static CombatSnapshot buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int turn = source.turn();
        if (turn < 0 || turn > CombatProtocol.MAX_TURN) {
            throw new IllegalArgumentException(
                "turn=" + turn + " outside 0-" + CombatProtocol.MAX_TURN
            );
        }
        return new CombatSnapshot(turn);
    }

    interface Source {
        int turn();
    }

    private static final class StsSource implements Source {
        @Override
        public int turn() {
            return CombatAccess.turn();
        }
    }
}
