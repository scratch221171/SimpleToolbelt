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
import net.scratch221171.simpletoolbelt.client.network.ClientBeltCache;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.network.SelectBeltSlotPayload;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import org.jspecify.annotations.NonNull;

public class ToolbeltScreen extends AbstractContainerScreen<ToolbeltMenu> {

    private static final int BASE_HEIGHT = 133;

    private static final ResourceLocation TEXTURE = STUtils.id("textures/gui/toolbelt/toolbelt.png");
    private static final ResourceLocation BUTTON = STUtils.id("textures/gui/toolbelt/button/button.png");
    private static final ResourceLocation BUTTON_HIGHLIGHTED =
            STUtils.id("textures/gui/toolbelt/button/button_highlighted.png");
    private static final ResourceLocation BUTTON_SELECTED =
            STUtils.id("textures/gui/toolbelt/button/button_selected.png");
    private static final ResourceLocation STOW = STUtils.id("textures/gui/toolbelt/button/stow.png");

    public ToolbeltScreen(ToolbeltMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = BASE_HEIGHT + (menu.getPages() - 1) * 18;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        // ToolbeltMenuのToolbeltSlotとの辻褄あわせ
        int x = leftPos + (8 + ToolbeltContents.PAGE_SIZE * 18);
        int y = topPos + (20);
        addBeltButton(
                new BeltStowButton(x, y, 16, 16, Component.translatable(Const.LangKey.Screen.WHEEL_TOOLTIP_STOW)));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int rows = menu.getPages();
        int x = leftPos, y = topPos;
        // 上部（ベルト1行目より上
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, 19);
        // ベルトのスロット行をページ数分だけ繰り返す
        for (int r = 0; r < rows; r++) {
            guiGraphics.blit(TEXTURE, x, y + 19 + r * 18, 0, 19, imageWidth, 18);
        }
        // 下部（インベントリ側
        guiGraphics.blit(TEXTURE, x, y + 19 + rows * 18, 0, 37, imageWidth, BASE_HEIGHT - 37);

        ClientBeltCache.read(menu.getBeltId()).ifPresent(cached -> {
            Const.LOGGER.info("menuSlots={}, cacheSlots={}", menu.getTotalSlots(), cached.totalSlots());
            for (int i = 0; i < menu.getTotalSlots(); i++) {
                ItemStack init = cached.getInitialFlat(i);
                ItemStack cur = cached.getCurrentFlat(i);
                Slot slot = menu.getSlot(i);
                if (cur.isEmpty() && !init.isEmpty()) {
                    int itemX = leftPos + slot.x;
                    int itemY = topPos + slot.y;
                    guiGraphics.fill(itemX, itemY, itemX + 16, itemY + 16, 0x80202020);
                    guiGraphics.renderItem(init, itemX, itemY);
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
                    guiGraphics.drawString(font, "0", itemX + 17 - font.width("0"), itemY + 9, 16777215, true);
                    guiGraphics.pose().popPose();
                }
            }
        });
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private <T extends AbstractWidget & BeltButton> void addBeltButton(T beltButton) {
        addRenderableWidget(beltButton);
    }

    interface BeltButton {}

    static class BeltStowButton extends AbstractButton implements BeltButton {
        private boolean isPressed = false;

        public BeltStowButton(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
            setTooltip(Tooltip.create(message));
        }

        @Override
        public void onPress() {
            ClientBeltCache.clear();
            PacketDistributor.sendToServer(new SelectBeltSlotPayload(SelectBeltSlotPayload.STOW_INDEX));
            isPressed = true;
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            isPressed = false;
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {
            defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public void renderWidget(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation resourcelocation;
            if (isPressed) {
                resourcelocation = BUTTON_SELECTED;
            } else if (isHovered()) {
                resourcelocation = BUTTON_HIGHLIGHTED;
            } else {
                resourcelocation = BUTTON;
            }

            guiGraphics.blit(resourcelocation, getX(), getY(), 0, 0, width, height, width, height);
            guiGraphics.blit(STOW, getX() + 2, getY() + 2, 0, 0, 12, 12, 12, 12);
        }
    }
}
