package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.map.MapRoomNode;
import reallyfastmode.access.MapAccess;
import reallyfastmode.protocol.CurrentMapSelectionProtocol;

import java.util.Objects;

/** Captures the current map coordinate. */
public final class CurrentMapSelectionSnapshotBuilder {
    private CurrentMapSelectionSnapshotBuilder() {
    }

    public static CurrentMapSelectionSnapshot build() {
        return buildFromSource(new StsSource());
    }

    static CurrentMapSelectionSnapshot buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int currentNodeX = encodeCoordinate("currentNodeX", source.currentNodeX());
        int currentNodeY = encodeCoordinate("currentNodeY", source.currentNodeY());
        return new CurrentMapSelectionSnapshot(currentNodeX, currentNodeY);
    }

    private static int encodeCoordinate(String name, int value) {
        int max = (1 << CurrentMapSelectionProtocol.COORDINATE_BITS) - 1;
        if (value < -1 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside -1-" + max);
        }
        return value & max;
    }

    interface Source {
        int currentNodeX();

        int currentNodeY();
    }

    private static final class StsSource implements Source {
        private final MapRoomNode currentNode;

        private StsSource() {
            this.currentNode = Objects.requireNonNull(
                MapAccess.currentNode(), "current map node"
            );
        }

        @Override
        public int currentNodeX() {
            return currentNode.x;
        }

        @Override
        public int currentNodeY() {
            return currentNode.y;
        }

    }
}
