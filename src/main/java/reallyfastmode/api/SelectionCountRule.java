package reallyfastmode.api;

/**
 * Mirrors vanilla card-selection confirmation counts. Confirming zero cards
 * remains distinct from skipping a card-reward screen.
 */
final class SelectionCountRule {
    private final int maximum;
    private final boolean exact;
    private final boolean canConfirmZero;

    private SelectionCountRule(int maximum, boolean exact, boolean canConfirmZero) {
        if (maximum < 0) {
            throw new IllegalArgumentException("maximum must not be negative");
        }
        this.maximum = maximum;
        this.exact = exact;
        this.canConfirmZero = canConfirmZero;
    }

    static SelectionCountRule exact(int count, boolean canConfirmZero) {
        return new SelectionCountRule(count, true, canConfirmZero);
    }

    static SelectionCountRule range(int maximum, boolean canConfirmZero) {
        return new SelectionCountRule(maximum, false, canConfirmZero);
    }

    boolean allows(int actual) {
        if (actual < 0 || actual > maximum) {
            return false;
        }
        if (actual == 0) {
            return canConfirmZero;
        }
        return !exact || actual == maximum;
    }

    String describe() {
        if (exact) {
            return canConfirmZero
                ? "requires either zero or exactly " + maximum + " card(s)"
                : "requires exactly " + maximum + " card(s)";
        }
        return canConfirmZero
            ? "allows between zero and " + maximum + " card(s)"
            : "allows between one and " + maximum + " card(s)";
    }
}
