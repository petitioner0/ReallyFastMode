package reallyfastmode.access;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.screens.CombatRewardScreen;
import reallyfastmode.protocol.VanillaCatalog.VanillaRewardTypeCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Reads the rewards currently owned by the room or displayed by reward screens. */
public final class RewardAccess {
    private RewardAccess() {
    }

    public static List<RewardItem> roomRewards() {
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        return room == null ? Collections.<RewardItem>emptyList() : immutableCopy(room.rewards);
    }

    public static List<RewardItem> combatRewards() {
        CombatRewardScreen screen = AbstractDungeon.combatRewardScreen;
        return AbstractDungeon.screen != AbstractDungeon.CurrentScreen.COMBAT_REWARD || screen == null
            ? Collections.<RewardItem>emptyList()
            : immutableCopy(screen.rewards);
    }

    public static List<AbstractCard> cardRewardChoices() {
        CardRewardScreen screen = AbstractDungeon.cardRewardScreen;
        return AbstractDungeon.screen != AbstractDungeon.CurrentScreen.CARD_REWARD || screen == null
            ? Collections.<AbstractCard>emptyList()
            : immutableCopy(screen.rewardGroup);
    }

    public static boolean cardRewardTakenAll() {
        return cardRewardScreen().hasTakenAll;
    }

    public static RewardItem.RewardType type(RewardItem reward) {
        return reward(reward).type;
    }

    /** Returns the reward type's explicit three-bit wire id. */
    public static int typeWireId(RewardItem reward) {
        return VanillaRewardTypeCatalog.wireId(reward(reward).type);
    }

    public static int gold(RewardItem reward) {
        return reward(reward).goldAmt;
    }

    public static int bonusGold(RewardItem reward) {
        return reward(reward).bonusGold;
    }

    public static RewardItem relicLink(RewardItem reward) {
        return reward(reward).relicLink;
    }

    public static AbstractRelic relic(RewardItem reward) {
        return reward(reward).relic;
    }

    public static AbstractPotion potion(RewardItem reward) {
        return reward(reward).potion;
    }

    public static List<AbstractCard> cards(RewardItem reward) {
        return immutableCopy(reward(reward).cards);
    }

    public static boolean done(RewardItem reward) {
        return reward(reward).isDone;
    }

    public static boolean ignored(RewardItem reward) {
        return reward(reward).ignoreReward;
    }

    private static CardRewardScreen cardRewardScreen() {
        if (AbstractDungeon.screen != AbstractDungeon.CurrentScreen.CARD_REWARD
            || AbstractDungeon.cardRewardScreen == null) {
            throw new IllegalStateException("No card reward screen is currently active.");
        }
        return AbstractDungeon.cardRewardScreen;
    }

    private static RewardItem reward(RewardItem reward) {
        return Objects.requireNonNull(reward, "reward");
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return source == null || source.isEmpty()
            ? Collections.<T>emptyList()
            : Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
