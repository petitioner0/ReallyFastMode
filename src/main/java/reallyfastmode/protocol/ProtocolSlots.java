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
        public static final int IN_DUNGEON = 0;
        public static final int DUNGEON_ID = 1;
        public static final int DUNGEON_NAME = 2;
        public static final int ACT = 3;
        public static final int FLOOR = 4;
        public static final int SEED = 5;
        public static final int ASCENSION_LEVEL = 6;
        public static final int ASCENSION_MODE = 7;
        public static final int HAS_RUBY_KEY = 8;
        public static final int HAS_EMERALD_KEY = 9;
        public static final int HAS_SAPPHIRE_KEY = 10;
        public static final int SCREEN = 11;
        public static final int SCREEN_UP = 12;
        public static final int ROOM_PHASE = 13;
        public static final int DUNGEON_BEATEN = 14;
        public static final int SIZE = 15;

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
        public static final int ORBS = 14;
        public static final int STANCE = 15;
        public static final int SIZE = 16;

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
        public static final int DRAW_PILE = 1;
        public static final int HAND = 2;
        public static final int DISCARD_PILE = 3;
        public static final int EXHAUST_PILE = 4;
        public static final int LIMBO = 5;
        public static final int UUID = 6;
        public static final int UUID_STRING = 7;
        public static final int CARD_ID = 8;
        public static final int NAME = 9;
        public static final int TYPE = 10;
        public static final int TARGET = 11;
        public static final int RARITY = 12;
        public static final int COLOR = 13;
        public static final int COST = 14;
        public static final int COST_FOR_TURN = 15;
        public static final int CHARGE_COST = 16;
        public static final int PRICE = 17;
        public static final int UPGRADED = 18;
        public static final int IN_BOTTLE_FLAME = 19;
        public static final int IN_BOTTLE_LIGHTNING = 20;
        public static final int IN_BOTTLE_TORNADO = 21;
        public static final int CAN_UPGRADE = 22;
        public static final int CAN_PLAY = 23;
        public static final int SIZE = 24;

        private Card() {
        }
    }

    /** Slots corresponding to {@code MonsterAccess}. */
    public static final class Monster {
        public static final int MONSTERS = 0;
        public static final int LIVING_MONSTERS = 1;
        public static final int ID = 2;
        public static final int INSTANCE_ID_4_BIT = 3;
        public static final int NAME = 4;
        public static final int HP = 5;
        public static final int MAX_HP = 6;
        public static final int BLOCK = 7;
        public static final int POWERS = 8;
        public static final int TYPE = 9;
        public static final int INTENT = 10;
        public static final int DAMAGE = 11;
        public static final int INTENT_DAMAGE = 12;
        public static final int INTENT_BASE_DAMAGE = 13;
        public static final int INTENT_MULTI_AMOUNT = 14;
        public static final int MULTI_DAMAGE_INTENT = 15;
        public static final int SIZE = 16;

        private Monster() {
        }
    }

    /** Slots corresponding to {@code PotionAccess}. */
    public static final class Potion {
        public static final int POTIONS = 0;
        public static final int SLOT_COUNT = 1;
        public static final int EMPTY_SLOT = 2;
        public static final int ID = 3;
        public static final int NAME = 4;
        public static final int SLOT = 5;
        public static final int PRICE = 6;
        public static final int CAN_USE = 7;
        public static final int CAN_DISCARD = 8;
        public static final int SIZE = 9;

        private Potion() {
        }
    }

    /** Slots corresponding to {@code SelectionAccess}. */
    public static final class Selection {
        public static final int SCREEN = 0;
        public static final int CANDIDATES = 1;
        public static final int SIZE = 2;

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
        public static final int ROOM_REWARDS = 0;
        public static final int COMBAT_REWARDS = 1;
        public static final int CARD_REWARD_CHOICES = 2;
        public static final int CARD_REWARD_TAKEN_ALL = 3;
        public static final int TYPE = 4;
        public static final int GOLD = 5;
        public static final int BONUS_GOLD = 6;
        public static final int RELIC_LINK = 7;
        public static final int RELIC = 8;
        public static final int POTION = 9;
        public static final int CARDS = 10;
        public static final int DONE = 11;
        public static final int IGNORED = 12;
        public static final int SIZE = 13;

        private Reward() {
        }
    }

    /** Slots corresponding to {@code EventAccess}. */
    public static final class Event {
        public static final int IN_EVENT_ROOM = 0;
        public static final int EVENT = 1;
        public static final int COMBAT_TIME = 2;
        public static final int OPTIONS = 3;
        public static final int SELECTED_OPTION = 4;
        public static final int SIZE = 5;

        private Event() {
        }
    }

    /** Slots corresponding to {@code ShopAccess}. */
    public static final class Shop {
        public static final int COLORED_CARDS = 0;
        public static final int COLORLESS_CARDS = 1;
        public static final int CARDS = 2;
        public static final int RELICS = 3;
        public static final int POTIONS = 4;
        public static final int PURGE_AVAILABLE = 5;
        public static final int PURGE_COST = 6;
        public static final int ACTUAL_PURGE_COST = 7;
        public static final int RELIC = 8;
        public static final int RELIC_PRICE = 9;
        public static final int POTION = 10;
        public static final int POTION_PRICE = 11;
        public static final int SIZE = 12;

        private Shop() {
        }
    }

    /** Slots corresponding to {@code RestAccess}. */
    public static final class Rest {
        public static final int IN_REST_ROOM = 0;
        public static final int REST_ROOM = 1;
        public static final int CAMPFIRE = 2;
        public static final int SOMETHING_SELECTED = 3;
        public static final int OPTIONS = 4;
        public static final int USABLE = 5;
        public static final int SIZE = 6;

        private Rest() {
        }
    }
}
