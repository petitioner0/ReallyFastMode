package reallyfastmode.patches.actions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.actions.common.DiscardAtEndOfTurnAction;
import com.megacrit.cardcrawl.actions.unique.RestoreRetainedCardsAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.EnemyTurnEffect;
import com.megacrit.cardcrawl.vfx.PlayerTurnEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.config.FastModeConfig;

import java.lang.reflect.Field;
import java.util.List;

public final class TurnTransitionPatches {
    private static final Logger LOGGER = LogManager.getLogger(TurnTransitionPatches.class.getName());
    private static final Field DISCARD_END_TURN_FIELD = findDiscardEndTurnField();

    private TurnTransitionPatches() {
    }

    @SpirePatch2(clz = DiscardAtEndOfTurnAction.class, method = "update")
    public static class EndTurnDiscardQueuePatch {
        @SpirePostfixPatch
        public static void collapseDuplicateDiscards() {
            if (!FastModeConfig.isFastModeEnabled() || DISCARD_END_TURN_FIELD == null) {
                return;
            }

            List<AbstractGameAction> actions = AbstractDungeon.actionManager.actions;
            for (int restoreIndex = 1; restoreIndex < actions.size(); ++restoreIndex) {
                AbstractGameAction action = actions.get(restoreIndex);
                if (action == null || action.getClass() != RestoreRetainedCardsAction.class
                    || !isEndTurnDiscard(actions.get(restoreIndex - 1))) {
                    continue;
                }

                int firstDiscardIndex = restoreIndex - 1;
                while (firstDiscardIndex > 0 && isEndTurnDiscard(actions.get(firstDiscardIndex - 1))) {
                    --firstDiscardIndex;
                }

                for (int index = restoreIndex - 1; index > firstDiscardIndex; --index) {
                    actions.remove(index);
                }
                return;
            }
        }
    }

    @SpirePatch2(clz = DiscardAction.class, method = "update")
    public static class EndTurnDiscardDurationPatch {
        @SpirePostfixPatch
        public static void finishAfterDiscard(DiscardAction __instance) {
            if (FastModeConfig.isFastModeEnabled() && isEndTurnDiscard(__instance)) {
                __instance.isDone = true;
            }
        }
    }

    @SpirePatch2(clz = EnemyTurnEffect.class, method = "update")
    public static class EnemyTurnBannerPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skip(EnemyTurnEffect __instance) {
            if (FastModeConfig.isFastModeEnabled()) {
                __instance.isDone = true;
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch2(clz = PlayerTurnEffect.class, method = "update")
    public static class PlayerTurnBannerPatch {
        @SpirePrefixPatch
        public static void finishWithCallbacks(PlayerTurnEffect __instance) {
            if (FastModeConfig.isFastModeEnabled()) {
                // Vanilla runs atEnergyGain() when this effect expires.
                __instance.duration = -1.0F;
            }
        }
    }

    @SpirePatch2(clz = EnemyTurnEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = PlayerTurnEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    public static class TurnBannerRenderPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skip() {
            if (FastModeConfig.isFastModeEnabled()) {
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }

    private static boolean isEndTurnDiscard(AbstractGameAction action) {
        if (action == null || action.getClass() != DiscardAction.class || DISCARD_END_TURN_FIELD == null) {
            return false;
        }

        try {
            return DISCARD_END_TURN_FIELD.getBoolean(action);
        } catch (IllegalAccessException exception) {
            LOGGER.error("Unable to inspect end-turn DiscardAction.", exception);
            return false;
        }
    }

    private static Field findDiscardEndTurnField() {
        try {
            Field field = DiscardAction.class.getDeclaredField("endTurn");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            LOGGER.error("Unable to access DiscardAction.endTurn; end-turn discards will use vanilla timing.", exception);
            return null;
        }
    }
}
