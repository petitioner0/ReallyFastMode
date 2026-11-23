package reallyfastmode.protocol.write;

import reallyfastmode.access.GameAccess;
import reallyfastmode.protocol.GameProtocol;
import reallyfastmode.protocol.PacketHeaderProtocol;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.build.GameSnapshotBuilder;
import reallyfastmode.protocol.build.MapSnapshot;
import reallyfastmode.protocol.build.MapSnapshotBuilder;
import reallyfastmode.protocol.io.BitWriter;

import static reallyfastmode.protocol.PacketHeaderProtocol.blockBit;

/** Packs the current floor metadata and the full map for its act. */
public final class MapPacketPacker {
    private static final int MAP_MASK = blockBit(ProtocolSlots.GAME) | blockBit(ProtocolSlots.MAP);

    private MapPacketPacker() {
    }

    /** Must be called on the render thread after the dungeon map has been generated. */
    public static byte[] capture() {
        MapSnapshot map = MapSnapshotBuilder.build();
        int bits = PacketHeaderProtocol.HEADER_BYTES * Byte.SIZE + GameProtocol.GAME_BLOCK_BITS
            + MapBlockWriter.bitSize(map);
        byte[] packet = new byte[(bits + Byte.SIZE - 1) / Byte.SIZE];
        BitWriter out = BitWriter.wrap(packet);
        PacketHeaderWriter.write(out, GameAccess.runId(), MAP_MASK);
        GameBlockWriter.write(out, GameSnapshotBuilder.build());
        MapBlockWriter.write(out, map);
        return packet;
    }
}
