package reallyfastmode.protocol.write;

import reallyfastmode.protocol.CardProtocol;
import reallyfastmode.protocol.build.CardColumns;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Encodes {@link CardColumns} in the fixed Card SoA wire order. */
public final class CardBlockWriter {
    private CardBlockWriter() {
    }

    public static void write(BitWriter out, CardColumns cards) {
        Objects.requireNonNull(out, "out");
        validate(Objects.requireNonNull(cards, "cards"));

        int count = cards.size;
        out.writeBits(count, CardProtocol.CARD_COUNT_BITS);
        for (int i = 0; i < count; i++) {
            out.writeBits(cards.cardWireId[i], CardProtocol.CARD_WIRE_ID_BITS);
        }
        for (int i = 0; i < count; i++) {
            out.writeBits(cards.cost[i], CardProtocol.COST_BITS);
        }
        for (int i = 0; i < count; i++) {
            out.writeBit(cards.upgraded[i]);
        }
        if (cards.layout == CardColumns.Layout.DECK) {
            for (int i = 0; i < count; i++) {
                out.writeBit(cards.inBottleFlame[i]);
            }
            for (int i = 0; i < count; i++) {
                out.writeBit(cards.inBottleLightning[i]);
            }
            for (int i = 0; i < count; i++) {
                out.writeBit(cards.inBottleTornado[i]);
            }
        }
    }

    private static void validate(CardColumns cards) {
        if (cards.size < 0 || cards.size > CardProtocol.MAX_CARDS) {
            throw new IllegalArgumentException("card count=" + cards.size);
        }
        requireLength("cardWireId", cards.cardWireId, cards.size);
        requireLength("cost", cards.cost, cards.size);
        requireLength("upgraded", cards.upgraded, cards.size);
        requireLength("inBottleFlame", cards.inBottleFlame, cards.size);
        requireLength("inBottleLightning", cards.inBottleLightning, cards.size);
        requireLength("inBottleTornado", cards.inBottleTornado, cards.size);

        for (int i = 0; i < cards.size; i++) {
            requireUnsigned("cardWireId[" + i + "]", cards.cardWireId[i],
                CardProtocol.CARD_WIRE_ID_BITS);
            requireSigned("cost[" + i + "]", cards.cost[i], CardProtocol.COST_BITS);
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

    private static void requireLength(String name, boolean[] values, int expected) {
        if (values == null || values.length != expected) {
            throw new IllegalArgumentException(
                name + " length=" + (values == null ? "null" : values.length)
                    + ", expected=" + expected
            );
        }
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
}
