package reallyfastmode.protocol.write;

import reallyfastmode.protocol.CurrentMapSelectionProtocol;
import reallyfastmode.protocol.MapProtocol;
import reallyfastmode.protocol.build.CurrentMapSelectionSnapshot;
import reallyfastmode.protocol.io.BitWriter;

import java.util.Objects;

/** Writes the map block kind followed by the coordinates in X, Y order. */
public final class CurrentMapSelectionBlockWriter {
    private CurrentMapSelectionBlockWriter() {
    }

    public static void write(BitWriter out, CurrentMapSelectionSnapshot selection) {
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(selection, "selection");

        int x = selection.currentNodeX;
        int y = selection.currentNodeY;
        int bits = CurrentMapSelectionProtocol.COORDINATE_BITS;
        int max = (1 << bits) - 1;
        if (x < 0 || x > max || y < 0 || y > max) {
            throw new IllegalArgumentException(
                "Current map coordinate (" + x + "," + y + ") outside 0-" + max
            );
        }

        int coordinateBits = CurrentMapSelectionProtocol.CURRENT_NODE_BITS;
        int coordinates = (x << bits) | y;
        out.writeBits((MapProtocol.CURRENT_NODE_KIND << coordinateBits) | coordinates,
            CurrentMapSelectionProtocol.CURRENT_NODE_BLOCK_BITS);
    }
}
