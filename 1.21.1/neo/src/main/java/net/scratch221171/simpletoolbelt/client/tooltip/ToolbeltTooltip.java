package net.scratch221171.simpletoolbelt.client.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.scratch221171.simpletoolbelt.common.component.ToolbeltContents;

public record ToolbeltTooltip(ToolbeltContents contents) implements TooltipComponent {}
