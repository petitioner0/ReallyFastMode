package reallyfastmode.protocol.VanillaCatalog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Fixed vanilla orb IDs in lexicographic wire order. */
public enum VanillaOrbCatalog {
    DARK(0, "Dark"),
    EMPTY(1, "Empty"),
    FROST(2, "Frost"),
    LIGHTNING(3, "Lightning"),
    PLASMA(4, "Plasma");

    public static final int UNKNOWN_WIRE_ID = values().length;
    public static final Map<String, Integer> orbIdToWireId;

    static {
        Map<String, Integer> ids = new LinkedHashMap<String, Integer>();
        for (VanillaOrbCatalog orb : values()) {
            ids.put(orb.orbId, orb.wireId);
        }
        orbIdToWireId = Collections.unmodifiableMap(ids);
    }

    public final int wireId;
    public final String orbId;

    VanillaOrbCatalog(int wireId, String orbId) {
        this.wireId = wireId;
        this.orbId = orbId;
    }
}
