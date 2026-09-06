package reallyfastmode.protocol.build;

/** Immutable semantic snapshot for the single scalar Game block. */
public final class GameSnapshot {
    public final int actWireId;
    public final int floor;
    public final int ascensionLevel;
    public final boolean hasRubyKey;
    public final boolean hasEmeraldKey;
    public final boolean hasSapphireKey;

    public GameSnapshot(
        int actWireId,
        int floor,
        int ascensionLevel,
        boolean hasRubyKey,
        boolean hasEmeraldKey,
        boolean hasSapphireKey
    ) {
        this.actWireId = actWireId;
        this.floor = floor;
        this.ascensionLevel = ascensionLevel;
        this.hasRubyKey = hasRubyKey;
        this.hasEmeraldKey = hasEmeraldKey;
        this.hasSapphireKey = hasSapphireKey;
    }
}
