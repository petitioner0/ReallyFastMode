package reallyfastmode.protocol.write;

import reallyfastmode.protocol.ShopProtocol;
import reallyfastmode.protocol.build.ShopSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes a {@link ShopSnapshot} in the fixed Shop slot order. */
public final class ShopBlockWriter {
    private ShopBlockWriter() {
    }

    public static void write(BitWriter out, ShopSnapshot shop) {
        Objects.requireNonNull(out, "out");
        validate(Objects.requireNonNull(shop, "shop"));

        out.writeBits(shop.cardsCount, ShopProtocol.CARDS_COUNT_BITS);
        writeValues(out, shop.cardsWireId, ShopProtocol.CARD_WIRE_ID_BITS);
        writeValues(out, shop.cardsPrice, ShopProtocol.PRICE_BITS);
        out.writeBit(shop.purgeAvailable);
        out.writeBits(shop.actualPurgeCost, ShopProtocol.ACTUAL_PURGE_COST_BITS);
        out.writeBits(shop.relicCount, ShopProtocol.RELIC_COUNT_BITS);
        writeValues(out, shop.relicWireId, ShopProtocol.RELIC_WIRE_ID_BITS);
        writeValues(out, shop.relicPrice, ShopProtocol.PRICE_BITS);
        out.writeBits(shop.potionCount, ShopProtocol.POTION_COUNT_BITS);
        writeValues(out, shop.potionWireId, ShopProtocol.POTION_WIRE_ID_BITS);
        writeValues(out, shop.potionPrice, ShopProtocol.PRICE_BITS);
    }

    private static void validate(ShopSnapshot shop) {
        requireCount("cardsCount", shop.cardsCount, ShopProtocol.MAX_CARDS);
        requireLength("cardsWireId", shop.cardsWireId, shop.cardsCount);
        requireLength("cardsPrice", shop.cardsPrice, shop.cardsCount);
        requireValues("cardsWireId", shop.cardsWireId,
            ShopProtocol.CARD_WIRE_ID_BITS);
        requireValues("cardsPrice", shop.cardsPrice, ShopProtocol.PRICE_BITS);

        requireUnsigned("actualPurgeCost", shop.actualPurgeCost,
            ShopProtocol.ACTUAL_PURGE_COST_BITS);

        requireCount("relicCount", shop.relicCount, ShopProtocol.MAX_RELICS);
        requireLength("relicWireId", shop.relicWireId, shop.relicCount);
        requireLength("relicPrice", shop.relicPrice, shop.relicCount);
        requireValues("relicWireId", shop.relicWireId,
            ShopProtocol.RELIC_WIRE_ID_BITS);
        requireValues("relicPrice", shop.relicPrice, ShopProtocol.PRICE_BITS);

        requireCount("potionCount", shop.potionCount, ShopProtocol.MAX_POTIONS);
        requireLength("potionWireId", shop.potionWireId, shop.potionCount);
        requireLength("potionPrice", shop.potionPrice, shop.potionCount);
        requireValues("potionWireId", shop.potionWireId,
            ShopProtocol.POTION_WIRE_ID_BITS);
        requireValues("potionPrice", shop.potionPrice, ShopProtocol.PRICE_BITS);
    }

    private static void writeValues(BitWriter out, int[] values, int bits) {
        for (int value : values) {
            out.writeBits(value, bits);
        }
    }

    private static void requireCount(String name, int value, int max) {
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }

    private static void requireLength(String name, int[] values, int expected) {
        if (values == null || values.length != expected) {
            throw new IllegalArgumentException(
                name + " length=" + (values == null ? "null" : values.length)
                    + ", expected=" + expected
            );
        }
    }

    private static void requireValues(String name, int[] values, int bits) {
        for (int i = 0; i < values.length; i++) {
            requireUnsigned(name + "[" + i + "]", values[i], bits);
        }
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }
}
