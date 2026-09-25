package net.scratch221171.simpletoolbelt.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;

public class ToolbeltScreen extends AbstractContainerScreen<ToolbeltMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "textures/gui/toolbelt.png");

    public ToolbeltScreen(ToolbeltMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int excludedSlot = this.menu.getExcludedSlot();
        if (excludedSlot >= 0) {
            ItemStack ghost = net.scratch221171.simpletoolbelt.common.item.ToolbeltItem.getStored(
                            net.scratch221171.simpletoolbelt.client.STClientEvents.findToolbeltInInventory(
                                    this.minecraft))
                    .activeGhost();
            if (!ghost.isEmpty()) {
                Slot slot = this.menu.slots.get(excludedSlot);
                int itemX = x + slot.x;
                int itemY = y + slot.y;
                guiGraphics.renderItem(ghost, itemX, itemY);
                guiGraphics.fill(itemX, itemY, itemX + 16, itemY + 16, 0x80202020); // 半透明の暗いオーバーレイ
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
