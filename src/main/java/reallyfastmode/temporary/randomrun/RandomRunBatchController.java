package reallyfastmode.temporary.randomrun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.rooms.TreasureRoom;
import com.megacrit.cardcrawl.screens.select.BossRelicSelectScreen;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.buttons.LargeDialogOptionButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.access.CardAccess;
import reallyfastmode.access.EventAccess;
import reallyfastmode.access.MapAccess;
import reallyfastmode.access.MonsterAccess;
import reallyfastmode.access.RewardAccess;
import reallyfastmode.api.CombatApi.CombatApi;
import reallyfastmode.api.CombatApi.CombatApiResult;
import reallyfastmode.api.CombatApi.CombatIdentifiers;
import reallyfastmode.api.MapApi.MapApi;
import reallyfastmode.api.RestApi.RestApi;
import reallyfastmode.api.RewardApi.RewardApi;
import reallyfastmode.api.RunApi.RunApi;
import reallyfastmode.api.RunApi.RunApiResult;
import reallyfastmode.patches.access.PrivateFieldAccess;
import reallyfastmode.patches.access.RunIdPatches;
import reallyfastmode.protocol.VanillaCatalog.VanillaRestOptionCatalog;

import java.io.IOException;
import java.util.List;

/** One-action-per-frame controller for the temporary 100-seed random run. */
final class RandomRunBatchController {
    private static final Logger LOGGER =
        LogManager.getLogger(RandomRunBatchController.class.getName());

    private static ControllerState state = ControllerState.IDLE;
    private static SeedBatchCsv batch;
    private static int currentIndex = -1;
    private static Random mapRandom;
    private static RunPerformanceMetrics.Snapshot performanceStart;

    private RandomRunBatchController() {
    }

    static void update() {
        try {
            if (state == ControllerState.IDLE) {
                if (isBatchArmReady() && isSpaceJustPressed()) {
                    beginOrResumeBatch();
                }
                return;
            }

            if (state == ControllerState.WAITING_TO_START) {
                tryStartNextRun();
                return;
            }

            if (isTerminalScreen()) {
                finishCurrentRun();
                return;
            }

            if (CardCrawlGame.mode != CardCrawlGame.GameMode.GAMEPLAY
                || CardCrawlGame.dungeon == null
                || AbstractDungeon.player == null) {
                return;
            }

            handleOneGameAction();
        } catch (IOException exception) {
            stop("Seed CSV operation failed.", exception);
        } catch (RuntimeException exception) {
            stop("Temporary random-run controller stopped on an unrecoverable state.", exception);
        }
    }

    private static void beginOrResumeBatch() throws IOException {
        batch = SeedBatchCsv.openDefault();
        int next = batch.firstIncompleteIndex();
        if (next < 0) {
            LOGGER.info("Temporary random-run batch is already complete: {}", batch.path());
            return;
        }

        state = ControllerState.WAITING_TO_START;
        LOGGER.info(
            "Temporary random-run batch armed at seed {}/{}: {}",
            next + 1,
            batch.size(),
            batch.path()
        );
        tryStartNextRun();
    }

    private static void tryStartNextRun() {
        prepareMainMenuForRunStart();
        if (!isRunStartReady()) {
            return;
        }

        currentIndex = batch.firstIncompleteIndex();
        if (currentIndex < 0) {
            state = ControllerState.IDLE;
            LOGGER.info("Temporary random-run batch completed all {} seeds.", batch.size());
            return;
        }

        String seed = batch.seedAt(currentIndex);
        RunPerformanceMetrics.Snapshot pendingPerformanceStart =
            RunPerformanceMetrics.start();
        RunApiResult result = RunApi.startRun(seed, 0);
        if (!result.isSuccess()) {
            throw new IllegalStateException(
                "RunApi rejected seed " + seed + ": "
                    + result.getCode() + " - " + result.getMessage()
            );
        }

        long mapSeed = SeedBatchCsv.FIXED_RANDOM_SEED ^ SeedHelper.getLong(seed);
        mapRandom = new Random(Long.valueOf(mapSeed));
        performanceStart = pendingPerformanceStart;
        state = ControllerState.RUNNING;
        LOGGER.info(
            "Temporary random-run started seed {}/{}: {}",
            currentIndex + 1,
            batch.size(),
            seed
        );
    }

