package reallyfastmode.protocol;

import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.campfire.DigOption;
import com.megacrit.cardcrawl.ui.campfire.LiftOption;
import com.megacrit.cardcrawl.ui.campfire.RecallOption;
import com.megacrit.cardcrawl.ui.campfire.RestOption;
import com.megacrit.cardcrawl.ui.campfire.SmithOption;
import com.megacrit.cardcrawl.ui.campfire.TokeOption;
import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaRestOptionCatalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RestOptionCatalogTest {
    @Test
    public void mapsEveryVanillaCampfireOptionToAStableThreeBitId() {
        assertEquals(0, VanillaRestOptionCatalog.wireId(RestOption.class));
        assertEquals(1, VanillaRestOptionCatalog.wireId(SmithOption.class));
        assertEquals(2, VanillaRestOptionCatalog.wireId(DigOption.class));
        assertEquals(3, VanillaRestOptionCatalog.wireId(LiftOption.class));
        assertEquals(4, VanillaRestOptionCatalog.wireId(TokeOption.class));
        assertEquals(5, VanillaRestOptionCatalog.wireId(RecallOption.class));
        assertEquals(7, VanillaRestOptionCatalog.UNKNOWN.wireId);
    }

    @Test
    public void mapsNullAndModdedTypesToUnknown() {
        assertEquals(VanillaRestOptionCatalog.UNKNOWN.wireId,
            VanillaRestOptionCatalog.wireId(null));
        assertEquals(VanillaRestOptionCatalog.UNKNOWN.wireId,
            VanillaRestOptionCatalog.wireId(ModdedOption.class));
    }

    @Test
    public void optionTypeMapIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
            () -> VanillaRestOptionCatalog.optionTypeToWireId.put(ModdedOption.class, 6));
    }

    private static final class ModdedOption extends AbstractCampfireOption {
        @Override
        public void useOption() {
        }
    }
}
