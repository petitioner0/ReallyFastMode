package reallyfastmode.patches.access;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CampfireAccessPatchTest {
    @Test
    public void updatePatchExposesTheActualVanillaButtonsField() throws Exception {
        ClassPool pool = new ClassPool(false);
        pool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));
        CtClass campfire = pool.get("com.megacrit.cardcrawl.rooms.CampfireUI");

        try {
            PrivateFieldAccess.InjectAccessorsPatch.inject(campfire.getDeclaredMethod("update"));

            assertTrue(campfire.subtypeOf(pool.get(PrivateFieldAccess.CampfireFields.class.getName())));
            assertEquals("java.util.ArrayList",
                campfire.getDeclaredMethod("reallyFastMode$campfireButtons").getReturnType().getName());
            final boolean[] readsButtons = {false};
            campfire.getDeclaredMethod("reallyFastMode$campfireButtons").instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess access) {
                    if (access.isReader() && "buttons".equals(access.getFieldName())
                        && "com.megacrit.cardcrawl.rooms.CampfireUI".equals(access.getClassName())) {
                        readsButtons[0] = true;
                    }
                }
            });
            assertTrue(readsButtons[0]);
        } finally {
            campfire.detach();
        }
    }
}
