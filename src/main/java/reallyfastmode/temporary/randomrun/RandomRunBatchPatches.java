package reallyfastmode.temporary.randomrun;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;

/** Self-contained update hook for the removable temporary run harness. */
public final class RandomRunBatchPatches {
    private RandomRunBatchPatches() {
    }

    @SpirePatch2(clz = CardCrawlGame.class, method = "update")
    public static class GameUpdatePatch {
        @SpirePostfixPatch
        public static void updateTemporaryRunBatch() {
            RandomRunBatchController.update();
        }
    }
}
