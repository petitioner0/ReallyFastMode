package reallyfastmode.protocol;

/**
 * Reference-only zero-based ordering for the concrete observation slots.
 *
 * <p>This class documents the intended slot order; it is not an implementation
 * based on {@code Object[]} and must not be used as one. Object arrays do not
 * provide the required performance characteristics, so concrete slot types use
 * this ordering only as a reference.</p>
 */


// TODO(packet-header): Every packet header must contain run_id. Keep run_id
// out of the Game block and do not allocate a Game slot for it.
public final class ProtocolSlots {
    public static final int GAME = 0;
    public static final int PLAYER = 1;
    public static final int COMBAT = 2;
    public static final int DECK_CARD = 3;
    public static final int HAND_CARD = 4;
    public static final int DRAW_PILE_CARD = 5;
    public static final int DISCARD_PILE_CARD = 6;
    public static final int EXHAUST_PILE_CARD = 7;
    public static final int MONSTER = 8;
    public static final int POTION = 9;
    public static final int SELECTION = 10;
    public static final int MAP = 11;
    public static final int REWARD = 12;
    public static final int EVENT = 13;
    public static final int SHOP = 14;
    public static final int REST = 15;
    public static final int SIZE = 16;

    private ProtocolSlots() {
    }

    /** Slots corresponding to {@code GameAccess}. */
    public static final class Game {
        public static final int ACT = 0;
        public static final int FLOOR = 1;
        public static final int ASCENSION_LEVEL = 2;
        public static final int HAS_RUBY_KEY = 3;
        public static final int HAS_EMERALD_KEY = 4;
        public static final int HAS_SAPPHIRE_KEY = 5;
        public static final int SIZE = 6;

        private Game() {
        }
    }

    /** Slots corresponding to {@code PlayerAccess}. */
    public static final class Player {
        public static final int HP = 0;
        public static final int MAX_HP = 1;
        public static final int BLOCK = 2;
        public static final int ENERGY = 3;
        public static final int GOLD = 4;
        public static final int MAX_ORBS = 5;
        public static final int ORBS_WIRE_ID = 6;
        public static final int POWER_ENTRY_COUNT = 7;
        public static final int POWERS_WIRE_ID = 8;
        public static final int POWER_COUNTS = 9;
        public static final int RELIC_ENTRY_COUNT = 10;
        public static final int RELICS_WIRE_ID = 11;
        public static final int RELICS_COUNT = 12;
        public static final int SIZE = 13;

        private Player() {
        }
    }

    /** Slots corresponding to {@code CombatAccess}. */
    public static final class Combat {
        public static final int TURN = 0;
        public static final int SIZE = 1;

        private Combat() {
        }
    }

    /** Slots corresponding to {@code CardAccess}. */
    public static final class DeckCard {
        public static final int CARD_WIRE_ID = 0;
        public static final int COST = 1;
        public static final int UPGRADED = 2;
        public static final int IN_BOTTLE_FLAME = 3;
        public static final int IN_BOTTLE_LIGHTNING = 4;
        public static final int IN_BOTTLE_TORNADO = 5;
        public static final int SIZE = 6;

        private DeckCard() {
        }
    }

    public static final class HandCard {
        public static final int CARD_WIRE_ID = 0;
        public static final int COST_FOR_TURN = 1;
        public static final int UPGRADED = 2;
        public static final int SIZE = 3;

        private HandCard() {
        }
    }

    public static final class DrawPileCard {
        public static final int CARD_WIRE_ID = 0;
        public static final int COST_FOR_TURN = 1;
        public static final int UPGRADED = 2;
        public static final int SIZE = 3;

        private DrawPileCard() {
        }
    }

    public static final class DiscardPileCard {
        public static final int CARD_WIRE_ID = 0;
        public static final int COST_FOR_TURN = 1;
        public static final int UPGRADED = 2;
        public static final int SIZE = 3;

        private DiscardPileCard() {
        }
    }

    public static final class ExhaustPileCard {
        public static final int CARD_WIRE_ID = 0;
        public static final int COST_FOR_TURN = 1;
        public static final int UPGRADED = 2;
        public static final int SIZE = 3;

        private ExhaustPileCard() {
        }
    }

    /** Slots corresponding to {@code MonsterAccess}. */
    public static final class Monster {
        public static final int INSTANCE_ID_4_BIT = 0;
        public static final int MONSTER_WIRE_ID = 1;
        public static final int HP = 2;
        public static final int MAX_HP = 3;
        public static final int BLOCK = 4;
        public static final int POWER_ENTRY_COUNT = 5;
        public static final int POWER_WIRE_ID = 6;
        public static final int POWER_AMOUNT = 7;
        public static final int INTENT_WIRE_ID = 8;
        public static final int INTENT_DAMAGE = 9;
        public static final int INTENT_MULTI_AMOUNT = 10;
        public static final int SIZE = 11;

        private Monster() {
        }
    }

    /** Slots corresponding to {@code PotionAccess}. */
    public static final class Potion {
        public static final int POTION_SLOTS_COUNT = 0;
        public static final int POTION_WIRED_ID = 1;
        public static final int SIZE = 2;

        private Potion() {
        }
    }

    /** Slots corresponding to {@code SelectionAccess}. */
    public static final class Selection {
        public static final int CANDIDATES_CARD_COUNT = 0;
        public static final int CANDIDATES_CARD_WIRE_ID = 1;
        public static final int SIZE = 2;

        private Selection() {
        }
    }

    /** Slots corresponding to {@code MapAccess}. */
    public static final class Map {
        public static final int IS_ACT_4 = 0;
        public static final int FULL_MAP_TYPE = 1;
        public static final int EMERALD_KEY_COORDINATE = 2;
        public static final int CONNECTIVITY = 3;
        public static final int SIZE = 4;

        private Map() {
        }
    }

    public static final class CurrentMapSelection {
        public static final int CURRENT_NODE = 0;
        public static final int SIZE = 1;

        private CurrentMapSelection() {
        }
    }

    /** Slots corresponding to {@code RewardAccess}. */
    public static final class Reward {
        public static final int CARD_REWARD_CHOICES = 0;
        public static final int TYPE = 1;
        public static final int RELIC = 2;
        public static final int POTION = 3;
        public static final int CARDS = 4;
        public static final int SIZE = 5;

        private Reward() {
        }
    }

    /** Slots corresponding to {@code EventAccess}. */
    public static final class Event {
        public static final int EVENT_WIRE_ID = 0;
        public static final int OPTIONS = 1;
        public static final int OPTIONS_APPLICABILITY = 2;
        public static final int SIZE = 3;

        private Event() {
        }
    }

    /** Slots corresponding to {@code ShopAccess}. */
    public static final class Shop {
        public static final int CARDS = 0;
        public static final int CARDS_PRICE = 1;
        public static final int PURGE_AVAILABLE = 2;
        public static final int ACTUAL_PURGE_COST = 3;
        public static final int RELIC = 4;
        public static final int RELIC_PRICE = 5;
        public static final int POTION = 6;
        public static final int POTION_PRICE = 7;
        public static final int SIZE = 8;

        private Shop() {
        }
    }

    /** Slots corresponding to {@code RestAccess}. */
    public static final class Rest {
        public static final int OPTIONS = 0;
        public static final int SIZE = 1;

        private Rest() {
        }
    }
}
