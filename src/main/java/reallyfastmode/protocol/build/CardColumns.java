package reallyfastmode.protocol.build;

import java.util.Objects;

/** Semantic Structure-of-Arrays snapshot for one card slot. */
public final class CardColumns {
    public enum Layout {
        DECK,
        COMBAT
    }

    public final Layout layout;
    public final int size;

    public final int[] cardWireId;
    public final int[] cost;
    public final boolean[] upgraded;
    public final boolean[] inBottleFlame;
    public final boolean[] inBottleLightning;
    public final boolean[] inBottleTornado;

    public CardColumns(Layout layout, int size) {
        this.layout = Objects.requireNonNull(layout, "layout");
        if (size < 0) {
            throw new IllegalArgumentException("size=" + size);
        }
        this.size = size;
        this.cardWireId = new int[size];
        this.cost = new int[size];
        this.upgraded = new boolean[size];
        this.inBottleFlame = new boolean[size];
        this.inBottleLightning = new boolean[size];
        this.inBottleTornado = new boolean[size];
    }
}
