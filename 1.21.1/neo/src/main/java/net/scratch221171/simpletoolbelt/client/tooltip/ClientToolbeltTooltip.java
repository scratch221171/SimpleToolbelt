package net.scratch221171.simpletoolbelt.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import org.jspecify.annotations.NonNull;

public class ClientToolbeltTooltip implements ClientTooltipComponent {

    private static final int COLUMNS = ToolbeltContents.PAGE_SIZE;
    private static final int SLOT_SIZE = 18;
    private final int rows;
    private final int totalSlots;

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");

    private final ToolbeltContents contents;

    public ClientToolbeltTooltip(ToolbeltTooltip tooltip) {
        this.contents = tooltip.contents();
        this.rows = contents.pagesSize();
        this.totalSlots = contents.totalSlots();
    }

    @Override
    public int getHeight() {
        return rows * SLOT_SIZE + 2;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return COLUMNS * SLOT_SIZE;
    }

    @Override
    public void renderImage(@NonNull Font font, int x, int y, @NonNull GuiGraphics guiGraphics) {
        for (int i = 0; i < totalSlots; i++) {
            int sx = x + (i % COLUMNS) * SLOT_SIZE;
            int sy = y + (i / COLUMNS) * SLOT_SIZE;
            guiGraphics.blitSprite(SLOT_SPRITE, sx, sy, 0, 18, 18);
            ItemStack init = contents.getInitialFlat(i);
            ItemStack cur = contents.getCurrentFlat(i);
            if (!cur.isEmpty()) {
                guiGraphics.renderItem(cur, sx + 1, sy + 1);
                guiGraphics.renderItemDecorations(font, cur, sx + 1, sy + 1);
            } else if (!init.isEmpty()) {
                guiGraphics.renderItem(init, sx + 1, sy + 1);
                guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0x80202020);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
                guiGraphics.drawString(font, "0", sx + 18 - font.width("0"), sy + 10, 0xFFFFFF, true);
                guiGraphics.pose().popPose();
            }
        }
    }
}
