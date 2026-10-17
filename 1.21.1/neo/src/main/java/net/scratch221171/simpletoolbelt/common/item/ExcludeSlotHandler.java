package net.scratch221171.simpletoolbelt.common.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jspecify.annotations.NonNull;

public class ExcludeSlotHandler implements IItemHandler {
    private final IItemHandler delegate;
    private final int excluded;

    public ExcludeSlotHandler(IItemHandler delegate, int excluded) {
        this.delegate = delegate;
        this.excluded = excluded;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    public @NonNull ItemStack getStackInSlot(int slot) {
        return slot == excluded ? ItemStack.EMPTY : delegate.getStackInSlot(slot);
    }

    @Override
    public @NonNull ItemStack insertItem(int slot, @NonNull ItemStack stack, boolean simulate) {
        return slot == excluded ? stack : delegate.insertItem(slot, stack, simulate);
    }

    @Override
    public @NonNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return slot == excluded ? ItemStack.EMPTY : delegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NonNull ItemStack stack) {
        return slot != excluded && delegate.isItemValid(slot, stack);
    }
}
