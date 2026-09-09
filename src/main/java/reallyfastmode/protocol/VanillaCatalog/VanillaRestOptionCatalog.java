package reallyfastmode.protocol.VanillaCatalog;

import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import com.megacrit.cardcrawl.ui.campfire.DigOption;
import com.megacrit.cardcrawl.ui.campfire.LiftOption;
import com.megacrit.cardcrawl.ui.campfire.RecallOption;
import com.megacrit.cardcrawl.ui.campfire.RestOption;
import com.megacrit.cardcrawl.ui.campfire.SmithOption;
import com.megacrit.cardcrawl.ui.campfire.TokeOption;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Explicit three-bit wire mapping for vanilla campfire options. */
public enum VanillaRestOptionCatalog {
    REST(0, RestOption.class),
    SMITH(1, SmithOption.class),
    DIG(2, DigOption.class),
    LIFT(3, LiftOption.class),
    TOKE(4, TokeOption.class),
    RECALL(5, RecallOption.class),
    UNKNOWN(7, null);

    public static final Map<Class<? extends AbstractCampfireOption>, Integer>
        optionTypeToWireId;

    static {
        Map<Class<? extends AbstractCampfireOption>, Integer> ids =
            new LinkedHashMap<Class<? extends AbstractCampfireOption>, Integer>();
        for (VanillaRestOptionCatalog value : values()) {
            if (value.optionType != null) {
                Integer previous = ids.put(value.optionType, value.wireId);
                if (previous != null) {
                    throw new IllegalStateException(
                        "Duplicate campfire option mapping: " + value.optionType.getName()
                    );
                }
            }
        }
        optionTypeToWireId = Collections.unmodifiableMap(ids);
    }

    public final int wireId;
    public final Class<? extends AbstractCampfireOption> optionType;

    VanillaRestOptionCatalog(
        int wireId,
        Class<? extends AbstractCampfireOption> optionType
    ) {
        this.wireId = wireId;
        this.optionType = optionType;
    }

    /** Maps exact vanilla option types; modded subclasses are unknown. */
    public static int wireId(Class<? extends AbstractCampfireOption> optionType) {
        if (optionType == null) {
            return UNKNOWN.wireId;
        }
        Integer wireId = optionTypeToWireId.get(optionType);
        return wireId == null ? UNKNOWN.wireId : wireId;
    }
}
