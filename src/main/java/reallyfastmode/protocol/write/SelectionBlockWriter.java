package reallyfastmode.protocol.write;

import reallyfastmode.protocol.SelectionProtocol;
import reallyfastmode.protocol.build.SelectionSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Writes the selection counts followed by matching card ID and upgrade columns. */
public final class SelectionBlockWriter {
    private SelectionBlockWriter() {
    }

    public static void write(BitWriter out, SelectionSnapshot selection) {
        Objects.requireNonNull(out, "out");
        validate(Objects.requireNonNull(selection, "selection"));

        out.writeBits(selection.candidatesCardCount, SelectionProtocol.CANDIDATES_CARD_COUNT_BITS);
        out.writeBits(selection.maxSelectableCards, SelectionProtocol.MAX_SELECTABLE_CARDS_BITS);
        for (int wireId : selection.candidatesCardWireId) {
            out.writeBits(wireId, SelectionProtocol.CANDIDATES_CARD_WIRE_ID_BITS);
        }
        for (boolean upgraded : selection.candidatesCardUpgraded) {
            out.writeBit(upgraded);
        }
    }

    private static void validate(SelectionSnapshot selection) {
        if (selection.candidatesCardCount > SelectionProtocol.MAX_CANDIDATES) {
            throw new IllegalArgumentException(
                "candidatesCardCount=" + selection.candidatesCardCount
                    + " exceeds " + SelectionProtocol.MAX_CANDIDATES
            );
        }
        int maxWireId = (1 << SelectionProtocol.CANDIDATES_CARD_WIRE_ID_BITS) - 1;
        for (int i = 0; i < selection.candidatesCardCount; i++) {
            int wireId = selection.candidatesCardWireId[i];
            if (wireId < 0 || wireId > maxWireId) {
                throw new IllegalArgumentException(
                    "candidatesCardWireId[" + i + "]=" + wireId + " outside 0-" + maxWireId
                );
            }
        }
    }
}
