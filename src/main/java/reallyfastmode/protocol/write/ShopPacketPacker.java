package reallyfastmode.protocol.write;

import reallyfastmode.access.CardAccess;
import reallyfastmode.access.GameAccess;
import reallyfastmode.protocol.CurrentMapSelectionProtocol;
import reallyfastmode.protocol.GameProtocol;
import reallyfastmode.protocol.PacketHeaderProtocol;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.build.CardColumns;
import reallyfastmode.protocol.build.CardColumnsBuilder;
import reallyfastmode.protocol.build.CurrentMapSelectionSnapshotBuilder;
import reallyfastmode.protocol.build.GameSnapshotBuilder;
import reallyfastmode.protocol.build.PlayerSnapshot;
import reallyfastmode.protocol.build.PlayerSnapshotBuilder;
import reallyfastmode.protocol.build.PotionSnapshot;
import reallyfastmode.protocol.build.PotionSnapshotBuilder;
import reallyfastmode.protocol.build.SelectionSnapshot;
import reallyfastmode.protocol.build.SelectionSnapshotBuilder;
import reallyfastmode.protocol.build.ShopSnapshot;
import reallyfastmode.protocol.build.ShopSnapshotBuilder;
import reallyfastmode.protocol.io.BitWriter;

import static reallyfastmode.protocol.PacketHeaderProtocol.blockBit;

/** Packs the current shop inventory and player state in protocol slot order. */
public final class ShopPacketPacker {
    private static final int SHOP_MASK = blockBit(ProtocolSlots.GAME)
        | blockBit(ProtocolSlots.PLAYER)
        | blockBit(ProtocolSlots.DECK_CARD)
        | blockBit(ProtocolSlots.POTION)
        | blockBit(ProtocolSlots.CURRENT_MAP_SELECTION)
        | blockBit(ProtocolSlots.SHOP);

    private ShopPacketPacker() {
    }

    /** Must be called on the render thread while the shop screen is active. */
    public static byte[] capture() {
        return capture(null);
    }

    /** Must be called on the render thread while the shop's removal grid is active. */
    public static byte[] captureRemovalSelection() {
        return capture(SelectionSnapshotBuilder.build());
    }

    private static byte[] capture(SelectionSnapshot selection) {
        PlayerSnapshot player = PlayerSnapshotBuilder.build();
        CardColumns deck = CardColumnsBuilder.buildDeck(CardAccess.masterDeck());
        PotionSnapshot potions = PotionSnapshotBuilder.build();
        ShopSnapshot shop = ShopSnapshotBuilder.build();
        int mask = SHOP_MASK;
        int bits = PacketHeaderProtocol.HEADER_BYTES * Byte.SIZE + GameProtocol.GAME_BLOCK_BITS
            + PlayerBlockWriter.bitSize(player) + CardBlockWriter.bitSize(deck)
            + PotionBlockWriter.bitSize(potions) + CurrentMapSelectionProtocol.CURRENT_NODE_BLOCK_BITS
            + ShopBlockWriter.bitSize(shop);
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
        ShopBlockWriter.write(out, shop);
        return packet;
    }
}
