package net.scratch221171.simpletoolbelt.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.client.STClientEvents;
import net.scratch221171.simpletoolbelt.client.STKeyMappings;
import net.scratch221171.simpletoolbelt.client.ToolbeltWheelState;
import net.scratch221171.simpletoolbelt.common.component.StoredItem;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.network.SelectToolbeltSlotPayload;

/**
 * Client-only "screen" that appears while R is held. Deliberately NOT tied to a menu/container —
 * it just reads the belt's current contents off the client player's own (already-synced)
 * inventory stack and renders them. All actual mutation happens server-side once a selection or
 * stow is sent.
 *
 * KNOWN LIMITATION: because this is a Screen, vanilla will not process movement (WASD) input while
 * it's open — the player is frozen in place for the duration of the hold. Good enough for a first
 * working pass; smoother "look/move while the wheel is open" behavior would need a Mixin-based
 * approach (e.g. intercepting MouseHandler) instead of a Screen, which is a reasonable follow-up.
 */
public class ToolbeltWheelScreen extends Screen {

    private static final float WHEEL_RADIUS = 70f;
    private static final int ICON_SIZE = 16;

    private static final float INNER_DEADZONE = 12f; // Createの5px相当より少し広め
    private static final int PENDING_NONE = Integer.MIN_VALUE; // 中心デッドゾーン用の特別値

    private int hoveredSlot = PENDING_NONE;

    public ToolbeltWheelScreen() {
        super(Component.translatable("gui.simpletoolbelt.wheel"));
        ToolbeltWheelState.setOpen(true);
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
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
        StoredItem stored = ToolbeltItem.getStored(belt);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float stowThreshold = Math.min(this.width, this.height) * 0.45f;

        if (distance > stowThreshold) {
            hoveredSlot = StoredItem.NO_ACTIVE_SLOT;
        } else if (distance < INNER_DEADZONE) {
            hoveredSlot = PENDING_NONE;
        } else {
            double angleDeg = Math.toDegrees(Math.atan2(dx, -dy));
            if (angleDeg < 0) angleDeg += 360;
            hoveredSlot = Math.floorMod(Math.round((float) angleDeg / 45f), StoredItem.RING_SIZE);
        }

        ToolbeltWheelState.setHoveredSlot(hoveredSlot);

        for (int i = 0; i < StoredItem.RING_SIZE; i++) {
            double angleRad = Math.toRadians(i * 45.0);
            int x = centerX + (int) (WHEEL_RADIUS * Math.sin(angleRad)) - ICON_SIZE / 2;
            int y = centerY - (int) (WHEEL_RADIUS * Math.cos(angleRad)) - ICON_SIZE / 2;

            boolean isHovered = i == hoveredSlot;
            int bgColor = isHovered ? 0xAAFFFFFF : 0x55000000;
            guiGraphics.fill(x - 2, y - 2, x + ICON_SIZE + 2, y + ICON_SIZE + 2, bgColor);

            ItemStack slotStack = stored.getStack(i);
            if (!slotStack.isEmpty()) {
                guiGraphics.renderItem(slotStack, x, y);
                guiGraphics.renderItemDecorations(this.font, slotStack, x, y);
            }
        }

        // simple center indicator for the "stow" state
        int centerColor = hoveredSlot == StoredItem.NO_ACTIVE_SLOT ? 0xAAFFFFFF : 0x55FFFFFF;
        guiGraphics.fill(centerX - 3, centerY - 3, centerX + 3, centerY + 3, centerColor);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (STKeyMappings.OPEN_WHEEL.matches(keyCode, scanCode)) {
            if (hoveredSlot != PENDING_NONE) {
                PacketDistributor.sendToServer(new SelectToolbeltSlotPayload(hoveredSlot));
            }
            this.onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        ToolbeltWheelState.setOpen(false);
        super.onClose();
    }
}
