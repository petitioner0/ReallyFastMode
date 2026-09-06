package reallyfastmode.api.RunApi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RunApiResultTest {
    @Test
    public void createsSuccessResult() {
        RunApiResult result = RunApiResult.success("started");

        assertTrue(result.isSuccess());
        assertEquals("ok", result.getCode());
        assertEquals("started", result.getMessage());
    }

    @Test
    public void createsErrorResult() {
        RunApiResult result = RunApiResult.error("run_already_active", "busy");

        assertFalse(result.isSuccess());
        assertEquals("run_already_active", result.getCode());
        assertEquals("busy", result.getMessage());
    }

    @Test
    public void rejectsEmptyErrorCode() {
        assertThrows(IllegalArgumentException.class, () -> RunApiResult.error(" ", "bad"));
    }
}
