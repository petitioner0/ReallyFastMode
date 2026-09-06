package reallyfastmode.patches.transitions;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import reallyfastmode.config.FastModeConfig;
import reallyfastmode.patches.access.RunIdPatches;

public final class SceneTransitionPatches {
    private SceneTransitionPatches() {
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "updateFading")
    public static class DungeonFadePatch {
        @SpirePrefixPatch
        public static void finishFadeImmediately(@ByRef float[] ___fadeTimer) {
            if (!AbstractDungeon.isFadingIn && !AbstractDungeon.isFadingOut) {
                return;
            }
            boolean apiStartupFade = RunIdPatches.consumeStartupFadeSkip();
            if (!FastModeConfig.isFastModeEnabled() && !apiStartupFade) {
                return;
            }
            // Let vanilla updateFading() run its completion branch and room-transition callback.
            ___fadeTimer[0] = -1.0F;
        }

        @SpirePostfixPatch
        public static void removeRoomFadeOverlay() {
            if (FastModeConfig.isFastModeEnabled()
                && !AbstractDungeon.isDungeonBeaten
                && AbstractDungeon.fadeColor != null) {
                AbstractDungeon.fadeColor.a = 0.0F;
            }
        }
    }
}
