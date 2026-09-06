package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.cards.AbstractCard;
import org.junit.Test;
import reallyfastmode.protocol.CardProtocol;
import reallyfastmode.protocol.VanillaCardCatalog;

import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

public class CardColumnsBuilderTest {
    @Test
    public void acceptsEmptyListsAndRejectsInvalidInputs() {
        CardColumns deck = CardColumnsBuilder.buildDeck(
            Collections.<AbstractCard>emptyList()
        );
        CardColumns combat = CardColumnsBuilder.buildCombat(
            Collections.<AbstractCard>emptyList()
        );

        assertEquals(0, deck.size);
        assertEquals(0, combat.size);
        assertSame(CardColumns.Layout.DECK, deck.layout);
        assertSame(CardColumns.Layout.COMBAT, combat.layout);
        assertThrows(IllegalArgumentException.class,
            () -> CardColumnsBuilder.buildDeck(null));
        assertThrows(NullPointerException.class,
            () -> CardColumnsBuilder.buildCombat(
                Collections.singletonList((AbstractCard) null)
            ));
        assertThrows(NullPointerException.class,
            () -> CardColumnsBuilder.buildFromSource(0, null, new FakeSource(0)));
        assertThrows(NullPointerException.class,
            () -> CardColumnsBuilder.buildFromSource(0, CardColumns.Layout.DECK, null));
    }

    @Test
    public void unknownCardIdFollowsCurrentCatalogTail() {
        assertEquals(432, VanillaCardCatalog.values().length);
        assertEquals(VanillaCardCatalog.values().length,
            VanillaCardCatalog.UNKNOWN_WIRE_ID);
        assertEquals(VanillaCardCatalog.UNKNOWN_WIRE_ID,
            CardProtocol.UNKNOWN_CARD_WIRE_ID);
    }

    @Test
    public void buildsDeckFromBaseCostAndBottleFlags() {
        FakeSource source = new FakeSource(2);
        source.cardWireId = new int[]{5, CardProtocol.UNKNOWN_CARD_WIRE_ID};
        source.baseCost = new int[]{-1, -2};
        source.costForTurn = new int[]{3, 0};
        source.upgraded = new boolean[]{true, false};
        source.inBottleFlame = new boolean[]{true, false};
        source.inBottleLightning = new boolean[]{false, true};
        source.inBottleTornado = new boolean[]{true, true};

        CardColumns columns = CardColumnsBuilder.buildFromSource(
            2, CardColumns.Layout.DECK, source
        );

        assertSame(CardColumns.Layout.DECK, columns.layout);
        assertArrayEquals(new int[]{5, 432}, columns.cardWireId);
        assertArrayEquals(new int[]{-1, -2}, columns.cost);
        assertArrayEquals(new boolean[]{true, false}, columns.upgraded);
        assertArrayEquals(new boolean[]{true, false}, columns.inBottleFlame);
        assertArrayEquals(new boolean[]{false, true}, columns.inBottleLightning);
        assertArrayEquals(new boolean[]{true, true}, columns.inBottleTornado);
    }

    @Test
    public void buildsCombatFromCostForTurnWithoutReadingBottleFlags() {
        FakeSource source = new FakeSource(2);
        source.cardWireId = new int[]{17, 31};
        source.baseCost = new int[]{7, 7};
        source.costForTurn = new int[]{0, -1};
        source.upgraded = new boolean[]{false, true};
        source.rejectBottleReads = true;

        CardColumns columns = CardColumnsBuilder.buildFromSource(
            2, CardColumns.Layout.COMBAT, source
        );

        assertSame(CardColumns.Layout.COMBAT, columns.layout);
        assertArrayEquals(new int[]{17, 31}, columns.cardWireId);
        assertArrayEquals(new int[]{0, -1}, columns.cost);
        assertArrayEquals(new boolean[]{false, true}, columns.upgraded);
        for (int i = 0; i < columns.size; i++) {
            assertFalse(columns.inBottleFlame[i]);
            assertFalse(columns.inBottleLightning[i]);
            assertFalse(columns.inBottleTornado[i]);
        }
    }

    @Test
    public void enforcesCountWireIdAndSignedCostBounds() {
        assertEquals(CardProtocol.MAX_CARDS,
            CardColumnsBuilder.buildFromSource(
                CardProtocol.MAX_CARDS,
                CardColumns.Layout.COMBAT,
                new FakeSource(CardProtocol.MAX_CARDS)
            ).size);
        assertThrows(IllegalArgumentException.class,
            () -> CardColumnsBuilder.buildFromSource(
                CardProtocol.MAX_CARDS + 1,
                CardColumns.Layout.COMBAT,
                new FakeSource(CardProtocol.MAX_CARDS + 1)
            ));

        FakeSource costEdges = new FakeSource(2);
        costEdges.baseCost = new int[]{-8, 7};
        assertArrayEquals(new int[]{-8, 7},
            CardColumnsBuilder.buildFromSource(
                2, CardColumns.Layout.DECK, costEdges
            ).cost);

        FakeSource lowCost = new FakeSource(1);
        lowCost.costForTurn[0] = -9;
        assertThrows(IllegalArgumentException.class,
            () -> CardColumnsBuilder.buildFromSource(
                1, CardColumns.Layout.COMBAT, lowCost
            ));

        FakeSource highCost = new FakeSource(1);
        highCost.baseCost[0] = 8;
        assertThrows(IllegalArgumentException.class,
            () -> CardColumnsBuilder.buildFromSource(
                1, CardColumns.Layout.DECK, highCost
            ));

        FakeSource highWireId = new FakeSource(1);
        highWireId.cardWireId[0] = 512;
        assertThrows(IllegalArgumentException.class,
            () -> CardColumnsBuilder.buildFromSource(
                1, CardColumns.Layout.COMBAT, highWireId
            ));
    }

    private static final class FakeSource implements CardColumnsBuilder.Source {
        private int[] cardWireId;
        private int[] baseCost;
        private int[] costForTurn;
        private boolean[] upgraded;
        private boolean[] inBottleFlame;
        private boolean[] inBottleLightning;
        private boolean[] inBottleTornado;
        private boolean rejectBottleReads;

        private FakeSource(int size) {
            cardWireId = new int[size];
            baseCost = new int[size];
            costForTurn = new int[size];
            upgraded = new boolean[size];
            inBottleFlame = new boolean[size];
            inBottleLightning = new boolean[size];
            inBottleTornado = new boolean[size];
        }

        @Override
        public int cardWireId(int cardIndex) {
            return cardWireId[cardIndex];
        }

        @Override
        public int baseCost(int cardIndex) {
            return baseCost[cardIndex];
        }

        @Override
        public int costForTurn(int cardIndex) {
            return costForTurn[cardIndex];
        }

        @Override
        public boolean upgraded(int cardIndex) {
            return upgraded[cardIndex];
        }

        @Override
        public boolean inBottleFlame(int cardIndex) {
            rejectBottleReads();
            return inBottleFlame[cardIndex];
        }

        @Override
        public boolean inBottleLightning(int cardIndex) {
            rejectBottleReads();
            return inBottleLightning[cardIndex];
        }

        @Override
        public boolean inBottleTornado(int cardIndex) {
            rejectBottleReads();
            return inBottleTornado[cardIndex];
        }

        private void rejectBottleReads() {
            if (rejectBottleReads) {
                throw new AssertionError("combat layout must not read bottle flags");
            }
        }
    }
}
