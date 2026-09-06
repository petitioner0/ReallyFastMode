package reallyfastmode.patches.transitions;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.screens.DungeonTransitionScreen;
import reallyfastmode.patches.access.RunIdPatches;

/** Skips presentation-only startup waits for runs created through RunApi. */
public final class RunStartupAnimationPatches {
    private RunStartupAnimationPatches() {
    }

    @SpirePatch2(
        clz = DungeonTransitionScreen.class,
        method = SpirePatch.CONSTRUCTOR,
        paramtypez = {String.class}
    )
    public static class DungeonTransitionPatch {
        @SpirePostfixPatch
        public static void completeImmediately(DungeonTransitionScreen __instance) {
            if (RunIdPatches.shouldSkipStartupAnimations()) {
                __instance.isComplete = true;
            }
        }
    }
}
