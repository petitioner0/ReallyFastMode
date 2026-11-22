package reallyfastmode.temporary.randomrun;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.map.DungeonMap;
import com.megacrit.cardcrawl.rewards.chests.AbstractChest;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.ui.buttons.ProceedButton;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/** Temporary injected access to vanilla commit points that are otherwise private. */
public final class TemporaryGameAccess {
    private TemporaryGameAccess() {
    }

    static void queueProceed(ProceedButton button) {
        ((ProceedCommit) button).reallyFastMode$queueTemporaryProceed();
    }

    static boolean openChest(AbstractChest chest) {
        return ((ChestCommit) chest).reallyFastMode$openTemporaryChest();
    }

    static void queueCardRewardSkip(CardRewardScreen screen) {
        ((CardRewardCommit) screen).reallyFastMode$queueTemporarySkip();
    }

    static void chooseFirstCardReward(CardRewardScreen screen) {
        ((CardRewardCommit) screen).reallyFastMode$chooseTemporaryFirstCard();
    }

    static boolean enterBoss(DungeonMap map) {
        return ((BossMapCommit) map).reallyFastMode$enterTemporaryBoss();
    }

    public interface ProceedCommit {
        void reallyFastMode$queueTemporaryProceed();
    }

    public interface ChestCommit {
        boolean reallyFastMode$openTemporaryChest();
    }

    public interface CardRewardCommit {
        void reallyFastMode$queueTemporarySkip();

        void reallyFastMode$chooseTemporaryFirstCard();
    }

    public interface BossMapCommit {
        boolean reallyFastMode$enterTemporaryBoss();
    }

    @SpirePatch2(clz = ProceedButton.class, method = "update")
    @SpirePatch2(clz = AbstractChest.class, method = "update")
    @SpirePatch2(clz = CardRewardScreen.class, method = "update")
    @SpirePatch2(clz = DungeonMap.class, method = "update")
    public static class InjectTemporaryAccessPatch {
        @SpireRawPatch
        public static void inject(CtBehavior behavior)
            throws CannotCompileException, NotFoundException {
            CtClass target = behavior.getDeclaringClass();
            String targetName = target.getName();
            if (ProceedButton.class.getName().equals(targetName)) {
                addAccessors(
                    target,
                    ProceedCommit.class,
                    "public void reallyFastMode$queueTemporaryProceed() {"
                        + " this.show(); this.hb.clicked = true; }"
                );
            } else if (AbstractChest.class.getName().equals(targetName)) {
                addAccessors(
                    target,
                    ChestCommit.class,
                    "public boolean reallyFastMode$openTemporaryChest() {"
                        + " if (!this.isOpen && this.keyRequirement()) {"
                        + " this.open(false); return true; } return false; }"
                );
            } else if (CardRewardScreen.class.getName().equals(targetName)) {
                addAccessors(
                    target,
                    CardRewardCommit.class,
                    "public void reallyFastMode$queueTemporarySkip() {"
                        + " this.skipButton.hb.clicked = true; }",
                    "public void reallyFastMode$chooseTemporaryFirstCard() {"
                        + " if (this.rewardGroup == null || this.rewardGroup.isEmpty()) {"
                        + " throw new IllegalStateException(\"Card reward has no candidates.\"); }"
                        + " com.megacrit.cardcrawl.cards.AbstractCard card ="
                        + " (com.megacrit.cardcrawl.cards.AbstractCard)this.rewardGroup.get(0);"
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
                        + " this.draftCount = 0; } }"
                );
            } else if (DungeonMap.class.getName().equals(targetName)) {
                addAccessors(
                    target,
                    BossMapCommit.class,
                    "public boolean reallyFastMode$enterTemporaryBoss() {"
                        + " com.megacrit.cardcrawl.rooms.AbstractRoom room ="
                        + " com.megacrit.cardcrawl.dungeons.AbstractDungeon.getCurrRoom();"
                        + " com.megacrit.cardcrawl.map.MapRoomNode current ="
                        + " com.megacrit.cardcrawl.dungeons.AbstractDungeon.getCurrMapNode();"
                        + " if (room == null || current == null"
                        + " || room.phase != com.megacrit.cardcrawl.rooms.AbstractRoom$RoomPhase.COMPLETE"
                        + " || com.megacrit.cardcrawl.dungeons.AbstractDungeon.screen"
                        + " != com.megacrit.cardcrawl.dungeons.AbstractDungeon$CurrentScreen.MAP)"
                        + " { return false; }"
                        + " boolean available = com.megacrit.cardcrawl.core.Settings.isDebug"
                        + " || current.y == 14"
                        + " || (\"TheEnding\".equals("
                        + " com.megacrit.cardcrawl.dungeons.AbstractDungeon.id)"
                        + " && current.y == 2);"
                        + " if (!available) { return false; }"
                        + " current.taken = true;"
                        + " java.util.Iterator edges = current.getEdges().iterator();"
                        + " while (edges.hasNext()) {"
                        + " com.megacrit.cardcrawl.map.MapEdge edge ="
                        + " (com.megacrit.cardcrawl.map.MapEdge)edges.next();"
                        + " if (edge != null) { edge.markAsTaken(); } }"
                        + " com.megacrit.cardcrawl.helpers.input.InputHelper.justClickedLeft = false;"
                        + " com.megacrit.cardcrawl.core.CardCrawlGame.music.fadeOutTempBGM();"
                        + " com.megacrit.cardcrawl.map.MapRoomNode boss ="
                        + " new com.megacrit.cardcrawl.map.MapRoomNode(-1, 15);"
                        + " boss.room = new com.megacrit.cardcrawl.rooms.MonsterRoomBoss();"
                        + " com.megacrit.cardcrawl.dungeons.AbstractDungeon.nextRoom = boss;"
                        + " if (com.megacrit.cardcrawl.dungeons.AbstractDungeon.pathY.size() > 1) {"
                        + " java.util.ArrayList xs ="
                        + " com.megacrit.cardcrawl.dungeons.AbstractDungeon.pathX;"
                        + " java.util.ArrayList ys ="
                        + " com.megacrit.cardcrawl.dungeons.AbstractDungeon.pathY;"
                        + " xs.add(xs.get(xs.size() - 1));"
                        + " ys.add(ys.get(ys.size() - 1)); }"
                        + " com.megacrit.cardcrawl.dungeons.AbstractDungeon.pathX.add("
                        + " java.lang.Integer.valueOf(-1));"
                        + " com.megacrit.cardcrawl.dungeons.AbstractDungeon.pathY.add("
                        + " java.lang.Integer.valueOf(15));"
                        + " com.megacrit.cardcrawl.dungeons.AbstractDungeon.nextRoomTransitionStart();"
                        + " this.bossHb.hovered = false;"
                        + " return true; }"
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
