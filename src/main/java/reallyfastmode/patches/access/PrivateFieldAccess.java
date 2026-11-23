package reallyfastmode.patches.access;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import com.megacrit.cardcrawl.ui.buttons.ProceedButton;
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

    public static void purchaseShopCard(ShopScreen screen, AbstractCard card) {
        ((ShopFields) screen).reallyFastMode$purchaseShopCard(card);
    }

    public static void openShopCardRemoval(ShopScreen screen) {
        ((ShopFields) screen).reallyFastMode$openShopCardRemoval();
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

    public static boolean proceedButtonAcceptsClick(ProceedButton button) {
        return ((ProceedButtonFields) button).reallyFastMode$proceedButtonAcceptsClick();
    }

    public static boolean chestKeyRequirement(AbstractChest chest) {
        return ((ChestFields) chest).reallyFastMode$chestKeyRequirement();
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

    public static AbstractCard gridPreviewedCard(GridCardSelectScreen screen) {
        return ((GridSelectFields) screen).reallyFastMode$gridPreviewedCard();
    }

    public static void setGridSelectAmount(GridCardSelectScreen screen, int amount) {
        ((GridSelectFields) screen).reallyFastMode$setGridSelectAmount(amount);
    }

    public static void chooseCardReward(CardRewardScreen screen, AbstractCard card) {
        ((CardRewardFields) screen).reallyFastMode$chooseCardReward(card);
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

        void reallyFastMode$purchaseShopCard(AbstractCard card);

        void reallyFastMode$openShopCardRemoval();
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

    public interface ProceedButtonFields {
        boolean reallyFastMode$proceedButtonAcceptsClick();
    }

    public interface ChestFields {
        boolean reallyFastMode$chestKeyRequirement();
    }

    public interface ImageEventDialogFields {
        boolean reallyFastMode$imageEventDialogReady();
    }

    public interface RoomEventDialogFields {
        boolean reallyFastMode$roomEventDialogVisible();
    }

    public interface GridSelectFields {
        int reallyFastMode$gridSelectRequiredCount();

        AbstractCard reallyFastMode$gridPreviewedCard();

        void reallyFastMode$setGridSelectAmount(int amount);
    }

    public interface CardRewardFields {
        void reallyFastMode$chooseCardReward(AbstractCard card);

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
    @SpirePatch2(clz = ProceedButton.class, method = "update")
    @SpirePatch2(clz = AbstractChest.class, method = "update")
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
                    "public java.util.ArrayList reallyFastMode$shopPotions() { return this.potions; }",
                    "public void reallyFastMode$purchaseShopCard(com.megacrit.cardcrawl.cards.AbstractCard card) {"
                        + " this.resetTouchscreenVars(); this.purchaseCard(card); }",
                    "public void reallyFastMode$openShopCardRemoval() {"
                        + " this.resetTouchscreenVars(); this.purchasePurge(); }"
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
            } else if ("com.megacrit.cardcrawl.ui.buttons.ProceedButton".equals(targetName)) {
                addAccessors(target, ProceedButtonFields.class,
                    "public boolean reallyFastMode$proceedButtonAcceptsClick() { return !this.isHidden && !this.hb.clicked; }"
                );
            } else if ("com.megacrit.cardcrawl.rewards.chests.AbstractChest".equals(targetName)) {
                addAccessors(target, ChestFields.class,
                    "public boolean reallyFastMode$chestKeyRequirement() { return this.keyRequirement(); }"
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
                    "public com.megacrit.cardcrawl.cards.AbstractCard reallyFastMode$gridPreviewedCard() { return this.hoveredCard; }",
                    "public void reallyFastMode$setGridSelectAmount(int amount) { this.cardSelectAmount = amount; }"
                );
            } else if ("com.megacrit.cardcrawl.screens.CardRewardScreen".equals(targetName)) {
                addAccessors(target, CardRewardFields.class,
                    // Match the desktop card-selection branch, including draft continuation.
                    "public void reallyFastMode$chooseCardReward(com.megacrit.cardcrawl.cards.AbstractCard card) {"
                        + " this.touchCard = null; this.confirmButton.hb.clicked = false;"
                        + " this.confirmButton.hb.clickStarted = false; this.confirmButton.hide();"
                        + " this.skipButton.hide(); this.bowlButton.hide();"
                        + " if (this.codex) { this.codexCard = card; }"
                        + " else if (this.discovery) { this.discoveryCard = card; }"
                        + " else if (this.chooseOne) { card.onChoseThisOption();"
                        + " com.megacrit.cardcrawl.dungeons.AbstractDungeon.effectList.add("
                        + " new com.megacrit.cardcrawl.vfx.cardManip.ExhaustCardEffect(card)); }"
                        + " else { this.acquireCard(card); }"
                        + " this.takeReward();"
                        + " if (this.draft && this.draftCount < 15) { this.draftOpen(); }"
                        + " else { com.megacrit.cardcrawl.dungeons.AbstractDungeon.closeCurrentScreen();"
                        + " this.draftCount = 0; } }",
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
