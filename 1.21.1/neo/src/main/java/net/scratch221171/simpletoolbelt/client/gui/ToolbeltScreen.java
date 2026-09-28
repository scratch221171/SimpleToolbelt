package net.scratch221171.simpletoolbelt.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.STUtils;
import net.scratch221171.simpletoolbelt.common.component.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import org.jspecify.annotations.NonNull;

public class ToolbeltScreen extends AbstractContainerScreen<ToolbeltMenu> {

    private static final ResourceLocation TEXTURE = STUtils.id("textures/gui/toolbelt.png");

    public ToolbeltScreen(ToolbeltMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.translatable(Const.LangKey.TOOLBELT_SCREEN));
        this.imageWidth = 176;
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        ToolbeltContents original = ToolbeltItem.getContent(this.menu.getBeltStack());
        for (int i = 0; i < ToolbeltMenu.BELT_SLOTS; i++) {
            ItemStack initialStack = original.ring().initial().getStack(i);
            Slot slot = this.menu.slots.get(i);
            if (!slot.hasItem() && !initialStack.isEmpty()) {
                int itemX = x + slot.x;
                int itemY = y + slot.y;
                guiGraphics.fill(itemX, itemY, itemX + 16, itemY + 16, 0x80202020);
                guiGraphics.renderItem(initialStack, itemX, itemY);
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
}
