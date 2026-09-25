package net.scratch221171.simpletoolbelt.common.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import org.jspecify.annotations.NonNull;

public class ToolbeltSlot extends Slot {
    public ToolbeltSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(@NonNull ItemStack stack) {
        return super.mayPlace(stack) && !stack.is(STItems.TOOLBELT.get());
    }
}
