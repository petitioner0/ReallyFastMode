package reallyfastmode.api;

import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.utility.HandCheckAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.PotionSlot;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Semantic combat commands that bypass mouse input while preserving vanilla
 * action, potion, and selection resolution.
 *
 * <p>These methods read and mutate Slay the Spire's global state and therefore
 * must be called on the game's render thread. A future transport layer should
 * enqueue work onto that thread before invoking this class.</p>
 */
public final class CombatApi {
    private static final Logger LOGGER = LogManager.getLogger(CombatApi.class.getName());

    private CombatApi() {
    }

    public static CombatApiResult playCard(String cardId) {
        return playCard(cardId, null);
    }

    public static CombatApiResult playCard(String cardId, String targetId) {
        try {
            CombatApiResult readiness = validatePlayerCommandReady();
            if (readiness != null) {
                return readiness;
            }
            if (cardId == null || cardId.trim().isEmpty()) {
                return CombatApiResult.error("card_not_found", "card_id must not be empty.");
            }

            AbstractCard card = findCardInHand(cardId.trim());
            if (card == null) {
                return CombatApiResult.error("card_not_found", "Card is not present in the player's hand.");
            }
            for (CardQueueItem item : AbstractDungeon.actionManager.cardQueue) {
                if (item != null && item.card == card) {
                    return CombatApiResult.error("card_already_queued", "Card is already queued for play.");
                }
            }

            AbstractMonster target = null;
            if (requiresTarget(card)) {
                TargetResult targetResult = requireActiveTarget(targetId);
                if (targetResult.error != null) {
                    return targetResult.error;
                }
                target = targetResult.monster;
            }

            if (!card.canUse(AbstractDungeon.player, target)) {
                String message = card.cantUseMessage == null
                    ? "Card cannot be played in the current state."
                    : card.cantUseMessage;
                return CombatApiResult.error("card_not_playable", message);
            }

            card.unhover();
            card.untip();
            if (target != null && AbstractDungeon.player.hasPower("Surrounded")) {
                AbstractDungeon.player.flipHorizontal = target.drawX < AbstractDungeon.player.drawX;
            }
            AbstractDungeon.actionManager.addCardQueueItem(new CardQueueItem(card, target));
            AbstractDungeon.player.releaseCard();
            return CombatApiResult.success("Card queued for play.");
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to execute play_card.", exception);
            return CombatApiResult.error("internal_error", "play_card failed unexpectedly.");
        }
    }

    public static CombatApiResult usePotion(int potionSlot) {
        return usePotion(potionSlot, null);
    }

    public static CombatApiResult usePotion(int potionSlot, String targetId) {
        try {
            CombatApiResult readiness = validatePlayerCommandReady();
            if (readiness != null) {
                return readiness;
            }
            if (potionSlot < 0 || potionSlot >= AbstractDungeon.player.potions.size()) {
                return CombatApiResult.error("potion_not_found", "Potion slot is outside the player's inventory.");
            }

            AbstractPotion potion = AbstractDungeon.player.potions.get(potionSlot);
            if (potion == null || potion instanceof PotionSlot) {
                return CombatApiResult.error("potion_not_found", "Potion slot is empty.");
            }
            if (!potion.canUse()) {
                return CombatApiResult.error("potion_not_usable", "Potion cannot be used in the current state.");
            }

            AbstractMonster target = null;
            if (potion.targetRequired) {
                TargetResult targetResult = requireActiveTarget(targetId);
                if (targetResult.error != null) {
                    return targetResult.error;
                }
                target = targetResult.monster;
            }

            CardCrawlGame.metricData.potions_floor_usage.add(AbstractDungeon.floorNum);
            potion.use(target);
            if (potion.targetRequired) {
                AbstractDungeon.actionManager.addToBottom(new HandCheckAction());
            }
            for (AbstractRelic relic : AbstractDungeon.player.relics) {
                relic.onUsePotion();
            }
            AbstractPotion.playPotionSound();
            AbstractDungeon.topPanel.destroyPotion(potionSlot);
            return CombatApiResult.success("Potion used.");
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to execute use_potion.", exception);
            return CombatApiResult.error("internal_error", "use_potion failed unexpectedly.");
        }
    }

