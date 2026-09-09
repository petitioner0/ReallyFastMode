package reallyfastmode.patches.access;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import java.util.ArrayList;

/** Direct, patch-injected access to vanilla fields that have no public API. */
public final class PrivateFieldAccess {
    private PrivateFieldAccess() {
    }

    public static int monsterIntentMultiAmount(AbstractMonster monster) {
        return ((MonsterFields) monster).reallyFastMode$intentMultiAmount();
    }

    public static boolean monsterHasMultiDamageIntent(AbstractMonster monster) {
        return ((MonsterFields) monster).reallyFastMode$isMultiDamageIntent();
    }

    public static ArrayList<StoreRelic> shopRelics(ShopScreen screen) {
        return ((ShopFields) screen).reallyFastMode$shopRelics();
    }

    public static ArrayList<StorePotion> shopPotions(ShopScreen screen) {
        return ((ShopFields) screen).reallyFastMode$shopPotions();
    }

    public static ArrayList<AbstractCampfireOption> campfireButtons(CampfireUI campfire) {
        return ((CampfireFields) campfire).reallyFastMode$campfireButtons();
    }

    public static boolean handSelectAnyNumber(HandCardSelectScreen screen) {
        return ((HandSelectFields) screen).reallyFastMode$handSelectAnyNumber();
    }

    public static boolean mapNodeSelectionPending(MapRoomNode node) {
        return ((MapNodeFields) node).reallyFastMode$mapNodeSelectionPending();
    }

    public static void queueMapNodeSelection(MapRoomNode node) {
        ((MapNodeFields) node).reallyFastMode$queueMapNodeSelection();
    }

    public static boolean imageEventDialogReady(GenericEventDialog dialog) {
        return ((ImageEventDialogFields) dialog).reallyFastMode$imageEventDialogReady();
    }

    public static boolean roomEventDialogVisible(RoomEventDialog dialog) {
        return ((RoomEventDialogFields) dialog).reallyFastMode$roomEventDialogVisible();
    }

    public static boolean handSelectForTransform(HandCardSelectScreen screen) {
        return ((HandSelectFields) screen).reallyFastMode$handSelectForTransform();
    }

    public static int gridSelectRequiredCount(GridCardSelectScreen screen) {
        return ((GridSelectFields) screen).reallyFastMode$gridSelectRequiredCount();
    }

    public static void setGridSelectAmount(GridCardSelectScreen screen, int amount) {
        ((GridSelectFields) screen).reallyFastMode$setGridSelectAmount(amount);
    }

    public static boolean cardRewardDiscovery(CardRewardScreen screen) {
        return ((CardRewardFields) screen).reallyFastMode$cardRewardDiscovery();
    }

    public static boolean cardRewardChooseOne(CardRewardScreen screen) {
        return ((CardRewardFields) screen).reallyFastMode$cardRewardChooseOne();
    }

    public static boolean cardRewardSkippable(CardRewardScreen screen) {
        return ((CardRewardFields) screen).reallyFastMode$cardRewardSkippable();
    }

    public static void expireAction(AbstractGameAction action) {
        ((ActionFields) action).reallyFastMode$setDuration(0.0F);
    }

    public static boolean discardEndsTurn(DiscardAction action) {
        return ((DiscardFields) action).reallyFastMode$discardEndsTurn();
    }

    public interface MonsterFields {
        int reallyFastMode$intentMultiAmount();

        boolean reallyFastMode$isMultiDamageIntent();
    }

    public interface ShopFields {
        ArrayList<StoreRelic> reallyFastMode$shopRelics();

        ArrayList<StorePotion> reallyFastMode$shopPotions();
    }

    public interface CampfireFields {
        ArrayList<AbstractCampfireOption> reallyFastMode$campfireButtons();
    }

    public interface HandSelectFields {
        boolean reallyFastMode$handSelectAnyNumber();

        boolean reallyFastMode$handSelectForTransform();
    }

    public interface MapNodeFields {
        boolean reallyFastMode$mapNodeSelectionPending();

        void reallyFastMode$queueMapNodeSelection();
    }

    public interface ImageEventDialogFields {
        boolean reallyFastMode$imageEventDialogReady();
    }

    public interface RoomEventDialogFields {
        boolean reallyFastMode$roomEventDialogVisible();
    }

    public interface GridSelectFields {
        int reallyFastMode$gridSelectRequiredCount();

        void reallyFastMode$setGridSelectAmount(int amount);
    }

    public interface CardRewardFields {
        boolean reallyFastMode$cardRewardDiscovery();

        boolean reallyFastMode$cardRewardChooseOne();

        boolean reallyFastMode$cardRewardSkippable();
    }

    public interface ActionFields {
        void reallyFastMode$setDuration(float duration);
    }

    public interface DiscardFields {
        boolean reallyFastMode$discardEndsTurn();
    }