    private static void finishCurrentRun() throws IOException {
        if (currentIndex < 0) {
            throw new IllegalStateException("A terminal screen was reached without an active seed.");
        }

        String completedSeed = batch.seedAt(currentIndex);
        RunPerformanceMetrics.Result performance =
            RunPerformanceMetrics.finish(performanceStart);
        batch.markCompleted(
            currentIndex,
            performance.elapsedMillis(),
            performance.processCpuTimeMillis()
        );
        LOGGER.info(
            "Temporary random-run completed seed {}/{}: {} ({} ms elapsed, {} ms process CPU)",
            currentIndex + 1,
            batch.size(),
            completedSeed,
            performance.elapsedMillis(),
            performance.processCpuTimeMillis()
        );

        currentIndex = -1;
        mapRandom = null;
        performanceStart = null;
        state = ControllerState.WAITING_TO_START;
        CardCrawlGame.startOver();
    }

    private static void handleOneGameAction() {
        switch (AbstractDungeon.screen) {
            case FTUE:
                AbstractDungeon.closeCurrentScreen();
                return;
            case HAND_SELECT:
                TemporarySelectionDriver.handleHandSelection();
                return;
            case GRID:
                TemporarySelectionDriver.handleGridSelection();
                return;
            case CARD_REWARD:
                handleCardRewardSelection();
                return;
            case COMBAT_REWARD:
                handleRewards();
                return;
            case BOSS_REWARD:
                handleBossRelic();
                return;
            case SHOP:
                AbstractDungeon.closeCurrentScreen();
                return;
            case MAP:
                handleMapSelection();
                return;
            case NONE:
                break;
            default:
                return;
        }

        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null) {
            return;
        }

        if (room instanceof TreasureRoom) {
            TreasureRoom treasureRoom = (TreasureRoom) room;
            if (treasureRoom.chest == null) {
                return;
            }
            if (!treasureRoom.chest.isOpen) {
                if (!TemporaryGameAccess.openChest(treasureRoom.chest)) {
                    throw new IllegalStateException("The current treasure chest cannot be opened.");
                }
                return;
            }
        }

        if (room.phase == AbstractRoom.RoomPhase.EVENT && room.event != null) {
            handleEvent(room.event);
            return;
        }

        if (room instanceof RestRoom
            && room.phase == AbstractRoom.RoomPhase.INCOMPLETE) {
            handleRest((RestRoom) room);
            return;
        }

        if (room.phase == AbstractRoom.RoomPhase.COMPLETE) {
            TemporaryGameAccess.queueProceed(AbstractDungeon.overlayMenu.proceedButton);
            return;
        }

