package reallyfastmode.patches.access;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EventDialogAccessPatchTest {
    @Test
    public void imageDialogRequiresVisibilityAndCompletedEntranceAnimation() throws Exception {
        Class<?> patched = patchedDialog("com.megacrit.cardcrawl.events.GenericEventDialog");
        Object dialog = patched.getConstructor().newInstance();
        PrivateFieldAccess.ImageEventDialogFields fields = (PrivateFieldAccess.ImageEventDialogFields) dialog;
        Field show = field(patched, "show");
        Field timer = field(patched, "animateTimer");

        assertFalse(fields.reallyFastMode$imageEventDialogReady());
        show.setBoolean(null, true);
        timer.setFloat(dialog, 0.5F);
        assertFalse(fields.reallyFastMode$imageEventDialogReady());
        timer.setFloat(dialog, 0.0F);
        assertTrue(fields.reallyFastMode$imageEventDialogReady());
        show.setBoolean(null, false);
        assertFalse(fields.reallyFastMode$imageEventDialogReady());
    }

    @Test
    public void roomDialogUsesInstanceVisibilityAsVanillaDoes() throws Exception {
        Class<?> patched = patchedDialog("com.megacrit.cardcrawl.events.RoomEventDialog");
        Object dialog = patched.getConstructor().newInstance();
        PrivateFieldAccess.RoomEventDialogFields fields = (PrivateFieldAccess.RoomEventDialogFields) dialog;
        Field show = field(patched, "show");

        assertFalse(fields.reallyFastMode$roomEventDialogVisible());
        show.setBoolean(dialog, true);
        field(patched, "animateTimer").setFloat(dialog, 0.5F);
        assertTrue(fields.reallyFastMode$roomEventDialogVisible());
        show.setBoolean(dialog, false);
        assertFalse(fields.reallyFastMode$roomEventDialogVisible());
    }

    private Class<?> patchedDialog(String name) throws Exception {
        ClassPool pool = new ClassPool(false);
        pool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));
        CtClass dialog = pool.get(name);
        try {
            PrivateFieldAccess.InjectAccessorsPatch.inject(dialog.getDeclaredMethod("update"));
            return new PatchedDialogLoader().define(name, dialog.toBytecode());
        } finally {
            dialog.detach();
        }
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static final class PatchedDialogLoader extends ClassLoader {
        PatchedDialogLoader() {
            super(EventDialogAccessPatchTest.class.getClassLoader());
        }

        Class<?> define(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }
}
