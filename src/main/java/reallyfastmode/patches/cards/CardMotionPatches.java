package reallyfastmode.patches.cards;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.Soul;
import com.megacrit.cardcrawl.cards.SoulGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import reallyfastmode.config.FastModeConfig;

public final class CardMotionPatches {
    private CardMotionPatches() {
    }

    @SpirePatch2(clz = AbstractPlayer.class, method = "renderHand", paramtypez = {SpriteBatch.class})
    public static class UsedCardRenderPatch {
        @SpireInstrumentPatch
        public static ExprEditor skipUsedCardRender() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall call) throws CannotCompileException {
                    if (AbstractCard.class.getName().equals(call.getClassName())
                        && "render".equals(call.getMethodName())) {
                        // Queued card effects need cardInUse until vanilla cleanup runs.
                        call.replace("{ if (!" + FastModeConfig.class.getName()
                            + ".isFastModeEnabled() || $0 != this.cardInUse) { $proceed($$); } }");
                    }
                }
            };
        }
    }

    @SpirePatch2(
        clz = SoulGroup.class,
        method = "shuffle",
        paramtypez = {AbstractCard.class, boolean.class}
    )
    public static class ShuffleSoulPatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> moveImmediately(AbstractCard card) {
            if (!FastModeConfig.isFastModeEnabled()) {
                return SpireReturn.Continue();
            }

            card.untip();
            card.unhover();
            AbstractDungeon.player.drawPile.addToTop(card);

            card.current_x = CardGroup.DRAW_PILE_X;
            card.current_y = CardGroup.DRAW_PILE_Y;
            card.target_x = CardGroup.DRAW_PILE_X;
            card.target_y = CardGroup.DRAW_PILE_Y;
            card.drawScale = 0.75F;
            card.targetDrawScale = 0.75F;
            card.setAngle(0.0F);
            card.lighten(false);
            card.clearPowers();

            if (AbstractDungeon.overlayMenu != null
                && AbstractDungeon.overlayMenu.combatDeckPanel != null) {
                AbstractDungeon.overlayMenu.combatDeckPanel.pop();
            }

            AbstractRoom room = AbstractDungeon.getCurrRoom();
            if (room != null && room.phase == AbstractRoom.RoomPhase.COMBAT) {
                AbstractDungeon.player.hand.applyPowers();
            }

            return SpireReturn.Return();
        }
    }

    @SpirePatch2(clz = Soul.class, method = "update")
    public static class SoulMotionPatch {
        @SpirePrefixPatch
        public static void finishMovement(Soul __instance) {
            if (FastModeConfig.isFastModeEnabled()) {
                __instance.isDone = true;
            }
        }
    }
}
