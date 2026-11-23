package reallyfastmode.protocol.read;

import reallyfastmode.protocol.CardProtocol;
import reallyfastmode.protocol.io.BitReader;

final class CardBlockDecoder {
    private CardBlockDecoder() {
    }

    static PacketRecord.Deck readDeck(BitReader in) {
        int count = in.readBits(CardProtocol.CARD_COUNT_BITS);
        PacketRecord.Deck cards = new PacketRecord.Deck(count);
        readCardColumns(in, cards.cardId, cards.cost, cards.upgraded);
        readFlags(in, cards.inBottleFlame);
        readFlags(in, cards.inBottleLightning);
        readFlags(in, cards.inBottleTornado);
        return cards;
    }

    static PacketRecord.CombatCards readCombat(BitReader in) {
        int count = in.readBits(CardProtocol.CARD_COUNT_BITS);
        PacketRecord.CombatCards cards = new PacketRecord.CombatCards(count);
        readCardColumns(in, cards.cardId, cards.costForTurn, cards.upgraded);
        return cards;
    }

    private static void readCardColumns(BitReader in, String[] cardIds, int[] costs, int[] upgraded) {
        for (int i = 0; i < cardIds.length; i++) {
            int wireId = in.readBits(CardProtocol.CARD_WIRE_ID_BITS);
            cardIds[i] = WireIds.lookup(WireIds.CARDS, wireId);
        }
        for (int i = 0; i < costs.length; i++) {
            costs[i] = in.readSignedBits(CardProtocol.COST_BITS);
        }
        readFlags(in, upgraded);
    }

    private static void readFlags(BitReader in, int[] flags) {
        for (int i = 0; i < flags.length; i++) {
            flags[i] = in.readBits(1);
        }
    }
}
