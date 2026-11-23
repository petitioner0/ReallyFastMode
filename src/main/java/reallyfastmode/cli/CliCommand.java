package reallyfastmode.cli;

/** The exposed method names and their positional argument counts. */
enum CliCommand {
    START_RUN("startRun seed runId [ascensionLevel [characterId]]", 2, 4),
    PLAY_CARD("playCard cardId [targetId]", 1, 2),
    USE_POTION("usePotion potionSlot [targetId]", 1, 2),
    END_TURN("endTurn", 0),
    SELECT_CARDS("selectCards [cardId ...]", 0, CliWire.MAX_ARGUMENTS - 1),
    SKIP_CARD_SELECTION("skipCardSelection", 0),
    SELECT_OPTION("selectOption optionIndex|restOption", 1),
    SELECT_NODE("selectNode x y", 2),
    CLAIM_REWARD("claimReward rewardIndex", 1),
    OPEN_CHEST("openChest", 0),
    CLAIM_BOSS_RELIC("claimBossRelic relicIndex", 1),
    PROCEED("proceed", 0),
    OPEN_CARD_REMOVAL("openCardRemoval", 0);

    final String usage;
    private final String methodName;
    private final int minArgs;
    private final int maxArgs;

    CliCommand(String usage, int count) {
        this(usage, count, count);
    }

    CliCommand(String usage, int minArgs, int maxArgs) {
        this.usage = usage;
        this.methodName = usage.split(" ", 2)[0];
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
    }

    static CliCommand parse(String[] args) {
        if (args.length != 0) {
            for (CliCommand command : values()) {
                if (command.methodName.equals(args[0])) {
                    int count = args.length - 1;
                    if (count < command.minArgs || count > command.maxArgs) {
                        throw new IllegalArgumentException("Usage: " + command.usage);
                    }
                    return command;
                }
            }
            throw new IllegalArgumentException("Unknown command: " + args[0]);
        }
        throw new IllegalArgumentException("A command is required.");
    }
}
