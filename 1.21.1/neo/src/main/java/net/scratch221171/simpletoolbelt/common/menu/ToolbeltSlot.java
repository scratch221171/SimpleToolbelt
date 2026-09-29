package net.scratch221171.simpletoolbelt.common.menu;

import java.util.function.Supplier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.STUtils;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import org.jspecify.annotations.NonNull;

public class ToolbeltSlot extends Slot {
    private final Supplier<ItemStack> initial;

    public ToolbeltSlot(Container container, int index, int x, int y, Supplier<ItemStack> initial) {
        super(container, index, x, y);
        this.initial = initial;
    }

    @Override
    public boolean mayPlace(@NonNull ItemStack stack) {
        if (!super.mayPlace(stack) || stack.is(STItems.TOOLBELT.get())) {
            return false;
        }
        if (initial.get() != ItemStack.EMPTY) {
            return STUtils.isSame(stack, initial.get());
        }
        return true;
    }
}
