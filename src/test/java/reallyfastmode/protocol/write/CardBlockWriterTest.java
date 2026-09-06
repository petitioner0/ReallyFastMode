package reallyfastmode.protocol.write;

import org.junit.Test;
import reallyfastmode.protocol.CardProtocol;
import reallyfastmode.protocol.build.CardColumns;
import reallyfastmode.protocol.io.BitWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CardBlockWriterTest {
    @Test
    public void writesEmptyCardBlockAsZeroCount() {
        BitWriter out = new BitWriter(0);

        CardBlockWriter.write(out, new CardColumns(CardColumns.Layout.COMBAT, 0));

        assertArrayEquals(new byte[]{0}, out.toByteArray());
        assertEquals(CardProtocol.CARD_COUNT_BITS, out.bitPosition());
    }

    @Test
    public void writesCombatColumnsInFixedOrder() {
        CardColumns cards = new CardColumns(CardColumns.Layout.COMBAT, 2);
        cards.cardWireId[0] = 1;
        cards.cardWireId[1] = CardProtocol.UNKNOWN_CARD_WIRE_ID;
        cards.cost[0] = -1;
        cards.cost[1] = -2;
        cards.upgraded[0] = true;
        cards.inBottleFlame[0] = true;
        cards.inBottleLightning[1] = true;
        cards.inBottleTornado[0] = true;

        BitWriter out = new BitWriter(0);
        CardBlockWriter.write(out, cards);

        assertArrayEquals(new byte[]{0x02, 0x00, (byte) 0xEC, 0x3F, (byte) 0xA0},
            out.toByteArray());
        assertEquals(36, out.bitPosition());
    }

    @Test
    public void appendsDeckBottleColumnsWithoutAligning() {
        CardColumns cards = new CardColumns(CardColumns.Layout.DECK, 1);
        cards.cardWireId[0] = 5;
        cards.cost[0] = -1;
        cards.upgraded[0] = true;
        cards.inBottleFlame[0] = true;
        cards.inBottleTornado[0] = true;

        BitWriter out = new BitWriter(0);
        out.writeBits(0b101, 3);
        CardBlockWriter.write(out, cards);

        assertArrayEquals(new byte[]{(byte) 0xA0, 0x20, 0x5F, (byte) 0xD0},
            out.toByteArray());
        assertEquals(28, out.bitPosition());
    }

    @Test
    public void validatesEntireBlockBeforeWriting() {
        CardColumns cards = new CardColumns(CardColumns.Layout.COMBAT, 1);
        cards.cost[0] = 8;
        BitWriter out = new BitWriter(0);
        out.writeBits(0b101, 3);

        assertThrows(IllegalArgumentException.class,
            () -> CardBlockWriter.write(out, cards));
        assertEquals(3, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0xA0}, out.toByteArray());

        CardColumns tooMany = new CardColumns(
            CardColumns.Layout.COMBAT, CardProtocol.MAX_CARDS + 1
        );
        assertThrows(IllegalArgumentException.class,
            () -> CardBlockWriter.write(new BitWriter(0), tooMany));
    }

    @Test
    public void rejectsNullArgumentsAndInvalidValues() {
        CardColumns cards = new CardColumns(CardColumns.Layout.DECK, 1);
        assertThrows(NullPointerException.class,
            () -> CardBlockWriter.write(null, cards));
        assertThrows(NullPointerException.class,
            () -> CardBlockWriter.write(new BitWriter(0), null));
        assertThrows(NullPointerException.class,
            () -> new CardColumns(null, 0));

        cards.cardWireId[0] = 512;
        assertThrows(IllegalArgumentException.class,
            () -> CardBlockWriter.write(new BitWriter(0), cards));

        cards.cardWireId[0] = 0;
        cards.cost[0] = -9;
        assertThrows(IllegalArgumentException.class,
            () -> CardBlockWriter.write(new BitWriter(0), cards));
    }
}
