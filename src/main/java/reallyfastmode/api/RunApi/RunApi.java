package reallyfastmode.api.RunApi;

import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ModHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.patches.access.RunIdPatches;

/** Commands that manage a complete run rather than an individual combat. */
public final class RunApi {
    private static final Logger LOGGER = LogManager.getLogger(RunApi.class.getName());

    private RunApi() {
    }

    /**
     * Starts a standard, non-ascension Ironclad run with the supplied seed and run id.
     * This method must be called on the game render thread.
     */
    public static synchronized RunApiResult startRun(long seed, int runId) {
        return startRunInternal(seed, runId, 0);
    }

    /**
     * Starts an Ironclad run at an ascension level from 1 through 20.
     * This method must be called on the game render thread.
     */
    public static synchronized RunApiResult startRun(long seed, int runId, int ascensionLevel) {
        if (ascensionLevel < 1 || ascensionLevel > 20) {
            return RunApiResult.error(
                "invalid_ascension_level",
                "ascensionLevel must be between 1 and 20."
            );
        }
        return startRunInternal(seed, runId, ascensionLevel);
    }

    private static RunApiResult startRunInternal(long seed, int runId, int ascensionLevel) {
        RunApiResult availability = validateStartAvailable();
        if (!availability.isSuccess()) {
            return availability;
        }

        try {
            CardCrawlGame.mainMenuScreen.fadeOutMusic();

            CardCrawlGame.loadingSave = false;
            CardCrawlGame.saveFile = null;
            CardCrawlGame.trial = null;
            CardCrawlGame.chosenCharacter = AbstractPlayer.PlayerClass.IRONCLAD;

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
                "Ironclad run queued with seed " + seed + ", RunID " + runId
                    + ", and ascension level " + ascensionLevel + "."
            );
        } catch (RuntimeException exception) {
            RunIdPatches.cancelPreparedRun();
            LOGGER.error("start_run failed", exception);
            return RunApiResult.error("internal_error", "start_run failed unexpectedly.");
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
