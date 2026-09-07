package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import reallyfastmode.access.PlayerAccess;
import reallyfastmode.protocol.PlayerProtocol;

import java.util.List;
import java.util.Objects;

/** Captures current STS player values into one semantic snapshot. */
public final class PlayerSnapshotBuilder {
    private PlayerSnapshotBuilder() {
    }

    public static PlayerSnapshot build() {
        return buildFromSource(new StsSource());
    }

    static PlayerSnapshot buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int hp = source.hp();
        int maxHp = source.maxHp();
        int block = source.block();
        int energy = source.energy();
        int gold = source.gold();
        int maxOrbs = source.maxOrbs();
        int orbSize = source.orbSize();
        int powerSize = source.powerSize();
        int relicSize = source.relicSize();

        requireUnsigned("hp", hp, PlayerProtocol.HP_BITS);
        requireUnsigned("maxHp", maxHp, PlayerProtocol.HP_BITS);
        requireUnsigned("block", block, PlayerProtocol.BLOCK_BITS);
        requireUnsigned("energy", energy, PlayerProtocol.ENERGY_BITS);
        requireUnsigned("gold", gold, PlayerProtocol.GOLD_BITS);
        requireUnsigned("maxOrbs", maxOrbs, PlayerProtocol.MAX_ORBS_BITS);
        if (maxOrbs > PlayerProtocol.MAX_ORBS) {
            throw new IllegalArgumentException(
                "maxOrbs=" + maxOrbs + " outside 0-" + PlayerProtocol.MAX_ORBS
            );
        }
        if (orbSize != maxOrbs) {
            throw new IllegalArgumentException(
                "orbWireId length=" + orbSize + " but maxOrbs=" + maxOrbs
            );
        }
        requireSize("powerSize", powerSize);
        requireSize("relicSize", relicSize);

        PlayerSnapshot result = new PlayerSnapshot(
            hp,
            maxHp,
            block,
            energy,
            gold,
            maxOrbs,
            powerSize,
            relicSize
        );

        for (int i = 0; i < maxOrbs; i++) {
            int wireId = source.orbWireId(i);
            requireUnsigned("orbWireId[" + i + "]", wireId,
                PlayerProtocol.ORB_WIRE_ID_BITS);
            result.orbWireId[i] = wireId;
        }
        for (int i = 0; i < powerSize; i++) {
            int wireId = source.powerWireId(i);
            int count = source.powerCount(i);
            requireUnsigned("powerWireId[" + i + "]", wireId,
                PlayerProtocol.POWER_WIRE_ID_BITS);
            requireSigned("powerCounts[" + i + "]", count,
                PlayerProtocol.POWER_COUNT_BITS);
            result.powerWireId[i] = wireId;
            result.powerCounts[i] = count;
        }
        for (int i = 0; i < relicSize; i++) {
            int wireId = source.relicWireId(i);
            int count = Math.max(0, source.relicCount(i));
            requireUnsigned("relicWireId[" + i + "]", wireId,
                PlayerProtocol.RELIC_WIRE_ID_BITS);
            requireUnsigned("relicCounts[" + i + "]", count,
                PlayerProtocol.RELIC_COUNT_BITS);
            result.relicWireId[i] = wireId;
            result.relicCounts[i] = count;
        }
        return result;
    }

    private static void requireSize(String name, int value) {
        if (value < 0) {
            throw new IllegalArgumentException(name + "=" + value);
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

    interface Source {
        int hp();

        int maxHp();

        int block();

        int energy();

        int gold();

        int maxOrbs();

        int orbSize();

        int orbWireId(int orbIndex);

        int powerSize();

        int powerWireId(int powerIndex);

        int powerCount(int powerIndex);

        int relicSize();

        int relicWireId(int relicIndex);

        int relicCount(int relicIndex);
    }

    private static final class StsSource implements Source {
        private final List<AbstractOrb> orbs;
        private final List<AbstractPower> powers;
        private final List<AbstractRelic> relics;

        private StsSource() {
            this.orbs = PlayerAccess.orbs();
            this.powers = PlayerAccess.powers();
            this.relics = PlayerAccess.relics();
        }

        @Override
        public int hp() {
            return PlayerAccess.hp();
        }

        @Override
        public int maxHp() {
            return PlayerAccess.maxHp();
        }

        @Override
        public int block() {
            return PlayerAccess.block();
        }

        @Override
        public int energy() {
            return PlayerAccess.energy();
        }

        @Override
        public int gold() {
            return PlayerAccess.gold();
        }

        @Override
        public int maxOrbs() {
            return PlayerAccess.maxOrbs();
        }

        @Override
        public int orbSize() {
            return orbs.size();
        }

        @Override
        public int orbWireId(int orbIndex) {
            return PlayerAccess.orbWireId(orb(orbIndex));
        }

        @Override
        public int powerSize() {
            return powers.size();
        }

        @Override
        public int powerWireId(int powerIndex) {
            return PlayerAccess.powerWireId(power(powerIndex));
        }

        @Override
        public int powerCount(int powerIndex) {
            return power(powerIndex).amount;
        }

        @Override
        public int relicSize() {
            return relics.size();
        }

        @Override
        public int relicWireId(int relicIndex) {
            return PlayerAccess.relicWireId(relic(relicIndex));
        }

        @Override
        public int relicCount(int relicIndex) {
            return PlayerAccess.relicCounter(relic(relicIndex));
        }

        private AbstractOrb orb(int index) {
            return Objects.requireNonNull(orbs.get(index), "orbs[" + index + "]");
        }

        private AbstractPower power(int index) {
            return Objects.requireNonNull(powers.get(index), "powers[" + index + "]");
        }

        private AbstractRelic relic(int index) {
            return Objects.requireNonNull(relics.get(index), "relics[" + index + "]");
        }
    }
}
