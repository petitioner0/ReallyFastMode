package reallyfastmode.patches.transitions;

import com.evacipated.cardcrawl.modthespire.lib.ByRef;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import reallyfastmode.config.FastModeConfig;

/** Removes the visual delays between combat completion and the reward screen. */
public final class CombatEndTransitionPatches {
    private CombatEndTransitionPatches() {
    }

    @SpirePatch2(clz = AbstractMonster.class, method = "updateDeathAnimation")
    public static class MonsterDeathFadePatch {
        @SpirePrefixPatch
        public static void finishImmediately(AbstractMonster __instance) {
            if (FastModeConfig.isFastModeEnabled() && __instance.isDying) {
                // Let vanilla mark the monster dead, dispose it, and end the battle.
                __instance.deathTimer = -1.0F;
            }
        }
    }

    @SpirePatch2(clz = AbstractRoom.class, method = "endBattle")
    public static class RewardScreenDelayPatch {
        @SpirePostfixPatch
        public static void finishImmediately(@ByRef float[] ___endBattleTimer) {
            if (FastModeConfig.isFastModeEnabled()) {
                // AbstractRoom.update() will still generate and open rewards normally.
                ___endBattleTimer[0] = -1.0F;
            }
        }
    }
}
