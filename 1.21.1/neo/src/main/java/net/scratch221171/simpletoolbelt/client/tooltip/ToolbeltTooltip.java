package net.scratch221171.simpletoolbelt.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;

public record ToolbeltTooltip(ToolbeltContents contents, ItemStack stack) implements TooltipComponent {}
