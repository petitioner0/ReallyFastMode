package reallyfastmode.ui;

import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.helpers.FontHelper;
import reallyfastmode.config.FastModeConfig;

/** Registers the in-game BaseMod settings entry for Really Fast Mode. */
public final class FastModeSettingsPanel {
    private static final String MOD_NAME = "Really Fast Mode";
    private static final String AUTHOR = "petitioner0";
    private static final String DESCRIPTION = "Skips combat animations and visual-only effects.";

    private static final float TOGGLE_X = 350.0F;
    private static final float TOGGLE_Y = 700.0F;

    private FastModeSettingsPanel() {
    }

    public static void register() {
        ModPanel panel = new ModPanel();
        panel.addUIElement(new ModLabeledToggleButton(
            "Enable really fast mode",
            TOGGLE_X,
            TOGGLE_Y,
            Color.WHITE,
            FontHelper.charDescFont,
            FastModeConfig.isFastModeEnabled(),
            panel,
            label -> { },
            toggle -> {
                if (!FastModeConfig.confirmFastModeEnabled(toggle.enabled)) {
                    toggle.enabled = FastModeConfig.isFastModeEnabled();
                }
            }
        ));

        BaseMod.registerModBadge(createBadgeTexture(), MOD_NAME, AUTHOR, DESCRIPTION, panel);
    }

    private static Texture createBadgeTexture() {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.08F, 0.10F, 0.16F, 1.0F);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
