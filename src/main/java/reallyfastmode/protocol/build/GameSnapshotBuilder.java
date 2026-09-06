package reallyfastmode.protocol.build;

import reallyfastmode.access.GameAccess;
import reallyfastmode.protocol.GameProtocol;
import reallyfastmode.protocol.VanillaActCatalog;

import java.util.Objects;

/** Captures current STS run-wide values into one scalar Game snapshot. */
public final class GameSnapshotBuilder {
    private GameSnapshotBuilder() {
    }

    public static GameSnapshot build() {
        return buildFromSource(new StsSource());
    }

    static GameSnapshot buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int actWireId = VanillaActCatalog.wireId(source.actNum());
        int floor = source.floor();
        int ascensionLevel = source.ascensionLevel();
        requireActWireId(actWireId);
        requireUnsigned("floor", floor, GameProtocol.FLOOR_BITS);
        requireUnsigned("ascensionLevel", ascensionLevel,
            GameProtocol.ASCENSION_LEVEL_BITS);

        return new GameSnapshot(
            actWireId,
            floor,
            ascensionLevel,
            source.hasRubyKey(),
            source.hasEmeraldKey(),
            source.hasSapphireKey()
        );
    }

    private static void requireActWireId(int value) {
        if (value < 0 || value > GameProtocol.MAX_ACT_WIRE_ID) {
            throw new IllegalArgumentException(
                "actWireId=" + value + " outside 0-" + GameProtocol.MAX_ACT_WIRE_ID
            );
        }
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }

    interface Source {
        int actNum();

        int floor();

        int ascensionLevel();

        boolean hasRubyKey();

        boolean hasEmeraldKey();

        boolean hasSapphireKey();
    }

    private static final class StsSource implements Source {
        @Override
        public int actNum() {
            return GameAccess.act();
        }

        @Override
        public int floor() {
            return GameAccess.floor();
        }

        @Override
        public int ascensionLevel() {
            return GameAccess.ascensionLevel();
        }

        @Override
        public boolean hasRubyKey() {
            return GameAccess.hasRubyKey();
        }

        @Override
        public boolean hasEmeraldKey() {
            return GameAccess.hasEmeraldKey();
        }

        @Override
        public boolean hasSapphireKey() {
            return GameAccess.hasSapphireKey();
        }
    }
}
