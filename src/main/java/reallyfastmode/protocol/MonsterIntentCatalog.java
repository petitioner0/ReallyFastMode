package reallyfastmode.protocol;

import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Explicit four-bit wire mapping for vanilla monster intents. */
public enum MonsterIntentCatalog {
    ATTACK(0, AbstractMonster.Intent.ATTACK),
    ATTACK_BUFF(1, AbstractMonster.Intent.ATTACK_BUFF),
    ATTACK_DEBUFF(2, AbstractMonster.Intent.ATTACK_DEBUFF),
    ATTACK_DEFEND(3, AbstractMonster.Intent.ATTACK_DEFEND),
    BUFF(4, AbstractMonster.Intent.BUFF),
    DEBUFF(5, AbstractMonster.Intent.DEBUFF),
    STRONG_DEBUFF(6, AbstractMonster.Intent.STRONG_DEBUFF),
    DEFEND(7, AbstractMonster.Intent.DEFEND),
    DEFEND_DEBUFF(8, AbstractMonster.Intent.DEFEND_DEBUFF),
    DEFEND_BUFF(9, AbstractMonster.Intent.DEFEND_BUFF),
    ESCAPE(10, AbstractMonster.Intent.ESCAPE),
    MAGIC(11, AbstractMonster.Intent.MAGIC),
    NONE(12, AbstractMonster.Intent.NONE),
    SLEEP(13, AbstractMonster.Intent.SLEEP),
    STUN(14, AbstractMonster.Intent.STUN),
    UNKNOWN(15, AbstractMonster.Intent.DEBUG, AbstractMonster.Intent.UNKNOWN);

    public static final Map<AbstractMonster.Intent, Integer> intentToWireId;

    static {
        Map<AbstractMonster.Intent, Integer> ids =
            new EnumMap<AbstractMonster.Intent, Integer>(AbstractMonster.Intent.class);
        for (MonsterIntentCatalog value : values()) {
            for (AbstractMonster.Intent intent : value.intents) {
                Integer previous = ids.put(intent, value.wireId);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate Intent mapping: " + intent);
                }
            }
        }
        intentToWireId = Collections.unmodifiableMap(ids);
    }

    public final int wireId;
    private final AbstractMonster.Intent[] intents;

    MonsterIntentCatalog(int wireId, AbstractMonster.Intent... intents) {
        this.wireId = wireId;
        this.intents = intents;
    }

    public static int wireId(AbstractMonster.Intent intent) {
        if (intent == null) {
            return UNKNOWN.wireId;
        }
        Integer wireId = intentToWireId.get(intent);
        return wireId == null ? UNKNOWN.wireId : wireId;
    }
}
