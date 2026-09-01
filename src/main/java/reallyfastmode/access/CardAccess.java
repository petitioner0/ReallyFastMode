package reallyfastmode.access;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Reads cards and card piles from the current player. */
public final class CardAccess {
    private CardAccess() {
    }

    public static List<AbstractCard> masterDeck() {
        return group(Group.MASTER_DECK);
    }

    public static List<AbstractCard> drawPile() {
        return group(Group.DRAW_PILE);
    }

    public static List<AbstractCard> hand() {
        return group(Group.HAND);
    }

    public static List<AbstractCard> discardPile() {
        return group(Group.DISCARD_PILE);
    }

    public static List<AbstractCard> exhaustPile() {
        return group(Group.EXHAUST_PILE);
    }

    public static List<AbstractCard> limbo() {
        return group(Group.LIMBO);
    }

    public static UUID uuid(AbstractCard card) {
        return card(card).uuid;
    }

    public static String uuidString(AbstractCard card) {
        UUID uuid = uuid(card);
        return uuid == null ? null : uuid.toString();
    }

    public static String cardId(AbstractCard card) {
        return card(card).cardID;
    }

    public static String name(AbstractCard card) {
        return card(card).name;
    }

    public static AbstractCard.CardType type(AbstractCard card) {
        return card(card).type;
    }

    public static AbstractCard.CardTarget target(AbstractCard card) {
        return card(card).target;
    }

    public static AbstractCard.CardRarity rarity(AbstractCard card) {
        return card(card).rarity;
    }

    public static AbstractCard.CardColor color(AbstractCard card) {
        return card(card).color;
    }

    public static int cost(AbstractCard card) {
        return card(card).cost;
    }

    public static int costForTurn(AbstractCard card) {
        return card(card).costForTurn;
    }

    public static int chargeCost(AbstractCard card) {
        return card(card).chargeCost;
    }

    public static int price(AbstractCard card) {
        return card(card).price;
    }

    public static boolean upgraded(AbstractCard card) {
        return card(card).upgraded;
    }

    public static boolean inBottleFlame(AbstractCard card) {
        return card(card).inBottleFlame;
    }

    public static boolean inBottleLightning(AbstractCard card) {
        return card(card).inBottleLightning;
    }

    public static boolean inBottleTornado(AbstractCard card) {
        return card(card).inBottleTornado;
    }

    public static boolean canUpgrade(AbstractCard card) {
        return card(card).canUpgrade();
    }

    /**
     * Calls the card's native {@code canUse} query. This does not check hand
     * membership, command readiness, or whether a target is required. Vanilla
     * mutates {@code cantUseMessage}; that known mutation is restored. Custom
     * card overrides may still have side effects that cannot be generalized.
     */
    public static boolean canPlay(AbstractCard card, AbstractMonster target) {
        AbstractCard checkedCard = card(card);
        if (AbstractDungeon.player == null) {
            throw new IllegalStateException("No current player is available.");
        }
        String previousMessage = checkedCard.cantUseMessage;
        try {
            return checkedCard.canUse(AbstractDungeon.player, target);
        } finally {
            checkedCard.cantUseMessage = previousMessage;
        }
    }

    private static AbstractCard card(AbstractCard card) {
        return Objects.requireNonNull(card, "card");
    }

    private static List<AbstractCard> group(Group group) {
        if (AbstractDungeon.player == null) {
            return Collections.emptyList();
        }
        switch (group) {
            case MASTER_DECK:
                return immutableCardGroup(AbstractDungeon.player.masterDeck);
            case DRAW_PILE:
                return immutableCardGroup(AbstractDungeon.player.drawPile);
            case HAND:
                return immutableCardGroup(AbstractDungeon.player.hand);
            case DISCARD_PILE:
                return immutableCardGroup(AbstractDungeon.player.discardPile);
            case EXHAUST_PILE:
                return immutableCardGroup(AbstractDungeon.player.exhaustPile);
            case LIMBO:
                return immutableCardGroup(AbstractDungeon.player.limbo);
            default:
                throw new IllegalStateException("Unsupported card group: " + group);
        }
    }

    private static List<AbstractCard> immutableCardGroup(com.megacrit.cardcrawl.cards.CardGroup group) {
        return group == null ? Collections.<AbstractCard>emptyList() : immutableCopy(group.group);
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }

    private enum Group {
        MASTER_DECK,
        DRAW_PILE,
        HAND,
        DISCARD_PILE,
        EXHAUST_PILE,
        LIMBO
    }
}
