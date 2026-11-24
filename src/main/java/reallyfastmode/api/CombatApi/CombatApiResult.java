package reallyfastmode.api.CombatApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable result returned by a combat API command. */
public final class CombatApiResult {
    private static final String OK_CODE = "ok";

    private final boolean success;
    private final String code;
    private final String message;
    private final List<String> selectedCardNames;

    private CombatApiResult(boolean success, String code, String message, List<String> selectedCardNames) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.selectedCardNames = selectedCardNames;
    }

    public static CombatApiResult success(String message) {
        return new CombatApiResult(true, OK_CODE, message, Collections.emptyList());
    }

    static CombatApiResult success(String message, List<String> selectedCardNames) {
        return new CombatApiResult(
            true, OK_CODE, message, Collections.unmodifiableList(new ArrayList<>(selectedCardNames))
        );
    }

    public static CombatApiResult error(String code, String message) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Error code must not be empty.");
        }
        return new CombatApiResult(false, code, message, Collections.emptyList());
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /** Returns the in-game display names of the selected cards. */
    public List<String> getSelectedCardNames() {
        return selectedCardNames;
    }

    @Override
    public String toString() {
        return "CombatApiResult{" +
            "success=" + success +
            ", code='" + code + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
