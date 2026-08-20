package reallyfastmode.patches.transitions;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.config.FastModeConfig;

import java.lang.reflect.Field;

public final class SceneTransitionPatches {
    private static final Logger LOGGER = LogManager.getLogger(SceneTransitionPatches.class.getName());
    private static final Field DUNGEON_FADE_TIMER_FIELD = findDungeonFadeTimerField();

    private SceneTransitionPatches() {
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "updateFading")
    public static class DungeonFadePatch {
        @SpirePrefixPatch
        public static void finishFadeImmediately() {
            if (!FastModeConfig.isFastModeEnabled()
                || (!AbstractDungeon.isFadingIn && !AbstractDungeon.isFadingOut)
                || DUNGEON_FADE_TIMER_FIELD == null) {
                return;
            }

            try {
                // Let vanilla updateFading() run its completion branch and room-transition callback.
                DUNGEON_FADE_TIMER_FIELD.setFloat(null, -1.0F);
            } catch (IllegalAccessException exception) {
                LOGGER.error("Unable to expire the dungeon fade timer.", exception);
            }
        }

        @SpirePostfixPatch
        public static void removeRoomFadeOverlay() {
            if (FastModeConfig.isFastModeEnabled()
                && !AbstractDungeon.isDungeonBeaten
                && AbstractDungeon.fadeColor != null) {
                AbstractDungeon.fadeColor.a = 0.0F;
            }
        }
    }

    private static Field findDungeonFadeTimerField() {
        try {
            Field field = AbstractDungeon.class.getDeclaredField("fadeTimer");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            LOGGER.error("Unable to access AbstractDungeon.fadeTimer; dungeon fades will use vanilla timing.", exception);
            return null;
        }
    }
}
