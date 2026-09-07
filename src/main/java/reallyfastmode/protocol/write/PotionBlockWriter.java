package reallyfastmode.protocol.write;

import reallyfastmode.protocol.PotionProtocol;
import reallyfastmode.protocol.build.PotionSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes one {@link PotionSnapshot} in the fixed Potion slot order. */
public final class PotionBlockWriter {
    private PotionBlockWriter() {
    }

    public static void write(BitWriter out, PotionSnapshot potions) {
        Objects.requireNonNull(out, "out");
        validate(Objects.requireNonNull(potions, "potions"));

        out.writeBits(potions.potionSlotsCount, PotionProtocol.POTION_SLOTS_COUNT_BITS);
        for (int i = 0; i < potions.potionSlotsCount; i++) {
            out.writeBits(potions.potionWireId[i], PotionProtocol.POTION_WIRE_ID_BITS);
        }
    }

    private static void validate(PotionSnapshot potions) {
        if (potions.potionSlotsCount < 0
            || potions.potionSlotsCount > PotionProtocol.MAX_POTION_SLOTS) {
            throw new IllegalArgumentException(
                "potionSlotsCount=" + potions.potionSlotsCount
                    + " outside 0-" + PotionProtocol.MAX_POTION_SLOTS
            );
        }
        if (potions.potionWireId == null
            || potions.potionWireId.length != potions.potionSlotsCount) {
            throw new IllegalArgumentException(
                "potionWireId length="
                    + (potions.potionWireId == null ? "null" : potions.potionWireId.length)
                    + ", expected=" + potions.potionSlotsCount
            );
        }
        for (int i = 0; i < potions.potionSlotsCount; i++) {
            requireUnsigned("potionWireId[" + i + "]", potions.potionWireId[i],
                PotionProtocol.POTION_WIRE_ID_BITS);
        }
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }
}
