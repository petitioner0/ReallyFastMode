package reallyfastmode.access;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import reallyfastmode.patches.access.PrivateFieldAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Reads the inventory currently held by the vanilla shop screen. */
public final class ShopAccess {
    private ShopAccess() {
    }

    private static boolean active() {
        return AbstractDungeon.screen == AbstractDungeon.CurrentScreen.SHOP
            && AbstractDungeon.shopScreen != null
            && AbstractDungeon.shopScreen.isActive;
    }

    public static List<AbstractCard> coloredCards() {
        ShopScreen screen = AbstractDungeon.shopScreen;
        return !active() || screen == null
            ? Collections.<AbstractCard>emptyList()
            : immutableCopy(screen.coloredCards);
    }

    public static List<AbstractCard> colorlessCards() {
        ShopScreen screen = AbstractDungeon.shopScreen;
        return !active() || screen == null
            ? Collections.<AbstractCard>emptyList()
            : immutableCopy(screen.colorlessCards);
    }

    public static List<AbstractCard> cards() {
        List<AbstractCard> cards = new ArrayList<AbstractCard>();
        cards.addAll(coloredCards());
        cards.addAll(colorlessCards());
        return immutableCopy(cards);
    }

    public static List<StoreRelic> relics() {
        ShopScreen screen = AbstractDungeon.shopScreen;
        return !active() || screen == null
            ? Collections.<StoreRelic>emptyList()
            : remainingRelics(PrivateFieldAccess.shopRelics(screen));
    }

    public static List<StorePotion> potions() {
        ShopScreen screen = AbstractDungeon.shopScreen;
        return !active() || screen == null
            ? Collections.<StorePotion>emptyList()
            : remainingPotions(PrivateFieldAccess.shopPotions(screen));
    }

    public static boolean purgeAvailable() {
        return shopScreen().purgeAvailable;
    }

    public static int purgeCost() {
        shopScreen();
        return ShopScreen.purgeCost;
    }

    public static int actualPurgeCost() {
        shopScreen();
        return ShopScreen.actualPurgeCost;
    }

    public static AbstractRelic relic(StoreRelic storeRelic) {
        return storeRelic(storeRelic).relic;
    }

    public static int price(StoreRelic storeRelic) {
        return storeRelic(storeRelic).price;
    }

    public static AbstractPotion potion(StorePotion storePotion) {
        return storePotion(storePotion).potion;
    }

    public static int price(StorePotion storePotion) {
        return storePotion(storePotion).price;
    }

    private static ShopScreen shopScreen() {
        if (!active()) {
            throw new IllegalStateException("No shop screen is currently active.");
        }
        return AbstractDungeon.shopScreen;
    }

    private static StoreRelic storeRelic(StoreRelic storeRelic) {
        return Objects.requireNonNull(storeRelic, "storeRelic");
    }

    private static StorePotion storePotion(StorePotion storePotion) {
        return Objects.requireNonNull(storePotion, "storePotion");
    }

    private static List<StoreRelic> remainingRelics(List<StoreRelic> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        List<StoreRelic> result = new ArrayList<StoreRelic>();
        for (StoreRelic relic : source) {
            if (relic == null || !relic.isPurchased) {
                result.add(relic);
            }
        }
        return immutableCopy(result);
    }

    private static List<StorePotion> remainingPotions(List<StorePotion> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        List<StorePotion> result = new ArrayList<StorePotion>();
        for (StorePotion potion : source) {
            if (potion == null || !potion.isPurchased) {
                result.add(potion);
            }
        }
        return immutableCopy(result);
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
