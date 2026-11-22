package net.scratch221171.simpletoolbelt.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.STUtils;

public final class GhostItemRenderer {

    private static final ResourceLocation GUI_OVERLAY = STUtils.id("textures/gui/toolbelt/overlay.png");
    private static final ResourceLocation HUD_OVERLAY = STUtils.id("textures/hud/overlay.png");

    private GhostItemRenderer() {}

    public static void render(GuiGraphics g, ItemStack stack, int x, int y, boolean isGui) {
        g.renderItem(stack, x, y);
        g.flush();

        g.pose().pushPose();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        g.blit(isGui ? GUI_OVERLAY : HUD_OVERLAY, x, y, 0, 0, 16, 16, 16, 16);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        g.pose().popPose();
    }
}
