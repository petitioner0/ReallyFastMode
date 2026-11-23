package reallyfastmode;

import basemod.BaseMod;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import reallyfastmode.cli.GameCliServer;
import reallyfastmode.config.FastModeConfig;
import reallyfastmode.patches.access.RunIdPatches;
import reallyfastmode.patches.vfx.AdvancedCombatEffectPatches;
import reallyfastmode.ui.FastModeSettingsPanel;


@SpireInitializer
public final class ReallyFastMode implements PostInitializeSubscriber {
    private ReallyFastMode() {
    }

    public static void initialize() {
        FastModeConfig.initialize();
        RunIdPatches.registerSaveField();
        BaseMod.subscribe(new ReallyFastMode());
    }

    @Override
    public void receivePostInitialize() {
        AdvancedCombatEffectPatches.installFilteringLists();
        FastModeSettingsPanel.register();
        GameCliServer.start(Gdx.app);
    }
}
