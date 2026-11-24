package reallyfastmode.api.RunApi;

import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.patches.access.RunIdPatches;
import reallyfastmode.protocol.PacketHeaderProtocol;

/** Commands that manage a complete run rather than an individual combat. */
public final class RunApi {
    private static final Logger LOGGER = LogManager.getLogger(RunApi.class.getName());
    private static final int DEFAULT_CHARACTER_ID = 0;
    private static final AbstractPlayer.PlayerClass[] VANILLA_CHARACTERS = {
        AbstractPlayer.PlayerClass.IRONCLAD,
        AbstractPlayer.PlayerClass.THE_SILENT,
        AbstractPlayer.PlayerClass.DEFECT,
        AbstractPlayer.PlayerClass.WATCHER
    };

    private RunApi() {
    }

    /**
     * Starts a standard, non-ascension Ironclad run using a normal
     * Slay the Spire seed string, for example "18ISL35FYK4".
     *
     * This method must be called on the game render thread.
     */
    public static synchronized RunApiResult startRun(String seed, int runId) {
        return startRun(seed, runId, 0, DEFAULT_CHARACTER_ID);
    }

    /**
     * Starts an Ironclad run at an ascension level from 1 through 20
     * using a normal Slay the Spire seed string.
     *
     * This method must be called on the game render thread.
     */
    public static synchronized RunApiResult startRun(
            String seed,
            int runId,
            int ascensionLevel
    ) {
        if (ascensionLevel < 1 || ascensionLevel > 20) {
            return RunApiResult.error(
                    "invalid_ascension_level",
                    "ascensionLevel must be between 1 and 20."
            );
        }

        return startRun(seed, runId, ascensionLevel, DEFAULT_CHARACTER_ID);
    }

    /**
     * Starts a run as Ironclad (0), Silent (1), Defect (2), or Watcher (3).
     * Ascension level 0 starts a normal run. Call on the game render thread.
     */
    public static synchronized RunApiResult startRun(
            String seed,
            int runId,
            int ascensionLevel,
            int characterId
    ) {
        if (ascensionLevel < 0 || ascensionLevel > 20) {
            return RunApiResult.error(
                    "invalid_ascension_level",
                    "ascensionLevel must be between 0 and 20."
            );
        }
        if (characterId < 0 || characterId >= VANILLA_CHARACTERS.length) {
            return RunApiResult.error(
                    "invalid_character_id",
                    "characterId must be between 0 and 3."
            );
        }
        Long numericSeed = parseSeed(seed);
        if (numericSeed == null) {
            return RunApiResult.error(
                    "invalid_seed",
                    "seed must be a valid Slay the Spire seed."
            );
        }

        return startRunInternal(numericSeed, runId, ascensionLevel, VANILLA_CHARACTERS[characterId]);
    }

    /**
     * Converts a user-facing Slay the Spire seed into the internal long.
     */
    private static Long parseSeed(String seed) {
        if (seed == null) {
            return null;
        }

        String trimmedSeed = seed.trim();
        if (trimmedSeed.isEmpty() || trimmedSeed.length() > SeedHelper.SEED_DEFAULT_LENGTH) {
            return null;
        }

        try {
            String normalized = SeedHelper.sterilizeString(trimmedSeed);
            if (normalized.isEmpty() || normalized.length() > SeedHelper.SEED_DEFAULT_LENGTH) {
                return null;
            }
            return SeedHelper.getLong(normalized);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static RunApiResult startRunInternal(
            long seed,
            int runId,
            int ascensionLevel,
            AbstractPlayer.PlayerClass character
    ) {
        if (!PacketHeaderProtocol.isValidRunId(runId)) {
            return RunApiResult.error(
                    "invalid_run_id",
                    "runId must be between 0 and " + PacketHeaderProtocol.MAX_RUN_ID + "."
            );
        }
        RunApiResult availability = validateStartAvailable();
        if (!availability.isSuccess()) {
            return availability;
        }

        try {
            CardCrawlGame.mainMenuScreen.fadeOutMusic();

            CardCrawlGame.loadingSave = false;
            CardCrawlGame.saveFile = null;
            CardCrawlGame.trial = null;
            CardCrawlGame.chosenCharacter = character;

            Settings.seed = Long.valueOf(seed);
            Settings.seedSet = true;
            Settings.specialSeed = null;
            Settings.isDailyRun = false;
            Settings.isTrial = false;
            Settings.isEndless = false;
            Settings.trialName = null;

            ModHelper.setModsFalse();

            AbstractDungeon.generateSeeds();

            AbstractDungeon.isAscensionMode = ascensionLevel > 0;
            AbstractDungeon.ascensionLevel = ascensionLevel;

            RunIdPatches.prepareRun(runId);

            CardCrawlGame.mainMenuScreen.isFadingOut = true;
            CardCrawlGame.mainMenuScreen.fadedOut = true;

            return RunApiResult.success(
                    "Run queued for "
                            + character
                            + " with seed "
                            + SeedHelper.getString(seed)
                            + ", RunID "
                            + runId
                            + ", and ascension level "
                            + ascensionLevel
                            + "."
            );
        } catch (RuntimeException exception) {
            RunIdPatches.cancelPreparedRun();
            LOGGER.error("start_run failed", exception);

            return RunApiResult.error(
                    "internal_error",
                    "start_run failed unexpectedly."
            );
        }
    }

    private static RunApiResult validateStartAvailable() {
        if (CardCrawlGame.isInARun()
                || CardCrawlGame.mode == CardCrawlGame.GameMode.GAMEPLAY
                || CardCrawlGame.dungeon != null
                || RunIdPatches.isRunStartPending()) {
            return RunApiResult.error(
                    "run_already_active",
                    "A run is already active or starting."
            );
        }

        if (CardCrawlGame.mode != CardCrawlGame.GameMode.CHAR_SELECT
                || CardCrawlGame.mainMenuScreen == null
                || CardCrawlGame.dungeonTransitionScreen != null
                || CardCrawlGame.mainMenuScreen.isFadingOut
                || CardCrawlGame.mainMenuScreen.fadedOut) {
            return RunApiResult.error(
                    "game_not_ready",
                    "The game is not ready to start a run from the main menu."
            );
        }

        return RunApiResult.success("A new run can be started.");
    }
}
