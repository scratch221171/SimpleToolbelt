package net.scratch221171.simpletoolbelt.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.client.gui.GhostItemRenderer;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import org.jspecify.annotations.NonNull;

public class ClientToolbeltTooltip implements ClientTooltipComponent {

    private static final int SLOT_SIZE = 18;
    private final int rows;
    private final int columns;
    private final int totalSlots;

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");

    private final ToolbeltContents contents;

    public ClientToolbeltTooltip(ToolbeltTooltip tooltip) {
        this.contents = tooltip.contents();
        this.columns = ToolbeltItem.getPageSize(tooltip.stack());
        this.rows = contents.totalSize() / columns;
        this.totalSlots = contents.totalSize();
    }

    @Override
    public int getHeight() {
        return rows * SLOT_SIZE + 2;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return columns * SLOT_SIZE;
    }

    @Override
    public void renderImage(@NonNull Font font, int x, int y, @NonNull GuiGraphics guiGraphics) {
        for (int i = 0; i < totalSlots; i++) {
            int sx = x + (i % columns) * SLOT_SIZE;
            int sy = y + (i / columns) * SLOT_SIZE;
            guiGraphics.blitSprite(SLOT_SPRITE, sx, sy, 0, 18, 18);
            ItemStack init = contents.getInitial(i);
            ItemStack cur = contents.getCurrent(i);
            if (!cur.isEmpty()) {
                guiGraphics.renderItem(cur, sx + 1, sy + 1);
                guiGraphics.renderItemDecorations(font, cur, sx + 1, sy + 1);
            } else if (!init.isEmpty()) {
                GhostItemRenderer.render(guiGraphics, init, sx + 1, sy + 1, true);
            }
        }
    }
}
