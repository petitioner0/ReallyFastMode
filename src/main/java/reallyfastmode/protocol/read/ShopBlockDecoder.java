package reallyfastmode.protocol.read;

import reallyfastmode.protocol.ShopProtocol;
import reallyfastmode.protocol.io.BitReader;

final class ShopBlockDecoder {
    private ShopBlockDecoder() {
    }

    static PacketRecord.Shop read(BitReader in) {
        int cardCount = in.readBits(ShopProtocol.CARDS_COUNT_BITS);
        String[] cardId = WireIds.readColumn(in, cardCount, ShopProtocol.CARD_WIRE_ID_BITS, WireIds.CARDS);
        int[] cardPrice = readPrices(in, cardCount);

        int purgeAvailable = in.readBits(ShopProtocol.PURGE_AVAILABLE_BITS);
        int purgeCost = in.readBits(ShopProtocol.ACTUAL_PURGE_COST_BITS) * ShopProtocol.PURGE_COST_UNIT;

        int relicCount = in.readBits(ShopProtocol.RELIC_COUNT_BITS);
        String[] relicId = WireIds.readColumn(in, relicCount, ShopProtocol.RELIC_WIRE_ID_BITS, WireIds.RELICS);
        int[] relicPrice = readPrices(in, relicCount);

        int potionCount = in.readBits(ShopProtocol.POTION_COUNT_BITS);
        String[] potionId = WireIds.readColumn(in, potionCount, ShopProtocol.POTION_WIRE_ID_BITS, WireIds.POTIONS);
        int[] potionPrice = readPrices(in, potionCount);

        return new PacketRecord.Shop(cardId, cardPrice, purgeAvailable, purgeCost,
            relicId, relicPrice, potionId, potionPrice);
    }

    private static int[] readPrices(BitReader in, int count) {
        int[] prices = new int[count];
        for (int i = 0; i < prices.length; i++) {
            prices[i] = in.readBits(ShopProtocol.PRICE_BITS);
        }
        return prices;
    }
}
