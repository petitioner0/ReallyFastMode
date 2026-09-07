package reallyfastmode.access;

import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;
import reallyfastmode.protocol.VanillaCatalog.VanillaPotionCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Reads the player's potion inventory and potion properties. */
public final class PotionAccess {
    private PotionAccess() {
    }

    public static List<AbstractPotion> potions() {
        return AbstractDungeon.player == null
            ? Collections.<AbstractPotion>emptyList()
            : immutableCopy(AbstractDungeon.player.potions);
    }

    public static int slotCount() {
        if (AbstractDungeon.player == null) {
            throw new IllegalStateException("No current player is available.");
        }
        return AbstractDungeon.player.potionSlots;
    }

    public static boolean emptySlot(AbstractPotion potion) {
        return potion(potion) instanceof PotionSlot;
    }

    public static String id(AbstractPotion potion) {
        return potion(potion).ID;
    }

    public static int wireId(AbstractPotion potion) {
        AbstractPotion checkedPotion = potion(potion);
        if (checkedPotion instanceof PotionSlot) {
            return VanillaPotionCatalog.EMPTY.wireId;
        }
        Integer wireId = VanillaPotionCatalog.potionIdToWireId.get(checkedPotion.ID);
        return wireId == null ? VanillaPotionCatalog.UNKNOWN.wireId : wireId;
    }

    public static int slot(AbstractPotion potion) {
        return potion(potion).slot;
    }

    public static int price(AbstractPotion potion) {
        return potion(potion).getPrice();
    }

    public static boolean canUse(AbstractPotion potion) {
        return potion(potion).canUse();
    }

    public static boolean canDiscard(AbstractPotion potion) {
        return potion(potion).canDiscard();
    }

    private static AbstractPotion potion(AbstractPotion potion) {
        return Objects.requireNonNull(potion, "potion");
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
