package reallyfastmode.protocol.read;

import reallyfastmode.protocol.build.CardColumns.Layout;
import reallyfastmode.protocol.build.CombatSnapshot;
import reallyfastmode.protocol.build.CurrentMapSelectionSnapshot;

/** Holds the decoded snapshots for one packet and the latest full map for its run. */
public final class PacketRecord {
    public final int runId;
    Game game;
    Player player;
    CombatSnapshot combat;
    Deck deck;
    CombatCards hand;
    CombatCards drawPile;
    CombatCards discardPile;
    CombatCards exhaustPile;
    Monsters monsters;
    Potions potions;
    Selection selection;
    DungeonMap map;
    CurrentMapSelectionSnapshot currentMapSelection;
    Rewards rewards;
    Event event;
    Shop shop;
    Rest rest;

    PacketRecord(int runId) {
        this.runId = runId;
    }

    public Game game() {
        return game;
    }

    public Player player() {
        return player;
    }

    public CombatSnapshot combat() {
        return combat;
    }

    public Deck deck() {
        return deck;
    }

    public CombatCards hand() {
        return hand;
    }

    public CombatCards drawPile() {
        return drawPile;
    }

    public CombatCards discardPile() {
        return discardPile;
    }

    public CombatCards exhaustPile() {
        return exhaustPile;
    }

    public Monsters monsters() {
        return monsters;
    }

    public Potions potions() {
        return potions;
    }

    public Selection selection() {
        return selection;
    }

    /** Returns the latest full map for this run as of this packet, or null before receiving one. */
    public DungeonMap map() {
        return map;
    }

    public CurrentMapSelectionSnapshot currentMapSelection() {
        return currentMapSelection;
    }

    public Rewards rewards() {
        return rewards;
    }

    public Event event() {
        return event;
    }

    public Shop shop() {
        return shop;
    }

    public Rest rest() {
        return rest;
    }

    public static final class Game {
        public final String actId;
        public final int floor;
        public final int ascensionLevel;
        public final int hasRubyKey;
        public final int hasEmeraldKey;
        public final int hasSapphireKey;

        Game(String actId, int floor, int ascensionLevel, int hasRubyKey,
             int hasEmeraldKey, int hasSapphireKey) {
            this.actId = actId;
            this.floor = floor;
            this.ascensionLevel = ascensionLevel;
            this.hasRubyKey = hasRubyKey;
            this.hasEmeraldKey = hasEmeraldKey;
            this.hasSapphireKey = hasSapphireKey;
        }
    }

    public static final class Player {
        public final int hp;
        public final int maxHp;
        public final int block;
        public final int energy;
        public final int gold;
        public final int maxOrbs;
        public final String[] orbId;
        public final String[] powerId;
        public final int[] powerCounts;
        public final String[] relicId;
        public final int[] relicCounts;

        Player(int hp, int maxHp, int block, int energy, int gold, String[] orbId,
               String[] powerId, int[] powerCounts, String[] relicId, int[] relicCounts) {
            this.hp = hp;
            this.maxHp = maxHp;
            this.block = block;
            this.energy = energy;
            this.gold = gold;
            this.maxOrbs = orbId.length;
            this.orbId = orbId;
            this.powerId = powerId;
            this.powerCounts = powerCounts;
            this.relicId = relicId;
            this.relicCounts = relicCounts;
        }
    }

    public static final class Deck {
        public final Layout layout = Layout.DECK;
        public final int size;
        public final String[] cardId;
        public final int[] cost;
        public final int[] upgraded;
        public final int[] inBottleFlame;
        public final int[] inBottleLightning;
        public final int[] inBottleTornado;

        Deck(int size) {
            this.size = size;
            this.cardId = new String[size];
            this.cost = new int[size];
            this.upgraded = new int[size];
            this.inBottleFlame = new int[size];
            this.inBottleLightning = new int[size];
            this.inBottleTornado = new int[size];
        }
    }

    public static final class CombatCards {
        public final int size;
        public final String[] cardId;
        public final int[] costForTurn;
        public final int[] upgraded;

        CombatCards(int size) {
            this.size = size;
            this.cardId = new String[size];
            this.costForTurn = new int[size];
            this.upgraded = new int[size];
        }
    }

    /** Power columns concatenate each monster's entries in monster order. */
    public static final class Monsters {
        public final int size;
        public final int totalPowerEntries;
        public final int[] instanceId;
        public final String[] monsterId;
        public final int[] hp;
        public final int[] maxHp;
        public final int[] block;
        public final int[] powerEntryCount;
        public final String[] powerId;
        public final int[] powerAmount;
        public final String[] intent;
        public final int[] intentDamage;
        public final int[] intentMultiAmount;

