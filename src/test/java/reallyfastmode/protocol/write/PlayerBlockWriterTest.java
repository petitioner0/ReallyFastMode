package reallyfastmode.protocol.write;

import org.junit.Test;
import reallyfastmode.protocol.PlayerProtocol;
import reallyfastmode.protocol.build.PlayerSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PlayerBlockWriterTest {
    @Test
    public void writesScalarsAndLengthPrefixedColumnsInSlotOrder() {
        PlayerSnapshot player = new PlayerSnapshot(
            0x2BC, 0x3EF, 0x234, 7, 0xA67, 2, 2, 2
        );
        player.orbWireId[0] = 3;
        player.orbWireId[1] = 1;
        player.powerWireId[0] = 5;
        player.powerWireId[1] = PlayerProtocol.UNKNOWN_POWER_WIRE_ID;
        player.powerCounts[0] = 2;
        player.powerCounts[1] = -1;
        player.relicWireId[0] = 0;
        player.relicWireId[1] = PlayerProtocol.UNKNOWN_RELIC_WIRE_ID;
        player.relicCounts[0] = -1;
        player.relicCounts[1] = 7;
        BitWriter out = new BitWriter(0);

        PlayerBlockWriter.write(out, player);

        byte[] bytes = out.toByteArray();
        int offset = 0;
        assertEquals(0x2BC, readBits(bytes, offset, 10));
        offset += 10;
        assertEquals(0x3EF, readBits(bytes, offset, 10));
        offset += 10;
        assertEquals(0x234, readBits(bytes, offset, 10));
        offset += 10;
        assertEquals(7, readBits(bytes, offset, 8));
        offset += 8;
        assertEquals(0xA67, readBits(bytes, offset, 12));
        offset += 12;
        assertEquals(2, readBits(bytes, offset, 4));
        offset += 4;
        assertEquals(3, readBits(bytes, offset, 3));
        offset += 3;
        assertEquals(1, readBits(bytes, offset, 3));
        offset += 3;
        assertEquals(5, readBits(bytes, offset, 8));
        offset += 8;
        assertEquals(159, readBits(bytes, offset, 8));
        offset += 8;
        assertEquals(2, readBits(bytes, offset, 12));
        offset += 12;
        assertEquals(0xFFF, readBits(bytes, offset, 12));
        offset += 12;
        assertEquals(0, readBits(bytes, offset, 8));
        offset += 8;
        assertEquals(190, readBits(bytes, offset, 8));
        offset += 8;
        assertEquals(0, readBits(bytes, offset, 8));
        offset += 8;
        assertEquals(7, readBits(bytes, offset, 8));
        offset += 8;
        assertEquals(offset, out.bitPosition());
    }

    @Test
    public void writesOnlyFixedScalarsWhenVariableSectionsAreEmpty() {
        PlayerSnapshot player = new PlayerSnapshot(0, 0, 0, 0, 0, 0, 0, 0);
        BitWriter out = new BitWriter(0);

        PlayerBlockWriter.write(out, player);

        assertEquals(PlayerProtocol.FIXED_SCALAR_BITS, out.bitPosition());
    }

    @Test
    public void acceptsConfiguredNumericBoundaries() {
        PlayerSnapshot player = new PlayerSnapshot(
            1023,
            1023,
            1023,
            255,
            4095,
            PlayerProtocol.MAX_ORBS,
            2,
            2
        );
        player.orbWireId[0] = 7;
        player.powerWireId[0] = 0;
        player.powerWireId[1] = 255;
        player.powerCounts[0] = -2048;
        player.powerCounts[1] = 2047;
        player.relicWireId[0] = 0;
        player.relicWireId[1] = 255;
        player.relicCounts[0] = -1;
        player.relicCounts[1] = 255;

        PlayerBlockWriter.write(new BitWriter(0), player);
    }

    @Test
    public void rejectsInvalidValuesBeforeWritingAnything() {
        PlayerSnapshot invalidHp = new PlayerSnapshot(1024, 0, 0, 0, 0, 0, 0, 0);
        assertRejected(invalidHp);

        PlayerSnapshot invalidBlock = new PlayerSnapshot(0, 0, 1024, 0, 0, 0, 0, 0);
        assertRejected(invalidBlock);

        PlayerSnapshot invalidGold = new PlayerSnapshot(0, 0, 0, 0, 4096, 0, 0, 0);
        assertRejected(invalidGold);

        PlayerSnapshot invalidOrb = new PlayerSnapshot(0, 0, 0, 0, 0, 1, 0, 0);
        invalidOrb.orbWireId[0] = 8;
        assertRejected(invalidOrb);

        PlayerSnapshot invalidPower = new PlayerSnapshot(0, 0, 0, 0, 0, 0, 1, 0);
        invalidPower.powerWireId[0] = 256;
        assertRejected(invalidPower);

        PlayerSnapshot invalidPowerCount = new PlayerSnapshot(0, 0, 0, 0, 0, 0, 1, 0);
        invalidPowerCount.powerCounts[0] = 2048;
        assertRejected(invalidPowerCount);

        PlayerSnapshot invalidRelic = new PlayerSnapshot(0, 0, 0, 0, 0, 0, 0, 1);
        invalidRelic.relicCounts[0] = 256;
        assertRejected(invalidRelic);

        assertThrows(IllegalArgumentException.class,
            () -> new PlayerSnapshot(0, 0, 0, 0, 0, -1, 0, 0));
    }

    @Test
    public void rejectsNullArguments() {
        PlayerSnapshot player = new PlayerSnapshot(0, 0, 0, 0, 0, 0, 0, 0);
        assertThrows(NullPointerException.class, () -> PlayerBlockWriter.write(null, player));

        BitWriter out = new BitWriter(0);
        assertThrows(NullPointerException.class, () -> PlayerBlockWriter.write(out, null));
        assertEquals(0, out.bitPosition());
    }

    private static void assertRejected(PlayerSnapshot player) {
        BitWriter out = new BitWriter(0);
        out.writeBits(0b101, 3);

        assertThrows(IllegalArgumentException.class,
            () -> PlayerBlockWriter.write(out, player));
        assertEquals(3, out.bitPosition());
    }

    private static int readBits(byte[] bytes, int bitOffset, int bitCount) {
        int value = 0;
        for (int i = 0; i < bitCount; i++) {
            int absoluteBit = bitOffset + i;
            int bit = (bytes[absoluteBit >>> 3] >>> (7 - (absoluteBit & 7))) & 1;
            value = (value << 1) | bit;
        }
        return value;
    }
}
