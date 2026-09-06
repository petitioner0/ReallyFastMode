package reallyfastmode.protocol.write;

import reallyfastmode.protocol.GameProtocol;
import reallyfastmode.protocol.build.GameSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes one {@link GameSnapshot} in the fixed scalar Game wire order. */
public final class GameBlockWriter {
    private GameBlockWriter() {
    }

    public static void write(BitWriter out, GameSnapshot game) {
        Objects.requireNonNull(out, "out");
        validate(Objects.requireNonNull(game, "game"));

        out.writeBits(game.actWireId, GameProtocol.ACT_BITS);
        out.writeBits(game.floor, GameProtocol.FLOOR_BITS);
        out.writeBits(game.ascensionLevel, GameProtocol.ASCENSION_LEVEL_BITS);
        out.writeBit(game.hasRubyKey);
        out.writeBit(game.hasEmeraldKey);
        out.writeBit(game.hasSapphireKey);
    }

    private static void validate(GameSnapshot game) {
        if (game.actWireId < 0 || game.actWireId > GameProtocol.MAX_ACT_WIRE_ID) {
            throw new IllegalArgumentException(
                "actWireId=" + game.actWireId
                    + " outside 0-" + GameProtocol.MAX_ACT_WIRE_ID
            );
        }
        requireUnsigned("floor", game.floor, GameProtocol.FLOOR_BITS);
        requireUnsigned("ascensionLevel", game.ascensionLevel,
            GameProtocol.ASCENSION_LEVEL_BITS);
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }
}
