package reallyfastmode.api.RunApi;

/** Immutable result returned by a run-level API command. */
public final class RunApiResult {
    private static final String OK_CODE = "ok";

    private final boolean success;
    private final String code;
    private final String message;

    private RunApiResult(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public static RunApiResult success(String message) {
        return new RunApiResult(true, OK_CODE, message);
    }

    public static RunApiResult error(String code, String message) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Error code must not be empty.");
        }
        return new RunApiResult(false, code, message);
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
        return "RunApiResult{" +
            "success=" + success +
            ", code='" + code + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
