package reallyfastmode.patches.access;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MapNodeAccessPatchTest {
    @Test
    public void queuesTheActualVanillaTimerAndDetectsManualSelections() throws Exception {
        ClassPool pool = new ClassPool(false);
        pool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));
        CtClass node = pool.get("com.megacrit.cardcrawl.map.MapRoomNode");

        try {
            PrivateFieldAccess.InjectAccessorsPatch.inject(node.getDeclaredMethod("update"));
            Class<?> patched = new PatchedNodeLoader().define(node.getName(), node.toBytecode());
            Object instance = patched.getConstructor(int.class, int.class).newInstance(2, 5);
            PrivateFieldAccess.MapNodeFields fields = (PrivateFieldAccess.MapNodeFields) instance;
            Field timer = patched.getDeclaredField("animWaitTimer");
            timer.setAccessible(true);

            assertFalse(fields.reallyFastMode$mapNodeSelectionPending());
            timer.setFloat(instance, 0.25F);
            assertTrue(fields.reallyFastMode$mapNodeSelectionPending());
            timer.setFloat(instance, 0.0F);
            assertFalse(fields.reallyFastMode$mapNodeSelectionPending());

            fields.reallyFastMode$queueMapNodeSelection();
            assertTrue(fields.reallyFastMode$mapNodeSelectionPending());
            assertEquals(-1.0F, timer.getFloat(instance), 0.0F);
        } finally {
            node.detach();
        }
    }

    private static final class PatchedNodeLoader extends ClassLoader {
        PatchedNodeLoader() {
            super(MapNodeAccessPatchTest.class.getClassLoader());
        }

        Class<?> define(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }
}
