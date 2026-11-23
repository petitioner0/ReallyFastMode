package reallyfastmode.patches.protocol;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.FastCardObtainEffect;
import com.megacrit.cardcrawl.vfx.GainPennyEffect;
import com.megacrit.cardcrawl.vfx.ObtainKeyEffect;
import com.megacrit.cardcrawl.vfx.ObtainPotionEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ExhaustCardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDrawPileEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;

import java.util.List;

/** Identifies queued or running effects that can change captured state. */
final class StatePacketEffects {
    private StatePacketEffects() {
    }

    static boolean hasPending() {
        return hasPending(AbstractDungeon.effectList) || hasPending(AbstractDungeon.effectsQueue)
            || hasPending(AbstractDungeon.topLevelEffects) || hasPending(AbstractDungeon.topLevelEffectsQueue);
    }

    private static boolean hasPending(List<AbstractGameEffect> effects) {
        for (AbstractGameEffect effect : effects) {
            if (!effect.isDone && (effect instanceof GainPennyEffect || effect instanceof ObtainPotionEffect
                || effect instanceof ObtainKeyEffect || effect instanceof ExhaustCardEffect
                || effect instanceof ShowCardAndAddToDiscardEffect || effect instanceof ShowCardAndAddToDrawPileEffect
                || effect instanceof FastCardObtainEffect || effect instanceof ShowCardAndObtainEffect)) {
                return true;
            }
        }
        return false;
    }
}
