package reallyfastmode.patches.access;

import basemod.BaseMod;
import basemod.abstracts.CustomSavable;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import reallyfastmode.protocol.PacketHeaderProtocol;

import java.util.ArrayList;

/** Adds a caller-defined identifier to every vanilla dungeon instance in one run. */
public final class RunIdPatches {
    private static final int UNASSIGNED_RUN_ID = 0;
    private static final String RUN_ID_SAVE_KEY = "ReallyFastMode:run_id";

    private static int activeRunId = UNASSIGNED_RUN_ID;
    private static boolean runStartPending;
    private static boolean startupAnimationsPending;

    private RunIdPatches() {
    }

    public static void registerSaveField() {
        BaseMod.addSaveField(RUN_ID_SAVE_KEY, new RunIdSaveField());
    }

    /** Accessor interface implemented by the patched {@link AbstractDungeon}. */
    public interface RunIdentifier {
        int getRunID();

        void reallyFastMode$setRunID(int runId);
    }

    public static int runId(AbstractDungeon dungeon) {
        if (dungeon == null) {
            throw new IllegalStateException("No current run is available.");
        }
        return ((RunIdentifier) dungeon).getRunID();
    }

    public static synchronized void prepareRun(int runId) {
        activeRunId = runId;
        runStartPending = true;
        startupAnimationsPending = true;
    }

    public static synchronized boolean isRunStartPending() {
        return runStartPending;
    }

    public static synchronized boolean shouldSkipStartupAnimations() {
        return startupAnimationsPending;
    }

    public static synchronized boolean consumeStartupFadeSkip() {
        if (!startupAnimationsPending) {
            return false;
        }
        startupAnimationsPending = false;
        return true;
    }

    public static synchronized void cancelPreparedRun() {
        activeRunId = UNASSIGNED_RUN_ID;
        runStartPending = false;
        startupAnimationsPending = false;
    }

    private static synchronized void assignRunId(AbstractDungeon dungeon) {
        ((RunIdentifier) dungeon).reallyFastMode$setRunID(activeRunId);
        runStartPending = false;
    }

    private static final class RunIdSaveField implements CustomSavable<Integer> {
        @Override
        public Integer onSave() {
            synchronized (RunIdPatches.class) {
                return activeRunId;
            }
        }

        @Override
        public void onLoad(Integer runId) {
            // BaseMod restores the player's save before the dungeon is constructed.
            synchronized (RunIdPatches.class) {
                activeRunId = runId != null && PacketHeaderProtocol.isValidRunId(runId)
                    ? runId : UNASSIGNED_RUN_ID;
            }
        }
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "update")
    public static class InjectRunIdPatch {
        @SpireRawPatch
        public static void inject(CtBehavior behavior)
            throws CannotCompileException, NotFoundException {
            CtClass target = behavior.getDeclaringClass();
            target.addField(CtField.make("public int RunID;", target));
            target.addInterface(target.getClassPool().get(RunIdentifier.class.getName()));
            target.addMethod(CtNewMethod.make(
                "public int getRunID() { return this.RunID; }",
                target
            ));
            target.addMethod(CtNewMethod.make(
                "public void reallyFastMode$setRunID(int runId) { this.RunID = runId; }",
                target
            ));
        }
    }

    @SpirePatch2(
        clz = AbstractDungeon.class,
        method = SpirePatch.CONSTRUCTOR,
        paramtypez = {String.class, String.class, AbstractPlayer.class, ArrayList.class}
    )
    public static class NewRunConstructorPatch {
        @SpirePostfixPatch
        public static void assign(AbstractDungeon __instance) {
            assignRunId(__instance);
        }
    }

    @SpirePatch2(
        clz = AbstractDungeon.class,
        method = SpirePatch.CONSTRUCTOR,
        paramtypez = {String.class, AbstractPlayer.class, SaveFile.class}
    )
    public static class LoadedRunConstructorPatch {
        @SpirePostfixPatch
        public static void assign(AbstractDungeon __instance) {
            assignRunId(__instance);
        }
    }

    @SpirePatch2(clz = CardCrawlGame.class, method = "startOver")
    @SpirePatch2(clz = CardCrawlGame.class, method = "startOverButShowCredits")
    public static class ClearRunIdPatch {
        @SpirePostfixPatch
        public static void clear() {
            cancelPreparedRun();
        }
    }
}
