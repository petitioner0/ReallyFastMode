package reallyfastmode.api.RestApi;

import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import org.junit.Test;
import reallyfastmode.protocol.VanillaCatalog.VanillaRestOptionCatalog;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RestApiTest {
    @Test
    public void rejectsNullAndUnknownBeforeReadingGameState() {
        assertThrows(IllegalArgumentException.class, () -> RestApi.selectOption(null));
        assertThrows(IllegalArgumentException.class,
            () -> RestApi.selectOption(VanillaRestOptionCatalog.UNKNOWN));
    }

    @Test
    public void returnsTheCurrentInstanceWithoutTriggeringItDuringValidation() {
        TestOption current = new TestOption();
        OtherOption other = new OtherOption();

        assertSame(current, RestApi.requireUsableOption(TestOption.class,
            Arrays.<AbstractCampfireOption>asList(other, current)));
        assertEquals(0, current.uses);
        assertEquals(0, other.uses);
    }

    @Test
    public void disabledOptionThrowsWithoutChangingInputOrUsingAnotherOption() {
        TestOption disabled = new TestOption();
        disabled.usable = false;
        disabled.hb.clicked = true;
        OtherOption other = new OtherOption();

        assertThrows(IllegalArgumentException.class,
            () -> RestApi.requireUsableOption(TestOption.class,
                Arrays.<AbstractCampfireOption>asList(disabled, other)));
        assertTrue(disabled.hb.clicked);
        assertEquals(0, disabled.uses);
        assertEquals(0, other.uses);
    }

    @Test
    public void missingOptionThrowsInsteadOfFallingBack() {
        OtherOption other = new OtherOption();

        assertThrows(IllegalArgumentException.class,
            () -> RestApi.requireUsableOption(TestOption.class,
                Collections.<AbstractCampfireOption>singletonList(other)));
        assertThrows(IllegalArgumentException.class,
            () -> RestApi.requireUsableOption(TestOption.class,
                Collections.<AbstractCampfireOption>emptyList()));
        assertEquals(0, other.uses);
    }

    @Test
    public void doesNotTreatASubclassAsTheExactCatalogType() {
        OtherOption subclass = new OtherOption();

        assertThrows(IllegalArgumentException.class,
            () -> RestApi.requireUsableOption(TestOption.class,
                Collections.<AbstractCampfireOption>singletonList(subclass)));
    }

    private static class TestOption extends AbstractCampfireOption {
        int uses;

        @Override
        public void useOption() {
            uses++;
        }
    }

    private static final class OtherOption extends TestOption {
    }
}
