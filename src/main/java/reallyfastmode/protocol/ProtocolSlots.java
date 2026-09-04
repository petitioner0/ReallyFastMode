package reallyfastmode.protocol;

/**
 * Zero-based slot indexes for the observation protocol.
 *
 * <p>The constants only describe the array layout. Callers create and populate
 * their own {@code Object[]} instances; newly allocated arrays therefore keep
 * every unpopulated slot as {@code null}.</p>
 */
public final class ProtocolSlots {
    public static final int GAME = 0;
    public static final int PLAYER = 1;
    public static final int COMBAT = 2;
    public static final int CARD = 3;
    public static final int MONSTER = 4;
    public static final int POTION = 5;
    public static final int SELECTION = 6;
    public static final int MAP = 7;
    public static final int REWARD = 8;
    public static final int EVENT = 9;
    public static final int SHOP = 10;
    public static final int REST = 11;
    public static final int SIZE = 12;

    private ProtocolSlots() {
    }

    /** Slots corresponding to {@code GameAccess}. */
    public static final class Game {
        public static final int DUNGEON_ID = 0;
        public static final int ACT = 1;
        public static final int FLOOR = 2;
        public static final int SEED = 3;
        public static final int ASCENSION_LEVEL = 4;
        public static final int HAS_RUBY_KEY = 5;
        public static final int HAS_EMERALD_KEY = 6;
        public static final int HAS_SAPPHIRE_KEY = 7;
        public static final int SCREEN = 8;
        public static final int SCREEN_UP = 9;
        public static final int ROOM_PHASE = 10;
        public static final int DUNGEON_BEATEN = 11;
        public static final int SIZE = 12;

        private Game() {
        }
    }

    /** Slots corresponding to {@code PlayerAccess}. */
    public static final class Player {
        public static final int ID = 0;
        public static final int PLAYER_CLASS = 1;
        public static final int HP = 2;
        public static final int MAX_HP = 3;
        public static final int BLOCK = 4;
        public static final int ENERGY = 5;
        public static final int GOLD = 6;
        public static final int GAME_HAND_SIZE = 7;
        public static final int MASTER_HAND_SIZE = 8;
        public static final int POTION_SLOTS = 9;
        public static final int MAX_ORBS = 10;
        public static final int MASTER_MAX_ORBS = 11;
        public static final int POWERS = 12;
        public static final int RELICS = 13;
        public static final int RELICS_COUNT = 14;
        public static final int ORBS = 15;
        public static final int STANCE = 16;
        public static final int SIZE = 17;

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
    public static final class Card {
        public static final int MASTER_DECK = 0;
        public static final int HAND = 1;
        public static final int DISCARD_PILE = 2;
        public static final int EXHAUST_PILE = 3;
        public static final int CARD_ID = 4;
        public static final int TYPE = 5;
        public static final int TARGET = 6;
        public static final int RARITY = 7;
        public static final int COST = 8;
        public static final int COST_FOR_TURN = 9;
        public static final int CHARGE_COST = 10;
        public static final int PRICE = 11;
        public static final int UPGRADED = 12;
        public static final int IN_BOTTLE_FLAME = 13;
        public static final int IN_BOTTLE_LIGHTNING = 14;
        public static final int IN_BOTTLE_TORNADO = 15;
        public static final int SIZE = 16;

        private Card() {
        }
    }

    /** Slots corresponding to {@code MonsterAccess}. */
    public static final class Monster {
        public static final int INSTANCE_ID_4_BIT = 0;
        public static final int NAME = 1;
        public static final int HP = 2;
        public static final int MAX_HP = 3;
        public static final int BLOCK = 4;
        public static final int POWERS = 5;
        public static final int TYPE = 6;
        public static final int INTENT = 7;
        public static final int DAMAGE = 8;
        public static final int INTENT_DAMAGE = 9;
        public static final int INTENT_MULTI_AMOUNT = 10;
        public static final int SIZE = 11;

        private Monster() {
        }
    }

    /** Slots corresponding to {@code PotionAccess}. */
    public static final class Potion {
        public static final int POTIONS = 0;
        public static final int ID = 1;
        public static final int NAME = 2;
        public static final int SLOT = 3;
        public static final int PRICE = 4;
        public static final int SIZE = 5;

        private Potion() {
        }
    }

    /** Slots corresponding to {@code SelectionAccess}. */
    public static final class Selection {
        public static final int CANDIDATES = 0;
        public static final int SIZE = 1;

        private Selection() {
        }
    }

    /** Slots corresponding to {@code MapAccess}. */
    public static final class Map {
        public static final int MAP = 0;
        public static final int CURRENT_NODE = 1;
        public static final int AVAILABLE_MAP_NODES = 2;
        public static final int HAS_EMERALD_KEY = 3;
        public static final int SIZE = 4;

        private Map() {
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
        public static final int EVENT = 0;
        public static final int OPTIONS = 1;
        public static final int SIZE = 2;

        private Event() {
        }
    }

    /** Slots corresponding to {@code ShopAccess}. */
    public static final class Shop {
        public static final int COLORED_CARDS = 0;
        public static final int COLORLESS_CARDS = 1;
        public static final int POTIONS = 2;
        public static final int PURGE_AVAILABLE = 3;
        public static final int ACTUAL_PURGE_COST = 4;
        public static final int RELIC = 5;
        public static final int RELIC_PRICE = 6;
        public static final int POTION = 7;
        public static final int POTION_PRICE = 8;
        public static final int SIZE = 9;

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
