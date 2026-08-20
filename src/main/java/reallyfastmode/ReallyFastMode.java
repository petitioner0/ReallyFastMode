package reallyfastmode;

import basemod.BaseMod;
import basemod.interfaces.PostInitializeSubscriber;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import reallyfastmode.config.FastModeConfig;
import reallyfastmode.ui.FastModeSettingsPanel;


@SpireInitializer
public final class ReallyFastMode implements PostInitializeSubscriber {
    private ReallyFastMode() {
    }

    public static void initialize() {
        FastModeConfig.initialize();
        BaseMod.subscribe(new ReallyFastMode());
    }

    @Override
    public void receivePostInitialize() {
        FastModeSettingsPanel.register();
    }
}
