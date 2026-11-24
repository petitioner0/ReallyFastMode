package reallyfastmode.protocol.write;

import reallyfastmode.access.CardAccess;
import reallyfastmode.access.GameAccess;
import reallyfastmode.protocol.CurrentMapSelectionProtocol;
import reallyfastmode.protocol.GameProtocol;
import reallyfastmode.protocol.PacketHeaderProtocol;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.RestProtocol;
import reallyfastmode.protocol.build.CardColumns;
import reallyfastmode.protocol.build.CardColumnsBuilder;
import reallyfastmode.protocol.build.CurrentMapSelectionSnapshotBuilder;
import reallyfastmode.protocol.build.GameSnapshotBuilder;
import reallyfastmode.protocol.build.PlayerSnapshot;
import reallyfastmode.protocol.build.PlayerSnapshotBuilder;
import reallyfastmode.protocol.build.PotionSnapshot;
import reallyfastmode.protocol.build.PotionSnapshotBuilder;
import reallyfastmode.protocol.build.RestSnapshot;
import reallyfastmode.protocol.build.RestSnapshotBuilder;
import reallyfastmode.protocol.build.SelectionSnapshot;
import reallyfastmode.protocol.build.SelectionSnapshotBuilder;
import reallyfastmode.protocol.io.BitWriter;

import static reallyfastmode.protocol.PacketHeaderProtocol.blockBit;

/** Packs the campfire menu, card selection, or completed room in slot order. */
public final class RestPacketPacker {
    private static final int REST_MASK = blockBit(ProtocolSlots.GAME)
        | blockBit(ProtocolSlots.PLAYER)
        | blockBit(ProtocolSlots.DECK_CARD)
        | blockBit(ProtocolSlots.POTION)
        | blockBit(ProtocolSlots.CURRENT_MAP_SELECTION)
        | blockBit(ProtocolSlots.REST);

    private RestPacketPacker() {
    }

    /** Must be called on the render thread in the current rest room. */
    public static byte[] capture(boolean awaitingSelection) {
        RestSnapshot rest = awaitingSelection ? new RestSnapshot(0) : RestSnapshotBuilder.build();
        return capture(rest, awaitingSelection);
    }

    /** Captures the settled player state after the campfire options are spent. */
    public static byte[] captureSettlement() {
        return capture(new RestSnapshot(0), false);
    }

    private static byte[] capture(RestSnapshot rest, boolean awaitingSelection) {
        PlayerSnapshot player = PlayerSnapshotBuilder.build();
        CardColumns deck = CardColumnsBuilder.buildDeck(CardAccess.masterDeck());
        PotionSnapshot potions = PotionSnapshotBuilder.build();
        SelectionSnapshot selection = awaitingSelection ? SelectionSnapshotBuilder.build() : null;

        int mask = REST_MASK;
        int bits = PacketHeaderProtocol.HEADER_BYTES * Byte.SIZE + GameProtocol.GAME_BLOCK_BITS
            + PlayerBlockWriter.bitSize(player) + CardBlockWriter.bitSize(deck)
            + PotionBlockWriter.bitSize(potions) + CurrentMapSelectionProtocol.CURRENT_NODE_BLOCK_BITS
            + RestProtocol.AVAILABLE_OPTIONS_MASK_BITS;
        if (selection != null) {
            mask |= blockBit(ProtocolSlots.SELECTION);
            bits += SelectionBlockWriter.bitSize(selection);
        }

        byte[] packet = new byte[(bits + Byte.SIZE - 1) / Byte.SIZE];
        BitWriter out = BitWriter.wrap(packet);
        PacketHeaderWriter.write(out, GameAccess.runId(), mask);
        GameBlockWriter.write(out, GameSnapshotBuilder.build());
        PlayerBlockWriter.write(out, player);
        CardBlockWriter.write(out, deck);
        PotionBlockWriter.write(out, potions);
        if (selection != null) {
            SelectionBlockWriter.write(out, selection);
        }
        CurrentMapSelectionBlockWriter.write(out, CurrentMapSelectionSnapshotBuilder.build());
        RestBlockWriter.write(out, rest);
        return packet;
    }
}
