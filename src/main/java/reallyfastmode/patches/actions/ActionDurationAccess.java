package reallyfastmode.patches.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import reallyfastmode.config.FastModeConfig;
import reallyfastmode.patches.access.PrivateFieldAccess;

final class ActionDurationAccess {
    private ActionDurationAccess() {
    }

    static void expireNow(AbstractGameAction action) {
        if (!FastModeConfig.isFastModeEnabled()) {
            return;
        }
        PrivateFieldAccess.expireAction(action);
    }
}
