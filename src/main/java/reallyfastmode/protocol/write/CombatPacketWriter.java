package reallyfastmode.protocol.write;

import reallyfastmode.access.CardAccess;
import reallyfastmode.access.GameAccess;
import reallyfastmode.access.MonsterAccess;
import reallyfastmode.protocol.CardProtocol;
import reallyfastmode.protocol.CombatProtocol;
import reallyfastmode.protocol.GameProtocol;
import reallyfastmode.protocol.MonsterProtocol;
import reallyfastmode.protocol.PacketHeaderProtocol;
import reallyfastmode.protocol.PlayerProtocol;
import reallyfastmode.protocol.PotionProtocol;
import reallyfastmode.protocol.ProtocolSlots;
import reallyfastmode.protocol.SelectionProtocol;
import reallyfastmode.protocol.build.CardColumns;
import reallyfastmode.protocol.build.CardColumnsBuilder;
import reallyfastmode.protocol.build.CombatSnapshotBuilder;
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
public final class CombatPacketWriter {
    private static final int COMBAT_MASK = blockBit(ProtocolSlots.GAME)
        | blockBit(ProtocolSlots.PLAYER)
        | blockBit(ProtocolSlots.COMBAT)
        | blockBit(ProtocolSlots.DECK_CARD)
        | blockBit(ProtocolSlots.HAND_CARD)
        | blockBit(ProtocolSlots.DRAW_PILE_CARD)
        | blockBit(ProtocolSlots.DISCARD_PILE_CARD)
        | blockBit(ProtocolSlots.EXHAUST_PILE_CARD)
        | blockBit(ProtocolSlots.MONSTER)
        | blockBit(ProtocolSlots.POTION);

    private static final int CARD_BITS = CardProtocol.CARD_WIRE_ID_BITS + CardProtocol.COST_BITS + 1;
    private static final int MONSTER_BITS = MonsterProtocol.INSTANCE_ID_BITS + MonsterProtocol.MONSTER_WIRE_ID_BITS
        + 2 * MonsterProtocol.HP_BITS + MonsterProtocol.BLOCK_BITS + MonsterProtocol.POWER_ENTRY_COUNT_BITS
        + MonsterProtocol.INTENT_WIRE_ID_BITS + MonsterProtocol.INTENT_DAMAGE_BITS
        + MonsterProtocol.INTENT_MULTI_AMOUNT_BITS;

    private CombatPacketWriter() {
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
            + playerBits(player) + CombatProtocol.COMBAT_BLOCK_BITS
            + 5 * CardProtocol.CARD_COUNT_BITS + deck.size * (CARD_BITS + 3)
            + (hand.size + draw.size + discard.size + exhaust.size) * CARD_BITS
            + MonsterProtocol.MONSTER_COUNT_BITS + monsters.size * MONSTER_BITS
            + monsters.totalPowerEntries * (MonsterProtocol.POWER_WIRE_ID_BITS + MonsterProtocol.POWER_AMOUNT_BITS)
            + PotionProtocol.POTION_SLOTS_COUNT_BITS + potions.potionSlotsCount * PotionProtocol.POTION_WIRE_ID_BITS;
        if (selection != null) {
            mask |= blockBit(ProtocolSlots.SELECTION);
            bits += SelectionProtocol.CANDIDATES_CARD_COUNT_BITS + SelectionProtocol.MAX_SELECTABLE_CARDS_BITS
                + selection.candidatesCardCount * (SelectionProtocol.CANDIDATES_CARD_WIRE_ID_BITS + 1);
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
        return packet;
    }

    private static int playerBits(PlayerSnapshot player) {
        return PlayerProtocol.FIXED_SCALAR_BITS
            + player.maxOrbs * PlayerProtocol.ORB_WIRE_ID_BITS
            + PlayerProtocol.POWER_ENTRY_COUNT_BITS
            + player.powerWireId.length * (PlayerProtocol.POWER_WIRE_ID_BITS + PlayerProtocol.POWER_COUNT_BITS)
            + PlayerProtocol.RELIC_ENTRY_COUNT_BITS
            + player.relicWireId.length * (PlayerProtocol.RELIC_WIRE_ID_BITS + PlayerProtocol.RELIC_COUNT_BITS);
    }
}
