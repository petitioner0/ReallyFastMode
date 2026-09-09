package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import reallyfastmode.access.CardAccess;
import reallyfastmode.access.PlayerAccess;
import reallyfastmode.access.PotionAccess;
import reallyfastmode.access.ShopAccess;
import reallyfastmode.protocol.ShopProtocol;

import java.util.List;
import java.util.Objects;

/** Captures the shop's remaining cards, relics, potions, and purge service. */
public final class ShopSnapshotBuilder {
    private ShopSnapshotBuilder() {
    }

    public static ShopSnapshot build() {
        return buildFromSource(new StsSource());
    }

    static ShopSnapshot buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int cardsCount = requireCount(
            "cardsCount", source.cardsCount(), ShopProtocol.MAX_CARDS
        );
        int relicCount = requireCount(
            "relicCount", source.relicCount(), ShopProtocol.MAX_RELICS
        );
        int potionCount = requireCount(
            "potionCount", source.potionCount(), ShopProtocol.MAX_POTIONS
        );
        int actualPurgeCost = encodeActualPurgeCost(source.actualPurgeCost());

        ShopSnapshot result = new ShopSnapshot(
            cardsCount,
            source.purgeAvailable(),
            actualPurgeCost,
            relicCount,
            potionCount
        );
        for (int i = 0; i < cardsCount; i++) {
            int wireId = source.cardWireId(i);
            requireUnsigned("cardsWireId[" + i + "]", wireId,
                ShopProtocol.CARD_WIRE_ID_BITS);
            result.cardsWireId[i] = wireId;
            result.cardsPrice[i] = requirePrice("cardsPrice[" + i + "]",
                source.cardPrice(i));
        }
        for (int i = 0; i < relicCount; i++) {
            int wireId = source.relicWireId(i);
            requireUnsigned("relicWireId[" + i + "]", wireId,
                ShopProtocol.RELIC_WIRE_ID_BITS);
            result.relicWireId[i] = wireId;
            result.relicPrice[i] = requirePrice("relicPrice[" + i + "]",
                source.relicPrice(i));
        }
        for (int i = 0; i < potionCount; i++) {
            int wireId = source.potionWireId(i);
            requireUnsigned("potionWireId[" + i + "]", wireId,
                ShopProtocol.POTION_WIRE_ID_BITS);
            result.potionWireId[i] = wireId;
            result.potionPrice[i] = requirePrice("potionPrice[" + i + "]",
                source.potionPrice(i));
        }
        return result;
    }

    private static int requireCount(String name, int value, int max) {
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
        return value;
    }

    private static int requirePrice(String name, int value) {
        requireUnsigned(name, value, ShopProtocol.PRICE_BITS);
        return value;
    }

    private static int encodeActualPurgeCost(int value) {
        if (value < 0 || value > ShopProtocol.MAX_ACTUAL_PURGE_COST) {
            throw new IllegalArgumentException(
                "actualPurgeCost=" + value
                    + " outside 0-" + ShopProtocol.MAX_ACTUAL_PURGE_COST
            );
        }
        return value / ShopProtocol.PURGE_COST_UNIT;
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }

    interface Source {
        int cardsCount();

        int cardWireId(int cardIndex);

        int cardPrice(int cardIndex);

        boolean purgeAvailable();

        int actualPurgeCost();

        int relicCount();

        int relicWireId(int relicIndex);

        int relicPrice(int relicIndex);

        int potionCount();

        int potionWireId(int potionIndex);

        int potionPrice(int potionIndex);
    }

    private static final class StsSource implements Source {
        private final List<AbstractCard> cards;
        private final boolean purgeAvailable;
        private final int actualPurgeCost;
        private final List<StoreRelic> relics;
        private final List<StorePotion> potions;

        private StsSource() {
            this.cards = ShopAccess.cards();
            this.purgeAvailable = ShopAccess.purgeAvailable();
            this.actualPurgeCost = ShopAccess.actualPurgeCost();
            this.relics = ShopAccess.relics();
            this.potions = ShopAccess.potions();
        }

        @Override
        public int cardsCount() {
            return cards.size();
        }

        @Override
        public int cardWireId(int cardIndex) {
            return CardAccess.wireId(card(cardIndex));
        }

        @Override
        public int cardPrice(int cardIndex) {
            return CardAccess.price(card(cardIndex));
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
            return relics.size();
        }

        @Override
        public int relicWireId(int relicIndex) {
            return PlayerAccess.relicWireId(ShopAccess.relic(relic(relicIndex)));
        }

        @Override
        public int relicPrice(int relicIndex) {
            return ShopAccess.price(relic(relicIndex));
        }

        @Override
        public int potionCount() {
            return potions.size();
        }

        @Override
        public int potionWireId(int potionIndex) {
            return PotionAccess.wireId(ShopAccess.potion(potion(potionIndex)));
        }

        @Override
        public int potionPrice(int potionIndex) {
            return ShopAccess.price(potion(potionIndex));
        }

        private AbstractCard card(int index) {
            return Objects.requireNonNull(cards.get(index), "cards[" + index + "]");
        }

        private StoreRelic relic(int index) {
            return Objects.requireNonNull(relics.get(index), "relics[" + index + "]");
        }

        private StorePotion potion(int index) {
            return Objects.requireNonNull(potions.get(index), "potions[" + index + "]");
        }
    }
}