        Monsters(
            int[] instanceId,
            String[] monsterId,
            int[] hp,
            int[] maxHp,
            int[] block,
            int[] powerEntryCount,
            String[] powerId,
            int[] powerAmount,
            String[] intent,
            int[] intentDamage,
            int[] intentMultiAmount
        ) {
            this.size = instanceId.length;
            this.totalPowerEntries = powerId.length;
            this.instanceId = instanceId;
            this.monsterId = monsterId;
            this.hp = hp;
            this.maxHp = maxHp;
            this.block = block;
            this.powerEntryCount = powerEntryCount;
            this.powerId = powerId;
            this.powerAmount = powerAmount;
            this.intent = intent;
            this.intentDamage = intentDamage;
            this.intentMultiAmount = intentMultiAmount;
        }
    }

    public static final class Potions {
        public final int potionSlotsCount;
        public final String[] potionId;

        Potions(String[] potionId) {
            this.potionSlotsCount = potionId.length;
            this.potionId = potionId;
        }
    }

    public static final class Selection {
        public final int candidatesCardCount;
        public final int maxSelectableCards;
        public final String[] candidatesCardId;
        public final int[] candidatesCardUpgraded;

        Selection(int maxSelectableCards, String[] candidatesCardId) {
            this.candidatesCardCount = candidatesCardId.length;
            this.maxSelectableCards = maxSelectableCards;
            this.candidatesCardId = candidatesCardId;
            this.candidatesCardUpgraded = new int[candidatesCardCount];
        }
    }

    /** Full map with row-major node columns, or the Act 4 marker. */
    public static final class DungeonMap {
        public final int isAct4;
        public final int emeraldKeyX;
        public final int emeraldKeyY;
        public final String[] nodeType;
        public final int[] connectsLeft;
        public final int[] connectsCenter;
        public final int[] connectsRight;
        private final MapGraph graph;

        DungeonMap(
            int isAct4,
            int emeraldKeyX,
            int emeraldKeyY,
            String[] nodeType,
            int[] connectsLeft,
            int[] connectsCenter,
            int[] connectsRight
        ) {
            this.isAct4 = isAct4;
            this.emeraldKeyX = emeraldKeyX;
            this.emeraldKeyY = emeraldKeyY;
            this.nodeType = nodeType;
            this.connectsLeft = connectsLeft;
            this.connectsCenter = connectsCenter;
            this.connectsRight = connectsRight;
            this.graph = MapGraph.fromMap(this);
        }

        public MapGraph graph() {
            return graph;
        }
    }

    /** Each payload column follows the order of its matching reward types. */
    public static final class Rewards {
        public final int rewardCount;
        public final String[] rewardType;
        public final String[] relicId;
        public final String[] potionId;
        /** Amounts for both GOLD and STOLEN_GOLD, in reward order. */
        public final int[] golds;

        Rewards(String[] rewardType, String[] relicId, String[] potionId, int[] golds) {
            this.rewardCount = rewardType.length;
            this.rewardType = rewardType;
            this.relicId = relicId;
            this.potionId = potionId;
            this.golds = golds;
        }
    }

    public static final class Event {
        public final String eventId;
        public final int optionsEntryCount;
        public final int[] optionsApplicability;

        Event(String eventId, int[] optionsApplicability) {
            this.eventId = eventId;
            this.optionsEntryCount = optionsApplicability.length;
            this.optionsApplicability = optionsApplicability;
        }
    }

    public static final class Shop {
        public final int cardCount;
        public final String[] cardId;
        public final int[] cardPrice;
        public final int purgeAvailable;
        /** Purge price in gold, rounded down to the encoded 25-gold increment. */
        public final int purgeCost;
        public final int relicCount;
        public final String[] relicId;
        public final int[] relicPrice;
        public final int potionCount;
        public final String[] potionId;
        public final int[] potionPrice;

        Shop(
            String[] cardId,
            int[] cardPrice,
            int purgeAvailable,
            int purgeCost,
            String[] relicId,
            int[] relicPrice,
            String[] potionId,
            int[] potionPrice
        ) {
            this.cardCount = cardId.length;
            this.cardId = cardId;
            this.cardPrice = cardPrice;
            this.purgeAvailable = purgeAvailable;
            this.purgeCost = purgeCost;
            this.relicCount = relicId.length;
            this.relicId = relicId;
            this.relicPrice = relicPrice;
            this.potionCount = potionId.length;
            this.potionId = potionId;
            this.potionPrice = potionPrice;
        }
    }

    public static final class Rest {
        public final String[] availableOptions;

        Rest(String[] availableOptions) {
            this.availableOptions = availableOptions;
        }
    }
}
