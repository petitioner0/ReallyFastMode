package reallyfastmode.patches.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.config.FastModeConfig;

import java.lang.reflect.Field;

final class ActionDurationAccess {
    private static final Logger LOGGER = LogManager.getLogger(ActionDurationAccess.class.getName());
    private static final Field DURATION_FIELD = findDurationField();

    private ActionDurationAccess() {
    }

    static void expireNow(AbstractGameAction action) {
        if (!FastModeConfig.isFastModeEnabled() || DURATION_FIELD == null) {
            return;
        }

        try {
            DURATION_FIELD.setFloat(action, 0.0F);
        } catch (IllegalAccessException exception) {
            LOGGER.error("Unable to expire action duration for " + action.getClass().getName(), exception);
        }
    }

    private static Field findDurationField() {
        try {
            Field field = AbstractGameAction.class.getDeclaredField("duration");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            LOGGER.error("Unable to access AbstractGameAction.duration; timed actions will use vanilla timing.", exception);
            return null;
        }
    }
}