    @SpirePatch2(clz = AbstractMonster.class, method = "update")
    @SpirePatch2(clz = ShopScreen.class, method = "update")
    @SpirePatch2(clz = CampfireUI.class, method = "update")
    @SpirePatch2(clz = MapRoomNode.class, method = "update")
    @SpirePatch2(clz = GenericEventDialog.class, method = "update")
    @SpirePatch2(clz = RoomEventDialog.class, method = "update")
    @SpirePatch2(clz = HandCardSelectScreen.class, method = "update")
    @SpirePatch2(clz = GridCardSelectScreen.class, method = "update")
    @SpirePatch2(clz = CardRewardScreen.class, method = "update")
    @SpirePatch2(clz = AbstractGameAction.class, method = "tickDuration")
    @SpirePatch2(clz = DiscardAction.class, method = "update")
    public static class InjectAccessorsPatch {
        @SpireRawPatch
        public static void inject(CtBehavior behavior) throws CannotCompileException, NotFoundException {
            CtClass target = behavior.getDeclaringClass();
            String targetName = target.getName();
            if ("com.megacrit.cardcrawl.monsters.AbstractMonster".equals(targetName)) {
                addAccessors(target, MonsterFields.class,
                    "public int reallyFastMode$intentMultiAmount() { return this.intentMultiAmt; }",
                    "public boolean reallyFastMode$isMultiDamageIntent() { return this.isMultiDmg; }"
                );
            } else if ("com.megacrit.cardcrawl.shop.ShopScreen".equals(targetName)) {
                addAccessors(target, ShopFields.class,
                    "public java.util.ArrayList reallyFastMode$shopRelics() { return this.relics; }",
                    "public java.util.ArrayList reallyFastMode$shopPotions() { return this.potions; }"
                );
            } else if ("com.megacrit.cardcrawl.rooms.CampfireUI".equals(targetName)) {
                addAccessors(target, CampfireFields.class,
                    "public java.util.ArrayList reallyFastMode$campfireButtons() { return this.buttons; }"
                );
            } else if ("com.megacrit.cardcrawl.map.MapRoomNode".equals(targetName)) {
                addAccessors(target, MapNodeFields.class,
                    "public boolean reallyFastMode$mapNodeSelectionPending() { return this.animWaitTimer != 0.0F; }",
                    // A negative, nonzero timer enters vanilla's transition branch even at zero delta time.
                    "public void reallyFastMode$queueMapNodeSelection() { this.animWaitTimer = -1.0F; }"
                );
            } else if ("com.megacrit.cardcrawl.events.GenericEventDialog".equals(targetName)) {
                addAccessors(target, ImageEventDialogFields.class,
                    "public boolean reallyFastMode$imageEventDialogReady() { return show && this.animateTimer == 0.0F; }"
                );
            } else if ("com.megacrit.cardcrawl.events.RoomEventDialog".equals(targetName)) {
                addAccessors(target, RoomEventDialogFields.class,
                    "public boolean reallyFastMode$roomEventDialogVisible() { return this.show; }"
                );
            } else if ("com.megacrit.cardcrawl.screens.select.HandCardSelectScreen".equals(targetName)) {
                addAccessors(target, HandSelectFields.class,
                    "public boolean reallyFastMode$handSelectAnyNumber() { return this.anyNumber; }",
                    "public boolean reallyFastMode$handSelectForTransform() { return this.forTransform; }"
                );
            } else if ("com.megacrit.cardcrawl.screens.select.GridCardSelectScreen".equals(targetName)) {
                addAccessors(target, GridSelectFields.class,
                    "public int reallyFastMode$gridSelectRequiredCount() { return this.numCards; }",
                    "public void reallyFastMode$setGridSelectAmount(int amount) { this.cardSelectAmount = amount; }"
                );
            } else if ("com.megacrit.cardcrawl.screens.CardRewardScreen".equals(targetName)) {
                addAccessors(target, CardRewardFields.class,
                    "public boolean reallyFastMode$cardRewardDiscovery() { return this.discovery; }",
                    "public boolean reallyFastMode$cardRewardChooseOne() { return this.chooseOne; }",
                    "public boolean reallyFastMode$cardRewardSkippable() { return this.skippable; }"
                );
            } else if ("com.megacrit.cardcrawl.actions.AbstractGameAction".equals(targetName)) {
                addAccessors(target, ActionFields.class,
                    "public void reallyFastMode$setDuration(float duration) { this.duration = duration; }"
                );
            } else if ("com.megacrit.cardcrawl.actions.common.DiscardAction".equals(targetName)) {
                addAccessors(target, DiscardFields.class,
                    "public boolean reallyFastMode$discardEndsTurn() { return this.endTurn; }"
                );
            }
        }

        private static void addAccessors(CtClass target, Class<?> accessor, String... methods)
            throws CannotCompileException, NotFoundException {
            target.addInterface(target.getClassPool().get(accessor.getName()));
            for (String method : methods) {
                target.addMethod(CtNewMethod.make(method, target));
            }
        }
    }
}
