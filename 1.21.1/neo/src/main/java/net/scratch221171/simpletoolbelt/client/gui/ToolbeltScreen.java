package net.scratch221171.simpletoolbelt.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.STUtils;
import net.scratch221171.simpletoolbelt.client.network.ClientBeltCache;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.network.SelectBeltSlotPayload;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import org.jspecify.annotations.NonNull;

public class ToolbeltScreen extends AbstractContainerScreen<ToolbeltMenu> {

    private static final ResourceLocation TEXTURE = STUtils.id("textures/gui/toolbelt/toolbelt.png");
    private static final ResourceLocation BUTTON = STUtils.id("textures/gui/toolbelt/button/button.png");
    private static final ResourceLocation BUTTON_HIGHLIGHTED =
            STUtils.id("textures/gui/toolbelt/button/button_highlighted.png");
    private static final ResourceLocation BUTTON_SELECTED =
            STUtils.id("textures/gui/toolbelt/button/button_selected.png");
    private static final ResourceLocation STOW = STUtils.id("textures/gui/toolbelt/button/stow.png");

    public ToolbeltScreen(ToolbeltMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.translatable(Const.LangKey.Screen.TOOLBELT_SCREEN_TITLE));
        this.imageWidth = 176;
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2 + (8 + ToolbeltMenu.BELT_SLOTS * 18);
        int y = (this.height - this.imageHeight) / 2 + (20);
        addBeltButton(
                new BeltStowButton(x, y, 16, 16, Component.translatable(Const.LangKey.Screen.WHEEL_TOOLTIP_STOW)));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        ToolbeltContents original = ClientBeltCache.read(this.menu.getBeltId());
        for (int i = 0; i < ToolbeltMenu.BELT_SLOTS; i++) {
            ItemStack init = original.ring().initial().getStack(i);
            ItemStack cur = original.ring().current().getStack(i);
            Slot slot = this.menu.getSlot(i);
            if (cur.isEmpty() && !init.isEmpty()) {
                int itemX = x + slot.x;
                int itemY = y + slot.y;
                guiGraphics.fill(itemX, itemY, itemX + 16, itemY + 16, 0x80202020);
                guiGraphics.renderItem(init, itemX, itemY);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
                guiGraphics.drawString(font, "0", itemX + 17 - font.width("0"), itemY + 9, 16777215, true);
                guiGraphics.pose().popPose();
            }
        }
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private <T extends AbstractWidget & BeltButton> void addBeltButton(T beltButton) {
        this.addRenderableWidget(beltButton);
    }

    interface BeltButton {}

    static class BeltStowButton extends AbstractButton implements BeltButton {
        private boolean isPressed = false;

        public BeltStowButton(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
            this.setTooltip(Tooltip.create(message));
        }

        @Override
        public void onPress() {
            ClientBeltCache.clear();
            PacketDistributor.sendToServer(new SelectBeltSlotPayload(SelectBeltSlotPayload.STOW_INDEX));
            this.isPressed = true;
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            this.isPressed = false;
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation resourcelocation;
            if (this.isPressed) {
                resourcelocation = BUTTON_SELECTED;
            } else if (this.isHovered()) {
                resourcelocation = BUTTON_HIGHLIGHTED;
            } else {
                resourcelocation = BUTTON;
            }

            guiGraphics.blit(
                    resourcelocation, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
            guiGraphics.blit(STOW, this.getX() + 2, this.getY() + 2, 0, 0, 12, 12, 12, 12);
        }
    }
}
