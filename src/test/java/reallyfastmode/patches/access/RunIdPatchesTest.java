package reallyfastmode.patches.access;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RunIdPatchesTest {
    @Test
    public void injectsRunIdFieldAndAccessorsIntoAbstractDungeon() throws Exception {
        ClassPool pool = new ClassPool(false);
        pool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));
        CtClass dungeon = pool.get("com.megacrit.cardcrawl.dungeons.AbstractDungeon");

        try {
            CtMethod update = dungeon.getDeclaredMethod("update");
            RunIdPatches.InjectRunIdPatch.inject(update);

            assertEquals(CtClass.intType, dungeon.getDeclaredField("RunID").getType());
            assertEquals(CtClass.intType, dungeon.getDeclaredMethod("getRunID").getReturnType());
            assertEquals(CtClass.voidType,
                dungeon.getDeclaredMethod("reallyFastMode$setRunID").getReturnType());
            assertTrue(dungeon.subtypeOf(pool.get(RunIdPatches.RunIdentifier.class.getName())));
        } finally {
            dungeon.detach();
        }
    }

    @Test
    public void startupAnimationSkipIsConsumedOnce() {
        RunIdPatches.prepareRun(17);

        try {
            assertTrue(RunIdPatches.shouldSkipStartupAnimations());
            assertTrue(RunIdPatches.consumeStartupFadeSkip());
            assertTrue(!RunIdPatches.consumeStartupFadeSkip());
        } finally {
            RunIdPatches.cancelPreparedRun();
        }
    }
}
