package reallyfastmode.protocol.write;

import reallyfastmode.access.CardAccess;
import reallyfastmode.access.GameAccess;
import reallyfastmode.access.MonsterAccess;
import reallyfastmode.protocol.CombatProtocol;
import reallyfastmode.protocol.CurrentMapSelectionProtocol;
import reallyfastmode.protocol.GameProtocol;
import reallyfastmode.protocol.MonsterProtocol;
import reallyfastmode.protocol.PacketHeaderProtocol;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.build.CardColumns;
import reallyfastmode.protocol.build.CardColumnsBuilder;
import reallyfastmode.protocol.build.CombatSnapshotBuilder;
import reallyfastmode.protocol.build.CurrentMapSelectionSnapshotBuilder;
import reallyfastmode.protocol.build.GameSnapshotBuilder;
import reallyfastmode.protocol.build.MonsterColumns;
import reallyfastmode.protocol.build.MonsterColumnsBuilder;
import reallyfastmode.protocol.build.PlayerSnapshot;
import reallyfastmode.protocol.build.PlayerSnapshotBuilder;
import reallyfastmode.protocol.build.PotionSnapshot;
import reallyfastmode.protocol.build.PotionSnapshotBuilder;
import reallyfastmode.protocol.build.SelectionSnapshot;
import reallyfastmode.protocol.build.SelectionSnapshotBuilder;
import reallyfastmode.protocol.io.BitWriter;

import static reallyfastmode.protocol.PacketHeaderProtocol.blockBit;

/** Captures and encodes a complete battle snapshot in protocol slot order. */
public final class CombatPacketPacker {
    private static final int COMBAT_MASK = blockBit(ProtocolSlots.GAME)
        | blockBit(ProtocolSlots.PLAYER)
        | blockBit(ProtocolSlots.COMBAT)
        | blockBit(ProtocolSlots.DECK_CARD)
        | blockBit(ProtocolSlots.HAND_CARD)
        | blockBit(ProtocolSlots.DRAW_PILE_CARD)
        | blockBit(ProtocolSlots.DISCARD_PILE_CARD)
        | blockBit(ProtocolSlots.EXHAUST_PILE_CARD)
        | blockBit(ProtocolSlots.MONSTER)
        | blockBit(ProtocolSlots.POTION)
        | blockBit(ProtocolSlots.CURRENT_MAP_SELECTION);

    private static final int MONSTER_BITS = MonsterProtocol.INSTANCE_ID_BITS + MonsterProtocol.MONSTER_WIRE_ID_BITS
        + 2 * MonsterProtocol.HP_BITS + MonsterProtocol.BLOCK_BITS + MonsterProtocol.POWER_ENTRY_COUNT_BITS
        + MonsterProtocol.INTENT_WIRE_ID_BITS + MonsterProtocol.INTENT_DAMAGE_BITS
        + MonsterProtocol.INTENT_MULTI_AMOUNT_BITS;

    private CombatPacketPacker() {
    }

    /** Must be called on the render thread in an active combat room. */
    public static byte[] capture(boolean awaitingSelection) {
        PlayerSnapshot player = PlayerSnapshotBuilder.build();
        CardColumns deck = CardColumnsBuilder.buildDeck(CardAccess.masterDeck());
        CardColumns hand = CardColumnsBuilder.buildCombat(CardAccess.hand());
        CardColumns draw = CardColumnsBuilder.buildCombat(CardAccess.drawPile());
        CardColumns discard = CardColumnsBuilder.buildCombat(CardAccess.discardPile());
        CardColumns exhaust = CardColumnsBuilder.buildCombat(CardAccess.exhaustPile());
        MonsterColumns monsters = MonsterColumnsBuilder.build(MonsterAccess.monsters());
        PotionSnapshot potions = PotionSnapshotBuilder.build();
        SelectionSnapshot selection = awaitingSelection ? SelectionSnapshotBuilder.build() : null;

        int mask = COMBAT_MASK;
        int bits = PacketHeaderProtocol.HEADER_BYTES * Byte.SIZE + GameProtocol.GAME_BLOCK_BITS
            + PlayerBlockWriter.bitSize(player) + CombatProtocol.COMBAT_BLOCK_BITS
            + CardBlockWriter.bitSize(deck) + CardBlockWriter.bitSize(hand) + CardBlockWriter.bitSize(draw)
            + CardBlockWriter.bitSize(discard) + CardBlockWriter.bitSize(exhaust)
            + MonsterProtocol.MONSTER_COUNT_BITS + monsters.size * MONSTER_BITS
            + monsters.totalPowerEntries * (MonsterProtocol.POWER_WIRE_ID_BITS + MonsterProtocol.POWER_AMOUNT_BITS)
            + PotionBlockWriter.bitSize(potions) + CurrentMapSelectionProtocol.CURRENT_NODE_BLOCK_BITS;
        if (selection != null) {
            mask |= blockBit(ProtocolSlots.SELECTION);
            bits += SelectionBlockWriter.bitSize(selection);
        }

        byte[] packet = new byte[(bits + Byte.SIZE - 1) / Byte.SIZE];
        BitWriter out = BitWriter.wrap(packet);
        PacketHeaderWriter.write(out, GameAccess.runId(), mask);
        GameBlockWriter.write(out, GameSnapshotBuilder.build());
        PlayerBlockWriter.write(out, player);
        CombatBlockWriter.write(out, CombatSnapshotBuilder.build());
        CardBlockWriter.write(out, deck);
        CardBlockWriter.write(out, hand);
        CardBlockWriter.write(out, draw);
        CardBlockWriter.write(out, discard);
        CardBlockWriter.write(out, exhaust);
        MonsterBlockWriter.write(out, monsters);
        PotionBlockWriter.write(out, potions);
        if (selection != null) {
            SelectionBlockWriter.write(out, selection);
        }
        CurrentMapSelectionBlockWriter.write(out, CurrentMapSelectionSnapshotBuilder.build());
        return packet;
    }
}
