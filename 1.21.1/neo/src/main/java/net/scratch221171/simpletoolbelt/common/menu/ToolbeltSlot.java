package net.scratch221171.simpletoolbelt.common.menu;

import java.util.function.BooleanSupplier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.STUtils;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import org.jspecify.annotations.NonNull;

public class ToolbeltSlot extends Slot {
    private final ItemStack initial;
    private final BooleanSupplier quickMoveContext;

    public ToolbeltSlot(
            Container container, int index, int x, int y, ItemStack initial, BooleanSupplier quickMoveContext) {
        super(container, index, x, y);
        this.initial = initial;
        this.quickMoveContext = quickMoveContext;
    }

    @Override
    public boolean mayPlace(@NonNull ItemStack stack) {
        if (!super.mayPlace(stack) || stack.is(STItems.TOOLBELT.get())) {
            return false;
        }
        // quickMoveでは、前にアイテムが入っていた場所はスキップさせる
        if (quickMoveContext.getAsBoolean() && initial != ItemStack.EMPTY) {
            return STUtils.isSame(stack, initial);
        }
        return true;
    }
}
