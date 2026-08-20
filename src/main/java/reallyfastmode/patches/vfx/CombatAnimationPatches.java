package reallyfastmode.patches.vfx;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import reallyfastmode.config.FastModeConfig;

public final class CombatAnimationPatches {
    private CombatAnimationPatches() {
    }

    @SpirePatch2(clz = AbstractCreature.class, method = "useFastAttackAnimation")
    @SpirePatch2(clz = AbstractCreature.class, method = "useSlowAttackAnimation")
    @SpirePatch2(clz = AbstractCreature.class, method = "useHopAnimation")
    @SpirePatch2(clz = AbstractCreature.class, method = "useJumpAnimation")
    @SpirePatch2(clz = AbstractCreature.class, method = "useStaggerAnimation")
    @SpirePatch2(clz = AbstractCreature.class, method = "useFastShakeAnimation", paramtypez = {float.class})
    @SpirePatch2(clz = AbstractCreature.class, method = "useShakeAnimation", paramtypez = {float.class})
    public static class CreatureAnimationPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skip() {
            if (FastModeConfig.isFastModeEnabled()) {
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch2(
        clz = ScreenShake.class,
        method = "shake",
        paramtypez = {ScreenShake.ShakeIntensity.class, ScreenShake.ShakeDur.class, boolean.class}
    )
    @SpirePatch2(clz = ScreenShake.class, method = "rumble", paramtypez = {float.class})
    @SpirePatch2(clz = ScreenShake.class, method = "mildRumble", paramtypez = {float.class})
    public static class ScreenShakePatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skip() {
            if (FastModeConfig.isFastModeEnabled()) {
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }
}
