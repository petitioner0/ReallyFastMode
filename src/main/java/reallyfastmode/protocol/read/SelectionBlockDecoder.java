package reallyfastmode.protocol.read;

import reallyfastmode.protocol.SelectionProtocol;
import reallyfastmode.protocol.io.BitReader;

final class SelectionBlockDecoder {
    private SelectionBlockDecoder() {
    }

    static PacketRecord.Selection read(BitReader in) {
        int count = in.readBits(SelectionProtocol.CANDIDATES_CARD_COUNT_BITS);
        int maxSelectableCards = in.readBits(SelectionProtocol.MAX_SELECTABLE_CARDS_BITS);
        if (maxSelectableCards > count) {
            throw new IllegalArgumentException("maxSelectableCards=" + maxSelectableCards + " outside 0-" + count);
        }

        String[] cardId = WireIds.readColumn(in, count,
            SelectionProtocol.CANDIDATES_CARD_WIRE_ID_BITS, WireIds.CARDS);
        PacketRecord.Selection selection = new PacketRecord.Selection(maxSelectableCards, cardId);
        for (int i = 0; i < count; i++) {
            selection.candidatesCardUpgraded[i] = in.readBits(1);
        }
        return selection;
    }
}
