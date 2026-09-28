package net.scratch221171.simpletoolbelt.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.STUtils;
import net.scratch221171.simpletoolbelt.client.STClientEvents;
import net.scratch221171.simpletoolbelt.client.STKeyMappings;
import net.scratch221171.simpletoolbelt.common.component.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.network.SelectBeltSlotPayload;
import org.jspecify.annotations.NonNull;

public class ToolbeltWheelScreen extends Screen {

    private static final float WHEEL_RADIUS = 70f;
    private static final float INNER_DEADZONE_RADIUS = 30f;
    private static final float OUTER_STOW_ZONE_RADIUS = 150f;

    private static final int ICON_SIZE = 16;

    private static final ResourceLocation SELECTED_FRAME_SPRITE = STUtils.id("textures/hud/wheel_selected_frame.png");
    private static final ResourceLocation FRAME_SPRITE = STUtils.id("textures/hud/wheel_frame.png");
    private static final ResourceLocation EMPTY_SPRITE = STUtils.id("textures/hud/wheel_empty.png");

    private HoverState hoverState = HoverState.noAction();

    public ToolbeltWheelScreen() {
        super(Component.translatable(Const.LangKey.WHEEL_SCREEN));
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
            this.onClose();
            return;
        }

        //        renderMenuBackground(guiGraphics);
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        ToolbeltContents stored = ToolbeltItem.getContent(belt);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float stowThreshold = Math.min(OUTER_STOW_ZONE_RADIUS, Math.min(this.width, this.height) * 0.45f);

        if (distance > stowThreshold) {
            hoverState = HoverState.stow();
        } else if (distance < INNER_DEADZONE_RADIUS) {
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
            boolean isSelected = hoverState.action() == HoverState.HoverAction.SELECT && i == hoverState.index();
            ItemStack initialStack = stored.ring().initial().getStack(i);
            ItemStack currentStack = stored.ring().current().getStack(i);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (currentStack.isEmpty()) {
                if (initialStack.isEmpty()) {
                    guiGraphics.blit(
                            EMPTY_SPRITE, x - (10 - ICON_SIZE) / 2, y - (10 - ICON_SIZE) / 2, 0, 0, 10, 10, 10, 10);
                } else {
                    guiGraphics.blit(
                            FRAME_SPRITE, x - (22 - ICON_SIZE) / 2, y - (22 - ICON_SIZE) / 2, 0, 0, 22, 22, 22, 22);
                    guiGraphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, 0x80202020);
                    guiGraphics.renderItem(initialStack, x, y);
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
                    guiGraphics.drawString(font, "0", x + 17 - font.width("0"), y + 9, 16777215, true);
                    guiGraphics.pose().popPose();
                }
            } else {
                guiGraphics.blit(
                        FRAME_SPRITE, x - (22 - ICON_SIZE) / 2, y - (22 - ICON_SIZE) / 2, 0, 0, 22, 22, 22, 22);
                if (isSelected) {
                    guiGraphics.blit(
                            SELECTED_FRAME_SPRITE,
                            x - (24 - ICON_SIZE) / 2,
                            y - (24 - ICON_SIZE) / 2,
                            0,
                            0,
                            24,
                            24,
                            24,
                            24);
                }
                guiGraphics.renderItem(currentStack, x, y);
                guiGraphics.renderItemDecorations(this.font, currentStack, x, y);
                if (isSelected) {
                    guiGraphics.renderTooltip(this.font, currentStack, mouseX, mouseY);
                }
            }
            RenderSystem.disableBlend();
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
