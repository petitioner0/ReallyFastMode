package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.potions.AbstractPotion;
import reallyfastmode.access.PlayerAccess;
import reallyfastmode.access.PotionAccess;
import reallyfastmode.protocol.PotionProtocol;

import java.util.List;
import java.util.Objects;

/** Captures the player's current potion slots into one semantic snapshot. */
public final class PotionSnapshotBuilder {
    private PotionSnapshotBuilder() {
    }

    public static PotionSnapshot build() {
        return buildFromSource(new StsSource());
    }

    static PotionSnapshot buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int potionSlotsCount = source.potionSlotsCount();
        if (potionSlotsCount < 0 || potionSlotsCount > PotionProtocol.MAX_POTION_SLOTS) {
            throw new IllegalArgumentException(
                "potionSlotsCount=" + potionSlotsCount
                    + " outside 0-" + PotionProtocol.MAX_POTION_SLOTS
            );
        }
        int potionSize = source.potionSize();
        if (potionSize != potionSlotsCount) {
            throw new IllegalArgumentException(
                "potionWireId length=" + potionSize
                    + " but potionSlotsCount=" + potionSlotsCount
            );
        }

        PotionSnapshot result = new PotionSnapshot(potionSlotsCount);
        for (int i = 0; i < potionSlotsCount; i++) {
            int wireId = source.potionWireId(i);
            requireUnsigned("potionWireId[" + i + "]", wireId,
                PotionProtocol.POTION_WIRE_ID_BITS);
            result.potionWireId[i] = wireId;
        }
        return result;
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }

    interface Source {
        int potionSlotsCount();

        int potionSize();

        int potionWireId(int potionIndex);
    }

    private static final class StsSource implements Source {
        private final List<AbstractPotion> potions;

        private StsSource() {
            this.potions = PotionAccess.potions();
        }

        @Override
        public int potionSlotsCount() {
            return PlayerAccess.potionSlots();
        }

        @Override
        public int potionSize() {
            return potions.size();
        }

        @Override
        public int potionWireId(int potionIndex) {
            AbstractPotion potion = Objects.requireNonNull(
                potions.get(potionIndex), "potions[" + potionIndex + "]"
            );
            return PotionAccess.wireId(potion);
        }
    }
}
