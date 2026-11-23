package reallyfastmode.protocol.write;

import com.megacrit.cardcrawl.rewards.RewardItem;
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
import reallyfastmode.protocol.build.RewardColumns;
import reallyfastmode.protocol.build.RewardColumnsBuilder;
import reallyfastmode.protocol.build.SelectionSnapshot;
import reallyfastmode.protocol.build.SelectionSnapshotBuilder;
import reallyfastmode.protocol.io.BitWriter;

import java.util.List;

import static reallyfastmode.protocol.PacketHeaderProtocol.blockBit;

/** Captures post-combat rewards and the player's state in protocol slot order. */
public final class RewardPacketPacker {
    private static final int REWARD_MASK = blockBit(ProtocolSlots.GAME)
        | blockBit(ProtocolSlots.PLAYER)
        | blockBit(ProtocolSlots.DECK_CARD)
        | blockBit(ProtocolSlots.POTION)
        | blockBit(ProtocolSlots.CURRENT_MAP_SELECTION)
        | blockBit(ProtocolSlots.REWARD);

    private RewardPacketPacker() {
    }

    /** Must be called on the render thread with the reward screen's current list. */
    public static byte[] capture(List<RewardItem> rewardItems, boolean awaitingSelection) {
        PlayerSnapshot player = PlayerSnapshotBuilder.build();
        CardColumns deck = CardColumnsBuilder.buildDeck(CardAccess.masterDeck());
        PotionSnapshot potions = PotionSnapshotBuilder.build();
        RewardColumns rewards = RewardColumnsBuilder.build(rewardItems);
        SelectionSnapshot selection = awaitingSelection ? SelectionSnapshotBuilder.build() : null;

        int mask = REWARD_MASK;
        int bits = PacketHeaderProtocol.HEADER_BYTES * Byte.SIZE + GameProtocol.GAME_BLOCK_BITS
            + PlayerBlockWriter.bitSize(player) + CardBlockWriter.bitSize(deck)
            + PotionBlockWriter.bitSize(potions) + CurrentMapSelectionProtocol.CURRENT_NODE_BLOCK_BITS
            + RewardBlockWriter.bitSize(rewards);
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
        RewardBlockWriter.write(out, rewards);
        return packet;
    }
}
