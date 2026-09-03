package reallyfastmode.patches.access;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/** Assigns a room-local four-bit id to each initialized monster instance. */
public final class MonsterInstanceIdPatches {
    private static final int INSTANCE_ID_LIMIT = 16;
    private static int counter;

    private MonsterInstanceIdPatches() {
    }

    /** Used by code injected into {@link AbstractMonster#init()}. */
    public static int nextInstanceId() {
        if (counter >= INSTANCE_ID_LIMIT) {
            throw new IllegalStateException(
                "A combat room cannot initialize more than 16 monster instances."
            );
        }
        return counter++;
    }

    public static int instanceId(AbstractMonster monster) {
        return ((MonsterInstanceId) monster).reallyFastMode$instanceId();
    }

    public interface MonsterInstanceId {
        int reallyFastMode$instanceId();
    }

    @SpirePatch2(clz = MonsterGroup.class, method = "init")
    public static class ResetCounterPatch {
        @SpirePrefixPatch
        public static void resetCounter() {
            counter = 0;
        }
    }

    @SpirePatch2(clz = AbstractMonster.class, method = "init")
    public static class InitializeInstanceIdPatch {
        @SpireRawPatch
        public static void inject(CtBehavior behavior)
            throws CannotCompileException, NotFoundException {
            CtClass target = behavior.getDeclaringClass();
            target.addField(CtField.make("public int instanceId;", target));
            target.addInterface(target.getClassPool().get(MonsterInstanceId.class.getName()));
            target.addMethod(CtNewMethod.make(
                "public int reallyFastMode$instanceId() { return this.instanceId; }",
                target
            ));
            behavior.insertBefore(
                "this.instanceId = reallyfastmode.patches.access."
                    + "MonsterInstanceIdPatches.nextInstanceId();"
            );
        }
    }
}
