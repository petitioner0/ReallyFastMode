package reallyfastmode.patches.actions;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.IntentFlashAction;
import com.megacrit.cardcrawl.actions.animations.AnimateFastAttackAction;
import com.megacrit.cardcrawl.actions.animations.AnimateHopAction;
import com.megacrit.cardcrawl.actions.animations.AnimateJumpAction;
import com.megacrit.cardcrawl.actions.animations.AnimateShakeAction;
import com.megacrit.cardcrawl.actions.animations.AnimateSlowAttackAction;
import com.megacrit.cardcrawl.actions.animations.FastShakeAction;
import com.megacrit.cardcrawl.actions.animations.SetAnimationAction;
import com.megacrit.cardcrawl.actions.animations.VFXAction;
import com.megacrit.cardcrawl.actions.common.ShowMoveNameAction;
import com.megacrit.cardcrawl.actions.utility.ShakeScreenAction;
import com.megacrit.cardcrawl.actions.utility.TextAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import reallyfastmode.config.FastModeConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class VisualOnlyActionPatches {
    private static final Set<Class<?>> SKIPPABLE_ACTIONS = new HashSet<Class<?>>(Arrays.<Class<?>>asList(
        WaitAction.class,
        VFXAction.class,
        ShakeScreenAction.class,
        TextAboveCreatureAction.class,
        IntentFlashAction.class,
        ShowMoveNameAction.class,
        AnimateFastAttackAction.class,
        AnimateSlowAttackAction.class,
        AnimateHopAction.class,
        AnimateJumpAction.class,
        AnimateShakeAction.class,
        FastShakeAction.class,
        SetAnimationAction.class
    ));

    private VisualOnlyActionPatches() {
    }

    private static boolean shouldSkip(AbstractGameAction action) {
        return FastModeConfig.isFastModeEnabled()
            && action != null
            && SKIPPABLE_ACTIONS.contains(action.getClass());
    }

    private static SpireReturn<Void> skipEnqueue(AbstractGameAction action) {
        if (shouldSkip(action)) {
            action.isDone = true;
            return SpireReturn.Return();
        }
        return SpireReturn.Continue();
    }

    @SpirePatch2(clz = GameActionManager.class, method = "addToBottom")
    public static class AddToBottomPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skip(AbstractGameAction action) {
            return skipEnqueue(action);
        }
    }

    @SpirePatch2(clz = GameActionManager.class, method = "addToTop")
    public static class AddToTopPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skip(AbstractGameAction action) {
            return skipEnqueue(action);
        }
    }

    @SpirePatch2(clz = GameActionManager.class, method = "addToTurnStart")
    public static class AddToTurnStartPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skip(AbstractGameAction action) {
            return skipEnqueue(action);
        }
    }

    @SpirePatch2(clz = GameActionManager.class, method = "addToNextCombat")
    public static class AddToNextCombatPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skip(AbstractGameAction action) {
            return skipEnqueue(action);
        }
    }

    @SpirePatch2(clz = GameActionManager.class, method = "update")
    public static class AlreadyQueuedActionPatch {
        @SpirePrefixPatch
        public static void skip(GameActionManager __instance) {
            if (shouldSkip(__instance.currentAction)) {
                __instance.currentAction.isDone = true;
            }
        }
    }
}
