package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.cards.AbstractCard;
import reallyfastmode.access.CardAccess;
import reallyfastmode.protocol.CardProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Captures STS card objects into a semantic SoA snapshot. */
public final class CardColumnsBuilder {
    private CardColumnsBuilder() {
    }

    public static CardColumns buildDeck(List<AbstractCard> cards) {
        return build(cards, CardColumns.Layout.DECK);
    }

    public static CardColumns buildCombat(List<AbstractCard> cards) {
        return build(cards, CardColumns.Layout.COMBAT);
    }

    private static CardColumns build(List<AbstractCard> cards, CardColumns.Layout layout) {
        if (cards == null) {
            throw new IllegalArgumentException("cards must not be null");
        }
        if (cards.size() > CardProtocol.MAX_CARDS) {
            throw new IllegalArgumentException("card count=" + cards.size());
        }
        return buildFromSource(cards.size(), layout, new StsSource(cards));
    }

    static CardColumns buildFromSource(int size, CardColumns.Layout layout, Source source) {
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(source, "source");
        if (size < 0 || size > CardProtocol.MAX_CARDS) {
            throw new IllegalArgumentException("card count=" + size);
        }

        CardColumns result = new CardColumns(layout, size);
        for (int i = 0; i < size; i++) {
            int cardWireId = source.cardWireId(i);
            int cost = layout == CardColumns.Layout.DECK
                ? source.baseCost(i)
                : source.costForTurn(i);
            requireUnsigned("cardWireId[" + i + "]", cardWireId,
                CardProtocol.CARD_WIRE_ID_BITS);
            requireSigned("cost[" + i + "]", cost, CardProtocol.COST_BITS);

            result.cardWireId[i] = cardWireId;
            result.cost[i] = cost;
            result.upgraded[i] = source.upgraded(i);
            if (layout == CardColumns.Layout.DECK) {
                result.inBottleFlame[i] = source.inBottleFlame(i);
                result.inBottleLightning[i] = source.inBottleLightning(i);
                result.inBottleTornado[i] = source.inBottleTornado(i);
            }
        }
        return result;
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
        int cardWireId(int cardIndex);

        int baseCost(int cardIndex);

        int costForTurn(int cardIndex);

        boolean upgraded(int cardIndex);

        boolean inBottleFlame(int cardIndex);

        boolean inBottleLightning(int cardIndex);

        boolean inBottleTornado(int cardIndex);
    }

    private static final class StsSource implements Source {
        private final List<AbstractCard> cards;

        private StsSource(List<AbstractCard> cards) {
            this.cards = new ArrayList<AbstractCard>(cards.size());
            for (int i = 0; i < cards.size(); i++) {
                this.cards.add(Objects.requireNonNull(cards.get(i), "cards[" + i + "]"));
            }
        }

        @Override
        public int cardWireId(int cardIndex) {
            return CardAccess.wireId(card(cardIndex));
        }

        @Override
        public int baseCost(int cardIndex) {
            return CardAccess.cost(card(cardIndex));
        }

        @Override
        public int costForTurn(int cardIndex) {
            return CardAccess.costForTurn(card(cardIndex));
        }

        @Override
        public boolean upgraded(int cardIndex) {
            return CardAccess.upgraded(card(cardIndex));
        }

        @Override
        public boolean inBottleFlame(int cardIndex) {
            return CardAccess.inBottleFlame(card(cardIndex));
        }

        @Override
        public boolean inBottleLightning(int cardIndex) {
            return CardAccess.inBottleLightning(card(cardIndex));
        }

        @Override
        public boolean inBottleTornado(int cardIndex) {
            return CardAccess.inBottleTornado(card(cardIndex));
        }

        private AbstractCard card(int index) {
            return cards.get(index);
        }
    }
}
