package reallyfastmode.patches.vfx;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.EnemyTurnEffect;
import com.megacrit.cardcrawl.vfx.SumDamageEffect;
import com.megacrit.cardcrawl.vfx.TextAboveCreatureEffect;
import com.megacrit.cardcrawl.vfx.cardManip.CardDisappearEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ExhaustCardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDrawPileEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToHandEffect;
import com.megacrit.cardcrawl.vfx.combat.BlockImpactLineEffect;
import com.megacrit.cardcrawl.vfx.combat.BlockedNumberEffect;
import com.megacrit.cardcrawl.vfx.combat.BlockedWordEffect;
import com.megacrit.cardcrawl.vfx.combat.DamageImpactBlurEffect;
import com.megacrit.cardcrawl.vfx.combat.DamageImpactCurvyEffect;
import com.megacrit.cardcrawl.vfx.combat.DamageImpactLineEffect;
import com.megacrit.cardcrawl.vfx.combat.DamageNumberEffect;
import com.megacrit.cardcrawl.vfx.combat.EmpowerEffect;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;
import com.megacrit.cardcrawl.vfx.combat.FlashIntentEffect;
import com.megacrit.cardcrawl.vfx.combat.HbBlockBrokenEffect;
import com.megacrit.cardcrawl.vfx.combat.HealEffect;
import com.megacrit.cardcrawl.vfx.combat.HealNumberEffect;
import com.megacrit.cardcrawl.vfx.combat.MoveNameEffect;
import com.megacrit.cardcrawl.vfx.combat.PotionBounceEffect;
import com.megacrit.cardcrawl.vfx.combat.PowerBuffEffect;
import com.megacrit.cardcrawl.vfx.combat.PowerDebuffEffect;
import com.megacrit.cardcrawl.vfx.combat.StrikeEffect;
import reallyfastmode.config.FastModeConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class VisualEffectPatches {
    private static final Set<Class<?>> SKIPPABLE_EFFECTS = new HashSet<Class<?>>(Arrays.<Class<?>>asList(
        CardDisappearEffect.class,
        ExhaustCardEffect.class,
        ShowCardAndAddToHandEffect.class,
        BlockImpactLineEffect.class,
        BlockedNumberEffect.class,
        BlockedWordEffect.class,
        DamageImpactBlurEffect.class,
        DamageImpactCurvyEffect.class,
        DamageImpactLineEffect.class,
        DamageNumberEffect.class,
        EnemyTurnEffect.class,
        EmpowerEffect.class,
        FlashAtkImgEffect.class,
        FlashIntentEffect.class,
        HbBlockBrokenEffect.class,
        HealEffect.class,
        HealNumberEffect.class,
        MoveNameEffect.class,
        PotionBounceEffect.class,
        PowerBuffEffect.class,
        PowerDebuffEffect.class,
        StrikeEffect.class,
        SumDamageEffect.class,
        TextAboveCreatureEffect.class
    ));

    private VisualEffectPatches() {
    }

    @SpirePatch2(clz = CardDisappearEffect.class, method = "update")
    @SpirePatch2(clz = ShowCardAndAddToHandEffect.class, method = "update")
    @SpirePatch2(clz = BlockImpactLineEffect.class, method = "update")
    @SpirePatch2(clz = BlockedNumberEffect.class, method = "update")
    @SpirePatch2(clz = BlockedWordEffect.class, method = "update")
    @SpirePatch2(clz = DamageImpactBlurEffect.class, method = "update")
    @SpirePatch2(clz = DamageImpactCurvyEffect.class, method = "update")
    @SpirePatch2(clz = DamageImpactLineEffect.class, method = "update")
    @SpirePatch2(clz = DamageNumberEffect.class, method = "update")
    @SpirePatch2(clz = EmpowerEffect.class, method = "update")
    @SpirePatch2(clz = FlashAtkImgEffect.class, method = "update")
    @SpirePatch2(clz = FlashIntentEffect.class, method = "update")
    @SpirePatch2(clz = HbBlockBrokenEffect.class, method = "update")
    @SpirePatch2(clz = HealEffect.class, method = "update")
    @SpirePatch2(clz = HealNumberEffect.class, method = "update")
    @SpirePatch2(clz = MoveNameEffect.class, method = "update")
    @SpirePatch2(clz = PotionBounceEffect.class, method = "update")
    @SpirePatch2(clz = PowerBuffEffect.class, method = "update")
    @SpirePatch2(clz = PowerDebuffEffect.class, method = "update")
    @SpirePatch2(clz = StrikeEffect.class, method = "update")
    @SpirePatch2(clz = SumDamageEffect.class, method = "update")
    @SpirePatch2(clz = TextAboveCreatureEffect.class, method = "update")
    public static class SkipPureVisualEffectPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skip(AbstractGameEffect __instance) {
            if (FastModeConfig.isFastModeEnabled()) {
                __instance.isDone = true;
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch2(clz = CardDisappearEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = ExhaustCardEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = ShowCardAndAddToDiscardEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = ShowCardAndAddToDrawPileEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = ShowCardAndAddToHandEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = BlockImpactLineEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = BlockedNumberEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = BlockedWordEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = DamageImpactBlurEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = DamageImpactCurvyEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = DamageImpactLineEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = DamageNumberEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = EmpowerEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = FlashAtkImgEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = FlashIntentEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = HbBlockBrokenEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = HealEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = HealNumberEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = MoveNameEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = PotionBounceEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = PowerBuffEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = PowerDebuffEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = StrikeEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = SumDamageEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    @SpirePatch2(clz = TextAboveCreatureEffect.class, method = "render", paramtypez = {SpriteBatch.class})
    public static class SkipPureVisualEffectRenderPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> skip() {
            if (FastModeConfig.isFastModeEnabled()) {
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch2(clz = ExhaustCardEffect.class, method = "update")
    public static class FinishExhaustEffectPatch {
        @SpirePrefixPatch
        public static void finishWithoutParticles(ExhaustCardEffect __instance) {
            if (FastModeConfig.isFastModeEnabled()) {
                // The vanilla update performs card cleanup when duration expires.
                // Expiring it here keeps that cleanup while bypassing 140 particles.
                __instance.duration = -1.0F;
            }
        }
    }

    @SpirePatch2(clz = ShowCardAndAddToDiscardEffect.class, method = "update")
    @SpirePatch2(clz = ShowCardAndAddToDrawPileEffect.class, method = "update")
    public static class FinishGeneratedCardMovementPatch {
        @SpirePrefixPatch
        public static void finishMovement(AbstractGameEffect __instance) {
            if (FastModeConfig.isFastModeEnabled()) {
                // Vanilla moves the card into the pile when this effect expires.
                // Force that branch to run now instead of skipping update entirely.
                __instance.duration = -1.0F;
            }
        }
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "update")
    public static class RemoveBeforeRenderPatch {
        @SpirePostfixPatch
        public static void removeFinishedVisuals() {
            if (!FastModeConfig.isFastModeEnabled()) {
                return;
            }

            removeSkippableEffects(AbstractDungeon.effectList);
            removeSkippableEffects(AbstractDungeon.effectsQueue);
            removeSkippableEffects(AbstractDungeon.topLevelEffects);
            removeSkippableEffects(AbstractDungeon.topLevelEffectsQueue);
        }
    }

    private static void removeSkippableEffects(List<AbstractGameEffect> effects) {
        if (effects == null || effects.isEmpty()) {
            return;
        }

        Iterator<AbstractGameEffect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            AbstractGameEffect effect = iterator.next();
            if (SKIPPABLE_EFFECTS.contains(effect.getClass())) {
                effect.isDone = true;
                iterator.remove();
            }
        }
    }
}
