package reallyfastmode.config;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Owns the persisted, confirmed state of fast mode.
 *
 * <p>Animation patches must query {@link #isFastModeEnabled()} before changing
 * game behavior. A pending UI selection must not call
 * {@link #confirmFastModeEnabled(boolean)} until the player confirms it.</p>
 */
public final class FastModeConfig {
    private static final Logger LOGGER = LogManager.getLogger(FastModeConfig.class.getName());

    private static final String MOD_ID = "ReallyFastMode";
    private static final String CONFIG_FILE_NAME = "config";
    private static final String FAST_MODE_ENABLED_KEY = "fastModeEnabled";
    private static final boolean DEFAULT_FAST_MODE_ENABLED = false;

    private static SpireConfig config;
    private static volatile boolean fastModeEnabled = DEFAULT_FAST_MODE_ENABLED;

    private FastModeConfig() {
    }

    public static synchronized void initialize() {
        if (config != null) {
            return;
        }

        Properties defaults = new Properties();
        defaults.setProperty(
            FAST_MODE_ENABLED_KEY,
            Boolean.toString(DEFAULT_FAST_MODE_ENABLED)
        );

        try {
            config = new SpireConfig(MOD_ID, CONFIG_FILE_NAME, defaults);
            fastModeEnabled = config.getBool(FAST_MODE_ENABLED_KEY);
            LOGGER.info("Fast mode loaded: {}", fastModeEnabled);
        } catch (IOException exception) {
            config = null;
            fastModeEnabled = DEFAULT_FAST_MODE_ENABLED;
            LOGGER.error("Unable to load fast mode config; fast mode remains disabled.", exception);
        }
    }

    public static boolean isFastModeEnabled() {
        return fastModeEnabled;
    }

    /**
     * Persists a confirmed setting and only then exposes it to animation patches.
     *
     * @return {@code true} when the confirmed value is active, or {@code false}
     *         when it could not be saved and the previous value remains active
     */
    public static synchronized boolean confirmFastModeEnabled(boolean enabled) {
        if (config == null) {
            initialize();
        }
        if (config == null) {
            return false;
        }
        if (fastModeEnabled == enabled) {
            return true;
        }

        boolean previousValue = fastModeEnabled;
        config.setBool(FAST_MODE_ENABLED_KEY, enabled);

        try {
            config.save();
            fastModeEnabled = enabled;
            LOGGER.info("Fast mode confirmed: {}", fastModeEnabled);
            return true;
        } catch (IOException exception) {
            config.setBool(FAST_MODE_ENABLED_KEY, previousValue);
            LOGGER.error("Unable to save fast mode config; keeping the previous value.", exception);
            return false;
        }
    }
}
