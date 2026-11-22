package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.cards.AbstractCard;
import reallyfastmode.access.CardAccess;
import reallyfastmode.access.SelectionAccess;
import reallyfastmode.protocol.SelectionProtocol;

import java.util.List;
import java.util.Objects;

/** Captures the active selection screen's candidate cards into one snapshot. */
public final class SelectionSnapshotBuilder {
    private SelectionSnapshotBuilder() {
    }

    public static SelectionSnapshot build() {
        return buildFromSource(new StsSource());
    }

    static SelectionSnapshot buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int candidatesCardCount = source.candidatesCardCount();
        if (candidatesCardCount < 0
            || candidatesCardCount > SelectionProtocol.MAX_CANDIDATES) {
            throw new IllegalArgumentException(
                "candidatesCardCount=" + candidatesCardCount
                    + " outside 0-" + SelectionProtocol.MAX_CANDIDATES
            );
        }

        int maxSelectableCards = Math.min(source.selectionLimit(), candidatesCardCount);
        SelectionSnapshot result = new SelectionSnapshot(candidatesCardCount, maxSelectableCards);
        for (int i = 0; i < candidatesCardCount; i++) {
            int wireId = source.candidatesCardWireId(i);
            requireUnsigned("candidatesCardWireId[" + i + "]", wireId,
                SelectionProtocol.CANDIDATES_CARD_WIRE_ID_BITS);
            result.candidatesCardWireId[i] = wireId;
            result.candidatesCardUpgraded[i] = source.candidatesCardUpgraded(i);
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
        int candidatesCardCount();

        int selectionLimit();

        int candidatesCardWireId(int candidateIndex);

        boolean candidatesCardUpgraded(int candidateIndex);
    }

    private static final class StsSource implements Source {
        private final List<AbstractCard> candidates;

        private StsSource() {
            this.candidates = SelectionAccess.candidates();
        }

        @Override
        public int candidatesCardCount() {
            return candidates.size();
        }

        @Override
        public int selectionLimit() {
            return SelectionAccess.selectionLimit();
        }

        @Override
        public int candidatesCardWireId(int candidateIndex) {
            return CardAccess.wireId(candidate(candidateIndex));
        }

        @Override
        public boolean candidatesCardUpgraded(int candidateIndex) {
            return CardAccess.upgraded(candidate(candidateIndex));
        }

        private AbstractCard candidate(int candidateIndex) {
            return Objects.requireNonNull(
                candidates.get(candidateIndex), "candidates[" + candidateIndex + "]"
            );
        }
    }
}
