package net.scratch221171.simpletoolbelt.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class GhostItemRenderer {
    private GhostItemRenderer() {}

    public static void render(GuiGraphics g, Font font, ItemStack stack, int x, int y) {
        g.fill(x, y, x + 16, y + 16, 0x80202020);
        g.renderItem(stack, x, y);
        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, 200.0F);
        g.drawString(font, "0", x + 17 - font.width("0"), y + 9, 16777215, true);
        g.pose().popPose();
    }
}
