package reallyfastmode.api.RestApi;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import reallyfastmode.patches.access.PrivateFieldAccess;
import reallyfastmode.protocol.VanillaCatalog.VanillaRestOptionCatalog;

import java.util.List;

/** Semantic commands for the current rest room. Call on the game render thread. */
public final class RestApi {
    private RestApi() {
    }

    /**
     * Triggers the current campfire option through its vanilla useOption method.
     * Success means the option was triggered; its effects and any subsequent
     * card selection continue through the normal game update loop.
     *
     * @throws IllegalArgumentException if option is null, unknown, absent or disabled
     * @throws IllegalStateException if the current campfire is not accepting input
     */
    public static void selectOption(VanillaRestOptionCatalog option) {
        if (option == null || option == VanillaRestOptionCatalog.UNKNOWN) {
            throw new IllegalArgumentException("A concrete vanilla rest option is required.");
        }
        if (CardCrawlGame.mode != CardCrawlGame.GameMode.GAMEPLAY
            || AbstractDungeon.currMapNode == null
            || !(AbstractDungeon.getCurrRoom() instanceof RestRoom)
            || AbstractDungeon.player == null) {
            throw new IllegalStateException("Player is not in an active rest room.");
        }

        RestRoom room = (RestRoom) AbstractDungeon.getCurrRoom();
        CampfireUI campfire = room.campfireUI;
        if (campfire == null
            || room.phase != AbstractRoom.RoomPhase.INCOMPLETE
            || campfire.somethingSelected
            || CampfireUI.hidden
            || AbstractDungeon.isScreenUp
            || AbstractDungeon.screen != AbstractDungeon.CurrentScreen.NONE
            || AbstractDungeon.isFadingIn
            || AbstractDungeon.isFadingOut
            || AbstractDungeon.player.isDead
            || AbstractDungeon.player.isDying) {
            throw new IllegalStateException("The current campfire is not accepting a selection.");
        }

        // CampfireUI.update raw patch exposes the actual private buttons list.
        List<AbstractCampfireOption> buttons = PrivateFieldAccess.campfireButtons(campfire);
        AbstractCampfireOption selected = requireUsableOption(option.optionType, buttons);

        // Consume pending UI input so a mouse/touch click cannot trigger a second option.
        for (AbstractCampfireOption button : buttons) {
            button.hb.clicked = false;
            button.hb.clickStarted = false;
        }
        campfire.touchOption = null;
        campfire.confirmButton.hb.clicked = false;
        campfire.confirmButton.hb.clickStarted = false;
        campfire.confirmButton.isDisabled = true;
        campfire.confirmButton.hide();

        // Same trigger and order as AbstractCampfireOption.update (desktop/controller)
        // and CampfireUI.updateTouchscreen (touch confirmation).
        selected.useOption();
        campfire.somethingSelected = true;
    }

    static AbstractCampfireOption requireUsableOption(
        Class<? extends AbstractCampfireOption> optionType, List<AbstractCampfireOption> buttons
    ) {
        for (AbstractCampfireOption button : buttons) {
            // Match the protocol's exact vanilla type mapping, not a modded subclass.
            if (button.getClass() == optionType) {
                if (!button.usable) {
                    throw new IllegalArgumentException(
                        "Rest option is currently disabled: " + optionType.getSimpleName());
                }
                return button;
            }
        }
        throw new IllegalArgumentException(
            "Rest option is not present at this campfire: " + optionType.getSimpleName());
    }
}
