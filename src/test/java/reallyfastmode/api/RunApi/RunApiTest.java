package reallyfastmode.api.RunApi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RunApiTest {
    @Test
    public void rejectsAscensionBelowOne() {
        RunApiResult result = RunApi.startRun(123L, 7, 0);

        assertFalse(result.isSuccess());
        assertEquals("invalid_ascension_level", result.getCode());
    }

    @Test
    public void rejectsAscensionAboveTwenty() {
        RunApiResult result = RunApi.startRun(123L, 7, 21);

        assertFalse(result.isSuccess());
        assertEquals("invalid_ascension_level", result.getCode());
    }
}
