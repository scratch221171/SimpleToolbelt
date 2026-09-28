package net.scratch221171.simpletoolbelt.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.component.ToolbeltContents;
import org.jspecify.annotations.NonNull;

public class ClientToolbeltTooltip implements ClientTooltipComponent {
    private static final int COLUMNS = 8;
    private static final int SLOT_SIZE = 18;
    private static final int ROWS = ToolbeltContents.RING_SIZE / COLUMNS;

    private final ToolbeltContents contents;

    public ClientToolbeltTooltip(ToolbeltTooltip tooltip) {
        this.contents = tooltip.contents();
    }

    @Override
    public int getHeight() {
        return ROWS * SLOT_SIZE + 2;
    }

    @Override
    public int getWidth(@NonNull Font font) {
        return COLUMNS * SLOT_SIZE;
    }

    @Override
    public void renderImage(@NonNull Font font, int x, int y, @NonNull GuiGraphics guiGraphics) {
        for (int i = 0; i < ToolbeltContents.RING_SIZE; i++) {
            int sx = x + (i % COLUMNS) * SLOT_SIZE;
            int sy = y + (i / COLUMNS) * SLOT_SIZE;
            guiGraphics.fill(sx, sy, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0x80000000);

            ItemStack current = contents.ring().current().getStack(i);
            ItemStack initial = contents.ring().initial().getStack(i);

            if (!current.isEmpty()) {
                guiGraphics.renderItem(current, sx + 1, sy + 1);
                guiGraphics.renderItemDecorations(font, current, sx + 1, sy + 1);
            } else if (!initial.isEmpty()) {
                guiGraphics.renderItem(initial, sx + 1, sy + 1);
                guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0x80202020);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
                guiGraphics.drawString(font, "0", sx + 18 - font.width("0"), sy + 10, 0xFFFFFF, true);
                guiGraphics.pose().popPose();
            }
        }
    }
}
