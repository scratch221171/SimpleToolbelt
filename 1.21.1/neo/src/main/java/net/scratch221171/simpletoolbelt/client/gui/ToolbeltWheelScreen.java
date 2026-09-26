package net.scratch221171.simpletoolbelt.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.client.STClientEvents;
import net.scratch221171.simpletoolbelt.client.STKeyMappings;
import net.scratch221171.simpletoolbelt.common.component.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.network.SelectBeltSlotPayload;
import org.jspecify.annotations.NonNull;

/**
 * Client-only "screen" that appears while R is held. Deliberately NOT tied to a menu/container —
 * it just reads the belt's current contents off the client player's own (already-synced)
 * inventory stack and renders them. All actual mutation happens server-side once a selection or
 * stow is sent.
 * <p>
 * KNOWN LIMITATION: because this is a Screen, vanilla will not process movement (WASD) input while
 * it's open — the player is frozen in place for the duration of the hold. Good enough for a first
 * working pass; smoother "look/move while the wheel is open" behavior would need a Mixin-based
 * approach (e.g. intercepting MouseHandler) instead of a Screen, which is a reasonable follow-up.
 */
public class ToolbeltWheelScreen extends Screen {

    private static final float WHEEL_RADIUS = 70f;
    private static final int ICON_SIZE = 16;

    private static final float INNER_DEADZONE = 20f; // Createの5px相当より少し広め

    private HoverState hoverState = HoverState.noAction();

    public ToolbeltWheelScreen() {
        super(Component.translatable("gui.simpletoolbelt.wheel"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        // no widgets — everything is drawn manually in render()
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = this.minecraft;
        if (mc == null) {
            return;
        }
        ItemStack belt = STClientEvents.findToolbeltInInventory(mc);
        if (belt.isEmpty()) {
            // belt vanished mid-hold (dropped, etc.) — bail out quietly
            this.onClose();
            return;
        }
        ToolbeltContents stored = ToolbeltItem.getContent(belt);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float stowThreshold = Math.min(this.width, this.height) * 0.45f;

        if (distance > stowThreshold) {
            hoverState = HoverState.stow();
        } else if (distance < INNER_DEADZONE) {
            hoverState = HoverState.noAction();
        } else {
            double angleDeg = Math.toDegrees(Math.atan2(dx, -dy));
            if (angleDeg < 0) angleDeg += 360;
            hoverState =
                    HoverState.select(Math.floorMod(Math.round((float) angleDeg / 45f), ToolbeltContents.RING_SIZE));
        }

        for (int i = 0; i < ToolbeltContents.RING_SIZE; i++) {
            double angleRad = Math.toRadians(i * 45.0);
            int x = centerX + (int) (WHEEL_RADIUS * Math.sin(angleRad)) - ICON_SIZE / 2;
            int y = centerY - (int) (WHEEL_RADIUS * Math.cos(angleRad)) - ICON_SIZE / 2;

            boolean isHovered = hoverState.action() == HoverState.HoverAction.SELECT && i == hoverState.index();
            int bgColor = isHovered ? 0xAAFFFFFF : 0x55000000;
            int margin = isHovered ? 6 : 4;
            guiGraphics.fill(x - margin, y - margin, x + ICON_SIZE + margin, y + ICON_SIZE + margin, bgColor);

            ItemStack slotStack = stored.ring().current().getStack(i);
            if (!slotStack.isEmpty()) {
                guiGraphics.renderItem(slotStack, x, y);
                guiGraphics.renderItemDecorations(this.font, slotStack, x, y);
            }
        }

        int centerColor = hoverState.action() == HoverState.HoverAction.STOW ? 0xAAFFFFFF : 0x55FFFFFF;
        // TODO: 外周を黒く塗りつぶしたい

        //        guiGraphics.fill(centerX - 3, centerY - 3, centerX + 3, centerY + 3, centerColor);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (STKeyMappings.OPEN_WHEEL.matches(keyCode, scanCode)) {
            if (hoverState.action() == HoverState.HoverAction.SELECT) {
                PacketDistributor.sendToServer(new SelectBeltSlotPayload(hoverState.index()));
            } else if (hoverState.action() == HoverState.HoverAction.STOW) {
                PacketDistributor.sendToServer(new SelectBeltSlotPayload(SelectBeltSlotPayload.STOW_INDEX));
            }
            this.onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public static final class HoverState {

        private final HoverAction action;
        private final int index;

        private HoverState(HoverAction action, int index) {
            this.action = action;
            this.index = index;
        }

        public static HoverState noAction() {
            return new HoverState(HoverAction.NO_ACTION, -1);
        }

        public static HoverState select(int index) {
            return new HoverState(HoverAction.SELECT, index);
        }

        public static HoverState stow() {
            return new HoverState(HoverAction.STOW, -1);
        }

        public HoverAction action() {
            return action;
        }

        public int index() {
            if (action != HoverAction.SELECT) {
                throw new IllegalStateException("Index is only available for SELECT!");
            }

            return index;
        }

        public enum HoverAction {
            NO_ACTION,
            SELECT,
            STOW
        }
    }
}
