package reallyfastmode.protocol.build;

/** Semantic snapshot of the currently usable campfire options. */
public final class RestSnapshot {
    public final int optionsCount;
    public final int[] optionsWireId;

    public RestSnapshot(int optionsCount) {
        if (optionsCount < 0) {
            throw new IllegalArgumentException("optionsCount=" + optionsCount);
        }
        this.optionsCount = optionsCount;
        this.optionsWireId = new int[optionsCount];
    }
}
