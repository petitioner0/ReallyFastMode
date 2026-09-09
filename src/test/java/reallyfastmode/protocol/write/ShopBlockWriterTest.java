package reallyfastmode.protocol.write;

import org.junit.Test;
import reallyfastmode.protocol.ShopProtocol;
import reallyfastmode.protocol.build.ShopSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ShopBlockWriterTest {
    @Test
    public void writesEveryColumnInShopSlotOrder() {
        ShopSnapshot shop = new ShopSnapshot(2, true, 3, 1, 2);
        shop.cardsWireId[0] = 1;
        shop.cardsWireId[1] = 511;
        shop.cardsPrice[0] = 25;
        shop.cardsPrice[1] = 300;
        shop.relicWireId[0] = 255;
        shop.relicPrice[0] = 511;
        shop.potionWireId[0] = 1;
        shop.potionWireId[1] = 63;
        shop.potionPrice[0] = 0;
        shop.potionPrice[1] = 511;
        BitWriter out = new BitWriter(0);

        ShopBlockWriter.write(out, shop);

        assertEquals(95, out.bitPosition());
        assertArrayEquals(new byte[]{
            0x40, 0x1F, (byte) 0xF8, 0x66, 0x59, 0x37,
            (byte) 0xFF, (byte) 0xFF, 0x03, (byte) 0xF8, 0x03, (byte) 0xFE
        }, out.toByteArray());
    }

    @Test
    public void emptyInventoryStillWritesAllFixedFieldsAndCounts() {
        BitWriter out = new BitWriter(0);

        ShopBlockWriter.write(out, new ShopSnapshot(0, false, 0, 0, 0));

        int fixedBits = ShopProtocol.CARDS_COUNT_BITS
            + ShopProtocol.PURGE_AVAILABLE_BITS
            + ShopProtocol.ACTUAL_PURGE_COST_BITS
            + ShopProtocol.RELIC_COUNT_BITS
            + ShopProtocol.POTION_COUNT_BITS;
        assertEquals(12, fixedBits);
        assertEquals(fixedBits, out.bitPosition());
        assertArrayEquals(new byte[]{0, 0}, out.toByteArray());
    }

    @Test
    public void appendsWithoutAligningTheBlock() {
        BitWriter out = new BitWriter(0);
        out.writeBit(true);

        ShopBlockWriter.write(out, new ShopSnapshot(0, false, 0, 0, 0));

        assertEquals(13, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0x80, 0}, out.toByteArray());
    }

    @Test
    public void rejectsInvalidValuesBeforeWritingAnything() {
        assertRejected(new ShopSnapshot(8, false, 0, 0, 0));
        assertRejected(new ShopSnapshot(0, false, 16, 0, 0));
        assertRejected(new ShopSnapshot(0, false, 0, 4, 0));
        assertRejected(new ShopSnapshot(0, false, 0, 0, 4));

        ShopSnapshot cardWireId = new ShopSnapshot(1, false, 0, 0, 0);
        cardWireId.cardsWireId[0] = 512;
        assertRejected(cardWireId);

        ShopSnapshot cardPrice = new ShopSnapshot(1, false, 0, 0, 0);
        cardPrice.cardsPrice[0] = -1;
        assertRejected(cardPrice);

        ShopSnapshot relicWireId = new ShopSnapshot(0, false, 0, 1, 0);
        relicWireId.relicWireId[0] = 256;
        assertRejected(relicWireId);

        ShopSnapshot potionPrice = new ShopSnapshot(0, false, 0, 0, 1);
        potionPrice.potionPrice[0] = 512;
        assertRejected(potionPrice);
    }

    @Test
    public void rejectsNullArguments() {
        ShopSnapshot shop = new ShopSnapshot(0, false, 0, 0, 0);
        assertThrows(NullPointerException.class,
            () -> ShopBlockWriter.write(null, shop));

        BitWriter out = new BitWriter(0);
        assertThrows(NullPointerException.class,
            () -> ShopBlockWriter.write(out, null));
        assertEquals(0, out.bitPosition());
    }

    private static void assertRejected(ShopSnapshot shop) {
        BitWriter out = new BitWriter(0);
        out.writeBits(0b101, 3);

        assertThrows(IllegalArgumentException.class,
            () -> ShopBlockWriter.write(out, shop));
        assertEquals(3, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0xA0}, out.toByteArray());
    }
}
