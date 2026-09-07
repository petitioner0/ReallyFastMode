package reallyfastmode.protocol.write;

import reallyfastmode.protocol.PlayerProtocol;
import reallyfastmode.protocol.build.PlayerSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes {@link PlayerSnapshot} in the fixed Player slot order. */
public final class PlayerBlockWriter {
    private PlayerBlockWriter() {
    }

    public static void write(BitWriter out, PlayerSnapshot player) {
        Objects.requireNonNull(out, "out");
        validate(Objects.requireNonNull(player, "player"));

        out.writeBits(player.hp, PlayerProtocol.HP_BITS);
        out.writeBits(player.maxHp, PlayerProtocol.HP_BITS);
        out.writeBits(player.block, PlayerProtocol.BLOCK_BITS);
        out.writeBits(player.energy, PlayerProtocol.ENERGY_BITS);
        out.writeBits(player.gold, PlayerProtocol.GOLD_BITS);
        out.writeBits(player.maxOrbs, PlayerProtocol.MAX_ORBS_BITS);
        for (int i = 0; i < player.maxOrbs; i++) {
            out.writeBits(player.orbWireId[i], PlayerProtocol.ORB_WIRE_ID_BITS);
        }

        out.writeBits(player.powerWireId.length, PlayerProtocol.POWER_ENTRY_COUNT_BITS);
        for (int i = 0; i < player.powerWireId.length; i++) {
            out.writeBits(player.powerWireId[i], PlayerProtocol.POWER_WIRE_ID_BITS);
        }
        for (int i = 0; i < player.powerCounts.length; i++) {
            out.writeBits(player.powerCounts[i], PlayerProtocol.POWER_COUNT_BITS);
        }

        out.writeBits(player.relicWireId.length, PlayerProtocol.RELIC_ENTRY_COUNT_BITS);
        for (int i = 0; i < player.relicWireId.length; i++) {
            out.writeBits(player.relicWireId[i], PlayerProtocol.RELIC_WIRE_ID_BITS);
        }
        for (int i = 0; i < player.relicCounts.length; i++) {
            out.writeBits(normalizedRelicCount(player.relicCounts[i]),
                PlayerProtocol.RELIC_COUNT_BITS);
        }
    }

    private static void validate(PlayerSnapshot player) {
        requireUnsigned("hp", player.hp, PlayerProtocol.HP_BITS);
        requireUnsigned("maxHp", player.maxHp, PlayerProtocol.HP_BITS);
        requireUnsigned("block", player.block, PlayerProtocol.BLOCK_BITS);
        requireUnsigned("energy", player.energy, PlayerProtocol.ENERGY_BITS);
        requireUnsigned("gold", player.gold, PlayerProtocol.GOLD_BITS);
        requireUnsigned("maxOrbs", player.maxOrbs, PlayerProtocol.MAX_ORBS_BITS);
        if (player.maxOrbs > PlayerProtocol.MAX_ORBS) {
            throw new IllegalArgumentException(
                "maxOrbs=" + player.maxOrbs + " outside 0-" + PlayerProtocol.MAX_ORBS
            );
        }
        requireLength("orbWireId", player.orbWireId, player.maxOrbs);
        requireSameLength("powerWireId", player.powerWireId,
            "powerCounts", player.powerCounts);
        requireSameLength("relicWireId", player.relicWireId,
            "relicCounts", player.relicCounts);
        requireUnsigned("powerEntryCount", player.powerWireId.length,
            PlayerProtocol.POWER_ENTRY_COUNT_BITS);
        requireUnsigned("relicEntryCount", player.relicWireId.length,
            PlayerProtocol.RELIC_ENTRY_COUNT_BITS);

        for (int i = 0; i < player.maxOrbs; i++) {
            requireUnsigned("orbWireId[" + i + "]", player.orbWireId[i],
                PlayerProtocol.ORB_WIRE_ID_BITS);
        }
        for (int i = 0; i < player.powerWireId.length; i++) {
            requireUnsigned("powerWireId[" + i + "]", player.powerWireId[i],
                PlayerProtocol.POWER_WIRE_ID_BITS);
            requireSigned("powerCounts[" + i + "]", player.powerCounts[i],
                PlayerProtocol.POWER_COUNT_BITS);
        }
        for (int i = 0; i < player.relicWireId.length; i++) {
            requireUnsigned("relicWireId[" + i + "]", player.relicWireId[i],
                PlayerProtocol.RELIC_WIRE_ID_BITS);
            requireUnsigned("relicCounts[" + i + "]",
                normalizedRelicCount(player.relicCounts[i]),
                PlayerProtocol.RELIC_COUNT_BITS);
        }
    }

    private static int normalizedRelicCount(int value) {
        return Math.max(0, value);
    }

    private static void requireLength(String name, int[] values, int expected) {
        if (values == null || values.length != expected) {
            throw new IllegalArgumentException(
                name + " length=" + (values == null ? "null" : values.length)
                    + ", expected=" + expected
            );
        }
    }

    private static void requireSameLength(
        String firstName,
        int[] first,
        String secondName,
        int[] second
    ) {
        if (first == null || second == null || first.length != second.length) {
            throw new IllegalArgumentException(
                firstName + " length=" + (first == null ? "null" : first.length)
                    + ", " + secondName + " length="
                    + (second == null ? "null" : second.length)
            );
        }
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }

    private static void requireSigned(String name, int value, int bits) {
        int min = -(1 << (bits - 1));
        int max = (1 << (bits - 1)) - 1;
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                name + "=" + value + " outside " + min + "-" + max
            );
        }
    }
}