        if (room.phase == AbstractRoom.RoomPhase.COMBAT) {
            handleCombat();
        }
    }

    private static void handleCardRewardSelection() {
        if (AbstractDungeon.cardRewardScreen == null) {
            throw new IllegalStateException("Card reward screen is unavailable.");
        }
        if (PrivateFieldAccess.cardRewardSkippable(AbstractDungeon.cardRewardScreen)) {
            TemporaryGameAccess.queueCardRewardSkip(AbstractDungeon.cardRewardScreen);
            return;
        }
        TemporaryGameAccess.chooseFirstCardReward(AbstractDungeon.cardRewardScreen);
    }

    private static void handleRewards() {
        List<RewardItem> rewards = RewardAccess.combatRewards();
        int claimIndex = firstClaimableReward(rewards);
        if (claimIndex >= 0) {
            RewardApi.claimReward(claimIndex);
            return;
        }
        TemporaryGameAccess.queueProceed(AbstractDungeon.overlayMenu.proceedButton);
    }

    static int firstClaimableReward(List<RewardItem> rewards) {
        for (int index = 0; index < rewards.size(); ++index) {
            RewardItem.RewardType type = rewards.get(index).type;
            if (isClaimableRewardType(type)) {
                return index;
            }
        }
        return -1;
    }

    static boolean isClaimableRewardType(RewardItem.RewardType type) {
        return type == RewardItem.RewardType.GOLD
            || type == RewardItem.RewardType.STOLEN_GOLD
            || type == RewardItem.RewardType.RELIC;
    }

    private static void handleBossRelic() {
        BossRelicSelectScreen screen = AbstractDungeon.bossRelicScreen;
        if (screen == null || screen.relics == null || screen.relics.isEmpty()) {
            throw new IllegalStateException("Boss relic selection has no relic candidates.");
        }

        AbstractRelic relic = screen.relics.get(0);
        if (!"Black Blood".equals(relic.relicId)
            && !"Ring of the Serpent".equals(relic.relicId)) {
            relic.obtain();
        }
        relic.isObtained = true;
    }

    private static void handleMapSelection() {
        if (!isMapSelectionReady()) {
            return;
        }
        List<MapRoomNode> available = MapAccess.availableMapNodes();
        if (available.isEmpty()) {
            if (AbstractDungeon.map != null
                && AbstractDungeon.currMapNode != null
                && AbstractDungeon.getCurrRoom() != null
                && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMPLETE
                && !AbstractDungeon.isFadingIn
                && !AbstractDungeon.isFadingOut) {
                throw new IllegalStateException("The map has no reachable nodes.");
            }
            return;
        }
        if (mapRandom == null) {
            throw new IllegalStateException("Map RNG is unavailable for the active seed.");
        }

        MapRoomNode selected = available.get(randomMapIndex(mapRandom, available.size()));
        if (selected.room instanceof MonsterRoomBoss) {
            if (AbstractDungeon.dungeonMapScreen.map == null
                || !TemporaryGameAccess.enterBoss(AbstractDungeon.dungeonMapScreen.map)) {
                throw new IllegalStateException("The boss map node cannot be entered.");
            }
            return;
        }
        MapApi.selectNode(selected.x, selected.y);
    }

    private static boolean isMapSelectionReady() {
        if (CardCrawlGame.mode != CardCrawlGame.GameMode.GAMEPLAY
            || AbstractDungeon.currMapNode == null
            || AbstractDungeon.getCurrRoom() == null
            || AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMPLETE
            || AbstractDungeon.player == null
            || AbstractDungeon.player.isDead
            || AbstractDungeon.player.isDying
            || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.MAP
            || AbstractDungeon.dungeonMapScreen == null
            || AbstractDungeon.map == null) {
            return false;
        }

        boolean selectionPending = false;
        for (List<MapRoomNode> row : AbstractDungeon.map) {
            if (row == null) {
                continue;
            }
            for (MapRoomNode node : row) {
                if (node != null && PrivateFieldAccess.mapNodeSelectionPending(node)) {
                    selectionPending = true;
                    break;
                }
            }
            if (selectionPending) {
                break;
            }
        }
        return mapUiAcceptingSelection(
            AbstractDungeon.isScreenUp,
            AbstractDungeon.isFadingIn,
            AbstractDungeon.isFadingOut,
            AbstractDungeon.isDungeonBeaten,
            selectionPending
        );
    }

    static boolean mapUiAcceptingSelection(
        boolean screenUp,
        boolean fadingIn,
        boolean fadingOut,
        boolean dungeonBeaten,
        boolean selectionPending
    ) {
        return screenUp
            && !fadingIn
            && !fadingOut
            && !dungeonBeaten
            && !selectionPending;
    }

    static int randomMapIndex(Random random, int availableCount) {
        if (random == null || availableCount <= 0) {
            throw new IllegalArgumentException("A map RNG and at least one node are required.");
        }
        return random.random(availableCount - 1);
    }

    private static void handleEvent(AbstractEvent event) {
        List<LargeDialogOptionButton> options = EventAccess.options();
        if (options.isEmpty()) {
            return;
        }

        int apiIndex = bottomMostSelectableOption(options);
        if (apiIndex < 0) {
            throw new IllegalStateException(
                "Event has no selectable options: " + event.getClass().getName()
            );
        }
        queueEventOption(event.hasDialog, options, apiIndex);
    }

    static boolean queueEventOption(
        boolean roomDialog,
        List<LargeDialogOptionButton> options,
        int apiIndex
    ) {
        if (!(roomDialog ? RoomEventDialog.waitForInput : GenericEventDialog.waitForInput)) {
            return false;
        }
        if (apiIndex < 0 || apiIndex >= options.size()) {
            throw new IllegalArgumentException("Event option index is out of range: " + apiIndex);
        }
        for (LargeDialogOptionButton option : options) {
            if (option != null && option.pressed) {
                return false;
            }
        }

        int vanillaIndex = options.size() - 1 - apiIndex;
        LargeDialogOptionButton option = options.get(vanillaIndex);
        if (!EventAccess.isSelectable(option)) {
            throw new IllegalArgumentException("Event option is disabled: " + apiIndex);
        }

        // Keep the input queued while an image-event animation is running. The
        // vanilla dialog update consumes `pressed` as soon as it accepts input.
        option.hb.clicked = false;
        option.hb.clickStarted = false;
        option.pressed = true;
        return true;
    }

    static int bottomMostSelectableOption(List<LargeDialogOptionButton> options) {
        int apiIndex = 0;
        for (int listIndex = options.size() - 1; listIndex >= 0; --listIndex, ++apiIndex) {
            if (EventAccess.isSelectable(options.get(listIndex))) {
                return apiIndex;
            }
        }
        return -1;
    }

    private static void handleRest(RestRoom room) {
        if (room.campfireUI == null) {
            return;
        }

        try {
            RestApi.selectOption(VanillaRestOptionCatalog.REST);
            return;
        } catch (IllegalStateException notReady) {
            return;
        } catch (IllegalArgumentException restUnavailable) {
            // Fall through to the first usable vanilla option in display order.
        }

        List<AbstractCampfireOption> buttons =
            PrivateFieldAccess.campfireButtons(room.campfireUI);
        if (buttons.isEmpty()) {
            return;
        }
        for (AbstractCampfireOption button : buttons) {
            if (button == null || !button.usable) {
                continue;
            }
            VanillaRestOptionCatalog option = catalogFor(button);
            if (option != null) {
                RestApi.selectOption(option);
                return;
            }
        }
        throw new IllegalStateException("The campfire has no usable vanilla option.");
    }

    static VanillaRestOptionCatalog catalogFor(AbstractCampfireOption button) {
        for (VanillaRestOptionCatalog option : VanillaRestOptionCatalog.values()) {
            if (option.optionType != null && option.optionType == button.getClass()) {
                return option;
            }
        }
        return null;
    }

    private static void handleCombat() {
        List<AbstractCard> hand = CardAccess.hand();
        List<AbstractMonster> livingMonsters = MonsterAccess.livingMonsters();
        for (AbstractCard card : hand) {
            if (requiresTarget(card)) {
                if (livingMonsters.isEmpty()) {
                    return;
                }
                for (AbstractMonster monster : livingMonsters) {
                    CombatApiResult result = CombatApi.playCard(
                        CombatIdentifiers.cardId(card),
                        CombatIdentifiers.targetId(monster)
                    );
                    if (result.isSuccess()) {
                        return;
                    }
                    if (isCombatBusy(result)) {
                        return;
                    }
                    if (!isRecoverableCardFailure(result)) {
                        throw combatFailure("play_card", result);
                    }
                }
            } else {
                CombatApiResult result = CombatApi.playCard(CombatIdentifiers.cardId(card));
                if (result.isSuccess()) {
                    return;
                }
                if (isCombatBusy(result)) {
                    return;
                }
                if (!isRecoverableCardFailure(result)) {
                    throw combatFailure("play_card", result);
                }
            }
        }

        CombatApiResult result = CombatApi.endTurn();
        if (!result.isSuccess() && !isCombatBusy(result)) {
            throw combatFailure("end_turn", result);
        }
    }

    private static boolean requiresTarget(AbstractCard card) {
        return card.target == AbstractCard.CardTarget.ENEMY
            || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY;
    }

    private static boolean isCombatBusy(CombatApiResult result) {
        String code = result.getCode();
        return "game_busy".equals(code)
            || "card_already_queued".equals(code)
            || "not_in_combat".equals(code)
            || "player_dead".equals(code);
    }

    private static boolean isRecoverableCardFailure(CombatApiResult result) {
        String code = result.getCode();
        return "card_not_playable".equals(code)
            || "target_not_found".equals(code)
            || "invalid_target".equals(code);
    }

    private static IllegalStateException combatFailure(
        String command,
        CombatApiResult result
    ) {
        return new IllegalStateException(
            command + " failed: " + result.getCode() + " - " + result.getMessage()
        );
    }

    private static boolean isTerminalScreen() {
        return state == ControllerState.RUNNING
            && (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.DEATH
                || AbstractDungeon.screen == AbstractDungeon.CurrentScreen.VICTORY);
    }

    private static boolean isRunStartReady() {
        return isBatchArmReady()
            && CardCrawlGame.dungeon == null
            && CardCrawlGame.dungeonTransitionScreen == null;
    }

    private static boolean isBatchArmReady() {
        return mainMenuReadyForBatch(
            CardCrawlGame.mode == CardCrawlGame.GameMode.CHAR_SELECT,
            CardCrawlGame.mainMenuScreen != null,
            CardCrawlGame.mainMenuScreen != null
                && CardCrawlGame.mainMenuScreen.isFadingOut,
            CardCrawlGame.mainMenuScreen != null
                && CardCrawlGame.mainMenuScreen.fadedOut,
            RunIdPatches.isRunStartPending()
        );
    }

    static boolean mainMenuReadyForBatch(
        boolean characterSelectMode,
        boolean mainMenuPresent,
        boolean menuFadingOut,
        boolean menuFadedOut,
        boolean runStartPending
    ) {
        return characterSelectMode
            && mainMenuPresent
            && !menuFadingOut
            && !menuFadedOut
            && !runStartPending;
    }

    private static void prepareMainMenuForRunStart() {
        if (!isBatchArmReady()) {
            return;
        }
        if (CardCrawlGame.dungeon != null
            || CardCrawlGame.dungeonTransitionScreen != null) {
            // startOver() resets AbstractDungeon's static state but retains the
            // old CardCrawlGame dungeon and creates a CHAR_SELECT placeholder
            // transition. RunApi correctly rejects both stale objects.
            CardCrawlGame.dungeon = null;
            CardCrawlGame.dungeonTransitionScreen = null;
            LOGGER.info("Cleared stale post-run dungeon state before starting the next seed.");
        }
    }

    private static boolean isSpaceJustPressed() {
        return Gdx.input != null && Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }

    private static void stop(String message, Exception exception) {
        state = ControllerState.IDLE;
        currentIndex = -1;
        mapRandom = null;
        performanceStart = null;
        LOGGER.error(message, exception);
    }

    private enum ControllerState {
        IDLE,
        WAITING_TO_START,
        RUNNING
    }
}