    public static CombatApiResult selectCards(String... cardIds) {
        if (cardIds == null) {
            return selectCards((List<String>) null);
        }
        return selectCards(Arrays.asList(cardIds));
    }

    public static CombatApiResult selectCards(List<String> cardIds) {
        try {
            List<String> safeCardIds = cardIds == null
                ? null
                : new ArrayList<String>(cardIds);
            return CombatSelectionAdapter.selectCards(safeCardIds);
        } catch (RuntimeException exception) {
            LOGGER.error("Unable to execute select_cards.", exception);
            return CombatApiResult.error("internal_error", "select_cards failed unexpectedly.");
        }
    }

    private static CombatApiResult validatePlayerCommandReady() {
        if (!isCombatRoom() || AbstractDungeon.player == null || AbstractDungeon.actionManager == null) {
            return CombatApiResult.error("not_in_combat", "Player is not in an active combat room.");
        }
        if (AbstractDungeon.player.isDead || AbstractDungeon.player.isDying) {
            return CombatApiResult.error("player_dead", "Player cannot act after death.");
        }
        if (AbstractDungeon.isScreenUp || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.NONE) {
            return CombatApiResult.error("game_busy", "A game screen is currently waiting for input.");
        }

        GameActionManager manager = AbstractDungeon.actionManager;
        if (manager.phase != GameActionManager.Phase.WAITING_ON_USER
            || manager.currentAction != null
            || !manager.actions.isEmpty()
            || !manager.preTurnActions.isEmpty()
            || !manager.cardQueue.isEmpty()
            || manager.turnHasEnded
            || AbstractDungeon.player.isEndingTurn
            || AbstractDungeon.player.endTurnQueued) {
            return CombatApiResult.error("game_busy", "Combat actions are still resolving.");
        }
        return null;
    }

    private static boolean isCombatRoom() {
        return AbstractDungeon.getCurrRoom() != null
            && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
    }

    private static AbstractCard findCardInHand(String cardId) {
        for (AbstractCard card : AbstractDungeon.player.hand.group) {
            if (cardId.equals(CombatIdentifiers.cardId(card))) {
                return card;
            }
        }
        return null;
    }

    private static boolean requiresTarget(AbstractCard card) {
        return card.target == AbstractCard.CardTarget.ENEMY
            || card.target == AbstractCard.CardTarget.SELF_AND_ENEMY;
    }

    private static TargetResult requireActiveTarget(String targetId) {
        if (targetId == null || targetId.trim().isEmpty()) {
            return TargetResult.error(CombatApiResult.error(
                "target_required",
                "target_id is required for this card or potion."
            ));
        }

        CombatIdentifiers.TargetResolution resolution = CombatIdentifiers.resolveTarget(targetId);
        if (resolution.isAmbiguous()) {
            return TargetResult.error(CombatApiResult.error(
                "ambiguous_target",
                "Monster's original id is not unique; use monster:<index>."
            ));
        }
        AbstractMonster monster = resolution.getMonster();
        if (monster == null) {
            return TargetResult.error(CombatApiResult.error(
                "target_not_found",
                "Target is not present in the current combat."
            ));
        }
        if (monster.isDeadOrEscaped() || monster.isDying || monster.isEscaping
            || monster.currentHealth <= 0) {
            return TargetResult.error(CombatApiResult.error(
                "invalid_target",
                "Target is no longer alive and targetable."
            ));
        }
        return TargetResult.success(monster);
    }

    private static final class TargetResult {
        private final AbstractMonster monster;
        private final CombatApiResult error;

        private TargetResult(AbstractMonster monster, CombatApiResult error) {
            this.monster = monster;
            this.error = error;
        }

        static TargetResult success(AbstractMonster monster) {
            return new TargetResult(monster, null);
        }

        static TargetResult error(CombatApiResult error) {
            return new TargetResult(null, error);
        }
    }
}
