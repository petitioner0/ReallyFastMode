package reallyfastmode.patches.actions;

import com.evacipated.cardcrawl.modthespire.lib.LineFinder;
import com.evacipated.cardcrawl.modthespire.lib.Matcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertLocator;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.actions.common.FastDrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.actions.utility.ShowCardAction;
import com.megacrit.cardcrawl.actions.utility.ShowCardAndPoofAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.SoulGroup;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import javassist.CtBehavior;
import reallyfastmode.config.FastModeConfig;

public final class ResolutionActionPatches {
    private static final int MAX_BATCH_STEPS = 1000;
    private static boolean batchingShuffle;
    private static boolean batchingDraw;

    private ResolutionActionPatches() {
    }

    private static void finishAfterFirstUpdate(AbstractGameAction action) {
        if (FastModeConfig.isFastModeEnabled()) {
            action.isDone = true;
        }
    }

    @SpirePatch2(clz = UseCardAction.class, method = "update")
    public static class UseCardPatch {
        @SpirePostfixPatch
        public static void finish(UseCardAction __instance) {
            finishAfterFirstUpdate(__instance);
        }
    }

    @SpirePatch2(clz = ShowCardAction.class, method = "update")
    public static class ShowCardPatch {
        @SpirePostfixPatch
        public static void finish(ShowCardAction __instance) {
            finishAfterFirstUpdate(__instance);
        }
    }

    @SpirePatch2(clz = ShowCardAndPoofAction.class, method = "update")
    public static class ShowCardAndPoofPatch {
        @SpirePostfixPatch
        public static void finish(ShowCardAndPoofAction __instance) {
            finishAfterFirstUpdate(__instance);
        }
    }

    @SpirePatch2(clz = ApplyPowerAction.class, method = "update")
    public static class ApplyPowerPatch {
        @SpirePostfixPatch
        public static void finish(ApplyPowerAction __instance) {
            finishAfterFirstUpdate(__instance);
        }
    }

    @SpirePatch2(clz = GainBlockAction.class, method = "update")
    public static class GainBlockPatch {
        @SpirePostfixPatch
        public static void finish(GainBlockAction __instance) {
            finishAfterFirstUpdate(__instance);
        }
    }

    @SpirePatch2(clz = HealAction.class, method = "update")
    public static class HealPatch {
        @SpirePostfixPatch
        public static void finish(HealAction __instance) {
            finishAfterFirstUpdate(__instance);
        }
    }

    @SpirePatch2(clz = DrawCardAction.class, method = "update")
    public static class DrawCardPatch {
        @SpirePrefixPatch
        public static void removeInterCardDelay(DrawCardAction __instance) {
            ActionDurationAccess.expireNow(__instance);
        }

        @SpirePostfixPatch
        public static void finishRemainingDraws(DrawCardAction __instance) {
            if (!canBatch(__instance, DrawCardAction.class) || batchingDraw) {
                return;
            }

            batchingDraw = true;
            try {
                drainDrawAction(__instance);
            } finally {
                batchingDraw = false;
            }
        }
    }

    @SpirePatch2(clz = FastDrawCardAction.class, method = "update")
    public static class FastDrawCardPatch {
        @SpirePrefixPatch
        public static void removeInterCardDelay(FastDrawCardAction __instance) {
            ActionDurationAccess.expireNow(__instance);
        }

        @SpirePostfixPatch
        public static void finishRemainingDraws(FastDrawCardAction __instance) {
            if (!canBatch(__instance, FastDrawCardAction.class) || batchingDraw) {
                return;
            }

            batchingDraw = true;
            try {
                drainDrawAction(__instance);
            } finally {
                batchingDraw = false;
            }
        }
    }

    @SpirePatch2(clz = EmptyDeckShuffleAction.class, method = "update")
    public static class EmptyDeckShufflePatch {
        @SpirePostfixPatch
        public static void finishRemainingShuffle(EmptyDeckShuffleAction __instance) {
            if (!canBatch(__instance, EmptyDeckShuffleAction.class) || batchingShuffle) {
                return;
            }

            batchingShuffle = true;
            try {
                for (int step = 0; step < MAX_BATCH_STEPS && !__instance.isDone; ++step) {
                    int previousSize = AbstractDungeon.player.discardPile.size();
                    __instance.update();
                    if (!__instance.isDone
                        && AbstractDungeon.player.discardPile.size() >= previousSize) {
                        break;
                    }
                }
            } finally {
                batchingShuffle = false;
            }
        }
    }

    @SpirePatch2(clz = DamageAction.class, method = "update")
    public static class DamagePatch {
        @SpireInsertPatch(locator = TickDurationLocator.class)
        public static void expire(DamageAction __instance) {
            ActionDurationAccess.expireNow(__instance);
        }
    }

    @SpirePatch2(clz = DamageAllEnemiesAction.class, method = "update")
    public static class DamageAllEnemiesPatch {
        @SpireInsertPatch(locator = TickDurationLocator.class)
        public static void expire(DamageAllEnemiesAction __instance) {
            ActionDurationAccess.expireNow(__instance);
        }
    }

    @SpirePatch2(clz = LoseHPAction.class, method = "update")
    public static class LoseHpPatch {
        @SpireInsertPatch(locator = TickDurationLocator.class)
        public static void expire(LoseHPAction __instance) {
            ActionDurationAccess.expireNow(__instance);
        }
    }

    public static class TickDurationLocator extends SpireInsertLocator {
        @Override
        public int[] Locate(CtBehavior ctMethod) throws Exception {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(
                ctMethod.getDeclaringClass().getName(),
                "tickDuration"
            );
            return LineFinder.findInOrder(ctMethod, finalMatcher);
        }
    }

    private static boolean canBatch(AbstractGameAction action, Class<?> exactClass) {
        return FastModeConfig.isFastModeEnabled()
            && action != null
            && action.getClass() == exactClass
            && !action.isDone;
    }

    private static void drainDrawAction(AbstractGameAction action) {
        for (int step = 0; step < MAX_BATCH_STEPS && !action.isDone; ++step) {
            if (SoulGroup.isActive()) {
                break;
            }

            int previousAmount = action.amount;
            ActionDurationAccess.expireNow(action);
            action.update();

            if (!action.isDone && action.amount >= previousAmount) {
                break;
            }
        }
    }
}
