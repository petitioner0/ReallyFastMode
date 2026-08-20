package reallyfastmode.api;

/** Immutable result returned by a combat API command. */
public final class CombatApiResult {
    private static final String OK_CODE = "ok";

    private final boolean success;
    private final String code;
    private final String message;

    private CombatApiResult(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public static CombatApiResult success(String message) {
        return new CombatApiResult(true, OK_CODE, message);
    }

    public static CombatApiResult error(String code, String message) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Error code must not be empty.");
        }
        return new CombatApiResult(false, code, message);
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

    @Override
    public String toString() {
        return "CombatApiResult{" +
            "success=" + success +
            ", code='" + code + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
