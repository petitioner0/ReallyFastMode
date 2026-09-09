package reallyfastmode.protocol.build;

import org.junit.Test;
import reallyfastmode.protocol.CardProtocol;
import reallyfastmode.protocol.PlayerProtocol;
import reallyfastmode.protocol.PotionProtocol;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.ShopProtocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ShopSnapshotBuilderTest {
    @Test
    public void capturesRemainingInventoryInDisplayOrder() {
        FakeSource source = new FakeSource(
            new int[]{7, CardProtocol.UNKNOWN_CARD_WIRE_ID, 31},
            new int[]{55, 511, 120},
            true,
            75,
            new int[]{9, PlayerProtocol.UNKNOWN_RELIC_WIRE_ID},
            new int[]{150, 300},
            new int[]{5, PotionProtocol.UNKNOWN_POTION_WIRE_ID},
            new int[]{48, 99}
        );

        ShopSnapshot shop = ShopSnapshotBuilder.buildFromSource(source);

        assertEquals(3, shop.cardsCount);
        assertArrayEquals(new int[]{7, CardProtocol.UNKNOWN_CARD_WIRE_ID, 31},
            shop.cardsWireId);
        assertArrayEquals(new int[]{55, 511, 120}, shop.cardsPrice);
        assertTrue(shop.purgeAvailable);
        assertEquals(3, shop.actualPurgeCost);
        assertEquals(2, shop.relicCount);
        assertArrayEquals(new int[]{9, PlayerProtocol.UNKNOWN_RELIC_WIRE_ID},
            shop.relicWireId);
        assertArrayEquals(new int[]{150, 300}, shop.relicPrice);
        assertEquals(2, shop.potionCount);
        assertArrayEquals(new int[]{5, PotionProtocol.UNKNOWN_POTION_WIRE_ID},
            shop.potionWireId);
        assertArrayEquals(new int[]{48, 99}, shop.potionPrice);
    }

    @Test
    public void acceptsEmptyInventoryAndZeroPurgeCost() {
        ShopSnapshot shop = ShopSnapshotBuilder.buildFromSource(
            new FakeSource(false, 0)
        );

        assertEquals(0, shop.cardsCount);
        assertEquals(0, shop.relicCount);
        assertEquals(0, shop.potionCount);
        assertFalse(shop.purgeAvailable);
        assertEquals(0, shop.actualPurgeCost);
    }

    @Test
    public void acceptsAllMaximumEncodableValues() {
        FakeSource source = new FakeSource(
            filled(ShopProtocol.MAX_CARDS, 511),
            filled(ShopProtocol.MAX_CARDS, ShopProtocol.MAX_PRICE),
            true,
            ShopProtocol.MAX_ACTUAL_PURGE_COST,
            filled(ShopProtocol.MAX_RELICS, 255),
            filled(ShopProtocol.MAX_RELICS, ShopProtocol.MAX_PRICE),
            filled(ShopProtocol.MAX_POTIONS, 63),
            filled(ShopProtocol.MAX_POTIONS, ShopProtocol.MAX_PRICE)
        );

        ShopSnapshot shop = ShopSnapshotBuilder.buildFromSource(source);

        assertEquals(7, shop.cardsCount);
        assertEquals(3, shop.relicCount);
        assertEquals(3, shop.potionCount);
        assertEquals(15, shop.actualPurgeCost);
    }

    @Test
    public void purgeCostUsesIntegerDivisionByTwentyFive() {
        assertEquals(2, ShopSnapshotBuilder.buildFromSource(
            new FakeSource(true, 74)
        ).actualPurgeCost);
        assertEquals(3, ShopSnapshotBuilder.buildFromSource(
            new FakeSource(true, 75)
        ).actualPurgeCost);
    }

    @Test
    public void rejectsCountsOutsideTheirWidthsBeforeReadingItems() {
        assertThrows(NullPointerException.class,
            () -> ShopSnapshotBuilder.buildFromSource(null));
        assertInvalidCounts(-1, 0, 0);
        assertInvalidCounts(8, 0, 0);
        assertInvalidCounts(0, -1, 0);
        assertInvalidCounts(0, 4, 0);
        assertInvalidCounts(0, 0, -1);
        assertInvalidCounts(0, 0, 4);
    }

    @Test
    public void rejectsWireIdsAndPricesOutsideTheirWidths() {
        assertRejected(new FakeSource(
            new int[]{512}, new int[]{0}, false, 0,
            new int[0], new int[0], new int[0], new int[0]
        ));
        assertRejected(new FakeSource(
            new int[]{0}, new int[]{512}, false, 0,
            new int[0], new int[0], new int[0], new int[0]
        ));
        assertRejected(new FakeSource(
            new int[0], new int[0], false, 0,
            new int[]{256}, new int[]{0}, new int[0], new int[0]
        ));
        assertRejected(new FakeSource(
            new int[0], new int[0], false, 0,
            new int[]{0}, new int[]{-1}, new int[0], new int[0]
        ));
        assertRejected(new FakeSource(
            new int[0], new int[0], false, 0,
            new int[0], new int[0], new int[]{64}, new int[]{0}
        ));
        assertRejected(new FakeSource(
            new int[0], new int[0], false, 0,
            new int[0], new int[0], new int[]{0}, new int[]{512}
        ));
    }

    @Test
    public void rejectsPurgeCostsOutsideFourEncodedBits() {
        assertRejected(new FakeSource(true, -1));
        assertRejected(new FakeSource(true, 400));
    }

    @Test
    public void constantsAndSlotsDescribeTheShopColumns() {
        assertEquals(3, ShopProtocol.CARDS_COUNT_BITS);
        assertEquals(CardProtocol.CARD_WIRE_ID_BITS,
            ShopProtocol.CARD_WIRE_ID_BITS);
        assertEquals(2, ShopProtocol.RELIC_COUNT_BITS);
        assertEquals(PlayerProtocol.RELIC_WIRE_ID_BITS,
            ShopProtocol.RELIC_WIRE_ID_BITS);
        assertEquals(2, ShopProtocol.POTION_COUNT_BITS);
        assertEquals(PotionProtocol.POTION_WIRE_ID_BITS,
            ShopProtocol.POTION_WIRE_ID_BITS);
        assertEquals(9, ShopProtocol.PRICE_BITS);
        assertEquals(1, ShopProtocol.PURGE_AVAILABLE_BITS);
        assertEquals(4, ShopProtocol.ACTUAL_PURGE_COST_BITS);
        assertEquals(25, ShopProtocol.PURGE_COST_UNIT);

        assertEquals(0, ProtocolSlots.Shop.CARDS_COUNT);
        assertEquals(1, ProtocolSlots.Shop.CARDS_WIRE_ID);
        assertEquals(2, ProtocolSlots.Shop.CARDS_PRICE);
        assertEquals(3, ProtocolSlots.Shop.PURGE_AVAILABLE);
        assertEquals(4, ProtocolSlots.Shop.ACTUAL_PURGE_COST);
        assertEquals(5, ProtocolSlots.Shop.RELIC_COUNT);
        assertEquals(6, ProtocolSlots.Shop.RELIC_WIRE_ID);
        assertEquals(7, ProtocolSlots.Shop.RELIC_PRICE);
        assertEquals(8, ProtocolSlots.Shop.POTION_COUNT);
        assertEquals(9, ProtocolSlots.Shop.POTION_WIRE_ID);
        assertEquals(10, ProtocolSlots.Shop.POTION_PRICE);
        assertEquals(11, ProtocolSlots.Shop.SIZE);
    }

    private static void assertInvalidCounts(int cards, int relics, int potions) {
        assertThrows(IllegalArgumentException.class,
            () -> ShopSnapshotBuilder.buildFromSource(
                new SizedSource(cards, relics, potions)
            ));
    }

    private static void assertRejected(ShopSnapshotBuilder.Source source) {
        assertThrows(IllegalArgumentException.class,
            () -> ShopSnapshotBuilder.buildFromSource(source));
    }

    private static int[] filled(int size, int value) {
        int[] result = new int[size];
        java.util.Arrays.fill(result, value);
        return result;
    }

    private static final class FakeSource implements ShopSnapshotBuilder.Source {
        private final int[] cardsWireId;
        private final int[] cardsPrice;
        private final boolean purgeAvailable;
        private final int actualPurgeCost;
        private final int[] relicWireId;
        private final int[] relicPrice;
        private final int[] potionWireId;
        private final int[] potionPrice;

        private FakeSource(boolean purgeAvailable, int actualPurgeCost) {
            this(new int[0], new int[0], purgeAvailable, actualPurgeCost,
                new int[0], new int[0], new int[0], new int[0]);
        }

        private FakeSource(
            int[] cardsWireId,
            int[] cardsPrice,
            boolean purgeAvailable,
            int actualPurgeCost,
            int[] relicWireId,
            int[] relicPrice,
            int[] potionWireId,
            int[] potionPrice
        ) {
            this.cardsWireId = cardsWireId;
            this.cardsPrice = cardsPrice;
            this.purgeAvailable = purgeAvailable;
            this.actualPurgeCost = actualPurgeCost;
            this.relicWireId = relicWireId;
            this.relicPrice = relicPrice;
            this.potionWireId = potionWireId;
            this.potionPrice = potionPrice;
        }

        @Override
        public int cardsCount() {
            return cardsWireId.length;
        }

        @Override
        public int cardWireId(int cardIndex) {
            return cardsWireId[cardIndex];
        }

        @Override
        public int cardPrice(int cardIndex) {
            return cardsPrice[cardIndex];
        }

        @Override
        public boolean purgeAvailable() {
            return purgeAvailable;
        }

        @Override
        public int actualPurgeCost() {
            return actualPurgeCost;
        }

        @Override
        public int relicCount() {
            return relicWireId.length;
        }

        @Override
        public int relicWireId(int relicIndex) {
            return relicWireId[relicIndex];
        }

        @Override
        public int relicPrice(int relicIndex) {
            return relicPrice[relicIndex];
        }

        @Override
        public int potionCount() {
            return potionWireId.length;
        }

        @Override
        public int potionWireId(int potionIndex) {
            return potionWireId[potionIndex];
        }

        @Override
        public int potionPrice(int potionIndex) {
            return potionPrice[potionIndex];
        }
    }

    private static final class SizedSource implements ShopSnapshotBuilder.Source {
        private final int cardsCount;
        private final int relicCount;
        private final int potionCount;

        private SizedSource(int cardsCount, int relicCount, int potionCount) {
            this.cardsCount = cardsCount;
            this.relicCount = relicCount;
            this.potionCount = potionCount;
        }

        @Override
        public int cardsCount() {
            return cardsCount;
        }

        @Override
        public int relicCount() {
            return relicCount;
        }

        @Override
        public int potionCount() {
            return potionCount;
        }

        @Override
        public int actualPurgeCost() {
            return 0;
        }

        @Override
        public boolean purgeAvailable() {
            return false;
        }

        @Override
        public int cardWireId(int cardIndex) {
            throw new AssertionError("invalid counts must fail before reading items");
        }

        @Override
        public int cardPrice(int cardIndex) {
            throw new AssertionError("invalid counts must fail before reading items");
        }

        @Override
        public int relicWireId(int relicIndex) {
            throw new AssertionError("invalid counts must fail before reading items");
        }

        @Override
        public int relicPrice(int relicIndex) {
            throw new AssertionError("invalid counts must fail before reading items");
        }

        @Override
        public int potionWireId(int potionIndex) {
            throw new AssertionError("invalid counts must fail before reading items");
        }

        @Override
        public int potionPrice(int potionIndex) {
            throw new AssertionError("invalid counts must fail before reading items");
        }
    }
}
