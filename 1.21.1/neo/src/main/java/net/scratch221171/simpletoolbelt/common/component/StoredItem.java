package net.scratch221171.simpletoolbelt.common.component;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public record StoredItem(List<StackGroup> stackGroups) {

    public static final StoredItem DEFAULT = new StoredItem(List.of(StackGroup.EMPTY));

    public record StackGroup(List<ItemStack> stacks) {
        public static final Codec<StackGroup> CODEC =
                ItemStack.CODEC.listOf(8, 8).xmap(StackGroup::new, StackGroup::stacks);

        public ItemStack getStack(int index) {
            return stacks.get(index);
        }

        public static StackGroup EMPTY = new StackGroup(Collections.nCopies(8, ItemStack.EMPTY));
    }

    public static final Codec<StoredItem> CODEC =
            StackGroup.CODEC.listOf().xmap(StoredItem::new, StoredItem::stackGroups);

    public StackGroup getEntry(int index) {
        return stackGroups.get(index);
    }
}
