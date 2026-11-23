package reallyfastmode.cli;

import reallyfastmode.api.CombatApi.CombatApi;
import reallyfastmode.api.CombatApi.CombatApiResult;
import reallyfastmode.api.EventApi.EventApi;
import reallyfastmode.api.MapApi.MapApi;
import reallyfastmode.api.ProceedApi.ProceedApi;
import reallyfastmode.api.RestApi.RestApi;
import reallyfastmode.api.RewardApi.RewardApi;
import reallyfastmode.api.RunApi.RunApi;
import reallyfastmode.api.RunApi.RunApiResult;
import reallyfastmode.api.ShopApi.ShopApi;
import reallyfastmode.cli.CliWire.Reply;
import reallyfastmode.protocol.VanillaCatalog.VanillaRestOptionCatalog;

import java.util.Arrays;
import java.util.Locale;

/** Routes CLI requests to game APIs on the render thread. */
final class GameCommandDispatcher {
    private GameCommandDispatcher() {
    }

    static Reply invoke(String[] args) {
        try {
            switch (CliCommand.parse(args)) {
                case START_RUN:
                    RunApiResult run = startRun(args);
                    return new Reply(run.isSuccess(), run.getCode(), run.getMessage());
                case PLAY_CARD:
                    return reply(CombatApi.playCard(args[1], target(args)));
                case USE_POTION:
                    return reply(CombatApi.usePotion(integer(args[1], "potionSlot"), target(args)));
                case END_TURN:
                    return reply(CombatApi.endTurn());
                case SELECT_CARDS:
                    return reply(CombatApi.selectCards(Arrays.copyOfRange(args, 1, args.length)));
                case SKIP_CARD_SELECTION:
                    return reply(CombatApi.skipCardSelection());
                case SELECT_OPTION:
                    selectOption(args[1]);
                    break;
                case SELECT_NODE:
                    MapApi.selectNode(integer(args[1], "x"), integer(args[2], "y"));
                    break;
                case CLAIM_REWARD:
                    RewardApi.claimReward(integer(args[1], "rewardIndex"));
                    break;
                case OPEN_CHEST:
                    RewardApi.openChest();
                    break;
                case CLAIM_BOSS_RELIC:
                    RewardApi.claimBossRelic(integer(args[1], "relicIndex"));
                    break;
                case PROCEED:
                    ProceedApi.proceed();
                    break;
                case OPEN_CARD_REMOVAL:
                    ShopApi.openCardRemoval();
                    break;
                default:
                    throw new AssertionError("No handler for " + args[0]);
            }
            return Reply.ok();
        } catch (IllegalArgumentException exception) {
            return Reply.error("invalid_argument", exception.getMessage());
        } catch (IllegalStateException exception) {
            return Reply.error("invalid_state", exception.getMessage());
        }
    }

    private static RunApiResult startRun(String[] args) {
        int runId = integer(args[2], "runId");
        if (args.length == 3) {
            return RunApi.startRun(args[1], runId);
        }
        int ascensionLevel = integer(args[3], "ascensionLevel");
        if (args.length == 4) {
            return RunApi.startRun(args[1], runId, ascensionLevel);
        }
        return RunApi.startRun(args[1], runId, ascensionLevel, integer(args[4], "characterId"));
    }

    private static void selectOption(String value) {
        int optionIndex;
        try {
            optionIndex = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            VanillaRestOptionCatalog option;
            try {
                option = VanillaRestOptionCatalog.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException invalidOption) {
                throw new IllegalArgumentException("selectOption requires an integer index or a rest option name.");
            }
            RestApi.selectOption(option);
            return;
        }
        EventApi.selectOption(optionIndex);
    }

    private static String target(String[] args) {
        return args.length == 3 ? args[2] : null;
    }

    private static int integer(String value, String parameter) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(parameter + " must be an integer.");
        }
    }

    private static Reply reply(CombatApiResult result) {
        return new Reply(result.isSuccess(), result.getCode(), result.getMessage());
    }
}
