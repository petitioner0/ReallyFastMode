package reallyfastmode.patches.vfx;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.vfx.combat.BattleStartEffect;
import reallyfastmode.config.FastModeConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class AdvancedCombatEffectPatches {
    private static final String COMBAT_EFFECT_PACKAGE = "com.megacrit.cardcrawl.vfx.combat.";
    private static final String STANCE_EFFECT_PACKAGE = "com.megacrit.cardcrawl.vfx.stance.";
    private static final Set<String> SECONDARY_COMBAT_EFFECTS = new HashSet<String>(Arrays.asList(
        "com.megacrit.cardcrawl.vfx.BorderFlashEffect",
        "com.megacrit.cardcrawl.vfx.BorderLongFlashEffect",
        "com.megacrit.cardcrawl.vfx.EnemyTurnEffect",
        "com.megacrit.cardcrawl.vfx.DarkSmokePuffEffect",
        "com.megacrit.cardcrawl.vfx.ExhaustEmberEffect",
        "com.megacrit.cardcrawl.vfx.FastSmokeParticle",
        "com.megacrit.cardcrawl.vfx.FireBurstParticleEffect",
        "com.megacrit.cardcrawl.vfx.GenericSmokeEffect",
        "com.megacrit.cardcrawl.vfx.GhostlyWeakFireEffect",
        "com.megacrit.cardcrawl.vfx.PetalEffect",
        "com.megacrit.cardcrawl.vfx.ShineSparkleEffect",
        "com.megacrit.cardcrawl.vfx.SpotlightEffect",
        "com.megacrit.cardcrawl.vfx.SumDamageEffect",
        "com.megacrit.cardcrawl.vfx.TextAboveCreatureEffect",
        "com.megacrit.cardcrawl.vfx.UpgradeShineParticleEffect",
        "com.megacrit.cardcrawl.vfx.cardManip.CardDisappearEffect",
        "com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToHandEffect",
        "com.megacrit.cardcrawl.vfx.scene.TorchParticleXLEffect"
    ));

    private AdvancedCombatEffectPatches() {
    }

    public static void installFilteringLists() {
        AbstractDungeon.effectList = wrap(AbstractDungeon.effectList);
        AbstractDungeon.effectsQueue = wrap(AbstractDungeon.effectsQueue);
        AbstractDungeon.topLevelEffects = wrap(AbstractDungeon.topLevelEffects);
        AbstractDungeon.topLevelEffectsQueue = wrap(AbstractDungeon.topLevelEffectsQueue);
    }

    @SpirePatch2(clz = BattleStartEffect.class, method = "update")
    public static class FinishBattleStartEffectPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> finishWithCallbacks(BattleStartEffect __instance) {
            if (!FastModeConfig.isFastModeEnabled()) {
                return SpireReturn.Continue();
            }

            if (AbstractDungeon.player != null) {
                AbstractDungeon.player.showHealthBar();
            }

            MonsterGroup monsters = AbstractDungeon.getMonsters();
            if (monsters != null) {
                for (AbstractMonster monster : monsters.monsters) {
                    monster.showHealthBar();
                }
                monsters.showIntent();
            }

            __instance.isDone = true;
            return SpireReturn.Return();
        }
    }

    private static ArrayList<AbstractGameEffect> wrap(ArrayList<AbstractGameEffect> effects) {
        if (effects instanceof FilteringEffectList) {
            return effects;
        }
        return new FilteringEffectList(effects);
    }

    private static boolean shouldReject(AbstractGameEffect effect) {
        if (!FastModeConfig.isFastModeEnabled()
            || AbstractDungeon.currMapNode == null
            || effect == null) {
            return false;
        }

        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (room == null
            || room.phase != AbstractRoom.RoomPhase.COMBAT
            || !isAdvancedVisualEffect(effect)) {
            return false;
        }

        effect.isDone = true;
        return true;
    }

    private static boolean isAdvancedVisualEffect(AbstractGameEffect effect) {
        if (effect == null || effect.getClass() == BattleStartEffect.class) {
            return false;
        }

        String className = effect.getClass().getName();
        return className.startsWith(COMBAT_EFFECT_PACKAGE)
            || className.startsWith(STANCE_EFFECT_PACKAGE)
            || SECONDARY_COMBAT_EFFECTS.contains(className);
    }

    private static final class FilteringEffectList extends ArrayList<AbstractGameEffect> {
        private FilteringEffectList(Collection<? extends AbstractGameEffect> effects) {
            if (effects != null) {
                addAll(effects);
            }
        }

        @Override
        public boolean add(AbstractGameEffect effect) {
            return !shouldReject(effect) && super.add(effect);
        }

        @Override
        public void add(int index, AbstractGameEffect effect) {
            if (!shouldReject(effect)) {
                super.add(index, effect);
            }
        }

        @Override
        public boolean addAll(Collection<? extends AbstractGameEffect> effects) {
            boolean modified = false;
            for (AbstractGameEffect effect : new ArrayList<AbstractGameEffect>(effects)) {
                modified |= add(effect);
            }
            return modified;
        }

        @Override
        public boolean addAll(int index, Collection<? extends AbstractGameEffect> effects) {
            if (index < 0 || index > size()) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
            }

            boolean modified = false;
            for (AbstractGameEffect effect : new ArrayList<AbstractGameEffect>(effects)) {
                if (!shouldReject(effect)) {
                    super.add(index++, effect);
                    modified = true;
                }
            }
            return modified;
        }
    }
}
