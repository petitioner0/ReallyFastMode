package reallyfastmode.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Result of submitting a game command on the render thread. */
public final class GameCommandResult {
    private static final Logger LOGGER = LogManager.getLogger(GameCommandResult.class.getName());
    private static final GameCommandResult OK = new GameCommandResult(true, "ok", "");

    private final boolean success;
    private final String code;
    private final String message;

    private GameCommandResult(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public static GameCommandResult execute(String command, Runnable operation) {
        try {
            operation.run();
            return OK;
        } catch (IllegalArgumentException exception) {
            return new GameCommandResult(false, "invalid_argument", exception.getMessage());
        } catch (IllegalStateException exception) {
            return new GameCommandResult(false, "invalid_state", exception.getMessage());
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to execute {}.", command, exception);
            return new GameCommandResult(false, "internal_error", command + " failed unexpectedly.");
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
