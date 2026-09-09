package reallyfastmode.protocol.build;

/** Semantic snapshot of an event and its currently displayed options. */
public final class EventSnapshot {
    public final int eventWireId;
    public final int optionsEntryCount;
    public final boolean[] optionsApplicability;

    public EventSnapshot(int eventWireId, int optionsEntryCount) {
        if (optionsEntryCount < 0) {
            throw new IllegalArgumentException(
                "optionsEntryCount=" + optionsEntryCount
            );
        }
        this.eventWireId = eventWireId;
        this.optionsEntryCount = optionsEntryCount;
        this.optionsApplicability = new boolean[optionsEntryCount];
    }
}
