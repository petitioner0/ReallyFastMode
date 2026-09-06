package reallyfastmode.protocol.write;

import org.junit.Test;
import reallyfastmode.protocol.GameProtocol;
import reallyfastmode.protocol.build.GameSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GameBlockWriterTest {
    @Test
    public void writesTheSeventeenBitScalarLayoutInSlotOrder() {
        GameSnapshot game = new GameSnapshot(2, 37, 20, true, false, true);
        BitWriter out = new BitWriter(0);

        GameBlockWriter.write(out, game);

        assertEquals(GameProtocol.GAME_BLOCK_BITS, out.bitPosition());
        assertArrayEquals(new byte[]{0x52, (byte) 0xD2, (byte) 0x80},
            out.toByteArray());
    }

    @Test
    public void appendsWithoutAligningTheBlock() {
        GameSnapshot game = new GameSnapshot(2, 37, 20, true, false, true);
        BitWriter out = new BitWriter(0);
        out.writeBit(true);

        GameBlockWriter.write(out, game);

        assertEquals(1 + GameProtocol.GAME_BLOCK_BITS, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0xA9, 0x69, 0x40}, out.toByteArray());
    }

    @Test
    public void acceptsTheFullConfiguredUnsignedRanges() {
        BitWriter out = new BitWriter(0);

        GameBlockWriter.write(out, new GameSnapshot(4, 63, 31, false, false, false));

        assertEquals(GameProtocol.GAME_BLOCK_BITS, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0x9F, (byte) 0xFC, 0}, out.toByteArray());
    }

    @Test
    public void rejectsInvalidValuesBeforeWritingAnything() {
        assertRejected(new GameSnapshot(-1, 0, 0, false, false, false));
        assertRejected(new GameSnapshot(5, 0, 0, false, false, false));
        assertRejected(new GameSnapshot(0, -1, 0, false, false, false));
        assertRejected(new GameSnapshot(0, 64, 0, false, false, false));
        assertRejected(new GameSnapshot(0, 0, -1, false, false, false));
        assertRejected(new GameSnapshot(0, 0, 32, false, false, false));
    }

    @Test
    public void rejectsNullArguments() {
        GameSnapshot game = new GameSnapshot(0, 0, 0, false, false, false);
        assertThrows(NullPointerException.class, () -> GameBlockWriter.write(null, game));

        BitWriter out = new BitWriter(0);
        assertThrows(NullPointerException.class, () -> GameBlockWriter.write(out, null));
        assertEquals(0, out.bitPosition());
    }

    private static void assertRejected(GameSnapshot game) {
        BitWriter out = new BitWriter(0);
        out.writeBit(true);

        assertThrows(IllegalArgumentException.class,
            () -> GameBlockWriter.write(out, game));
        assertEquals(1, out.bitPosition());
        assertArrayEquals(new byte[]{(byte) 0x80}, out.toByteArray());
    }
}
