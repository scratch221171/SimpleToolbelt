package net.scratch221171.simpletoolbelt.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * Persistent data carried by the toolbelt item itself.
 * <p>
 * stackGroups: one entry per "ring". Ring 0 is the base 8-slot ring; further rings are
 * appended when the belt is expanded. Only ring 0 is wired up by the current UI — expansion is
 * left for a later pass (see AstralEnchant Expanse-style slot-count sync bugs for cautionary tales).
 * activeSlot: the flat index (ring * RING_SIZE + slotInRing) of the item currently held in the
 * player's main hand, or -1 if the player isn't currently holding a belt-sourced item.
 */
public record StoredItem(List<StackGroup> stackGroups, int activeSlot, ItemStack activeGhost) {

    public static final int RING_SIZE = 8;
    public static final int NO_ACTIVE_SLOT = -1;

    public static final StoredItem DEFAULT = new StoredItem(List.of(StackGroup.EMPTY), NO_ACTIVE_SLOT, ItemStack.EMPTY);

    public record StackGroup(List<ItemStack> stacks) {
        public static final Codec<StackGroup> CODEC =
                ItemStack.OPTIONAL_CODEC.listOf(RING_SIZE, RING_SIZE).xmap(StackGroup::new, StackGroup::stacks);

        public static final StackGroup EMPTY = new StackGroup(Collections.nCopies(RING_SIZE, ItemStack.EMPTY));

        public ItemStack getStack(int index) {
            return stacks.get(index);
        }

        public StackGroup withStack(int index, ItemStack stack) {
            List<ItemStack> copy = new ArrayList<>(stacks);
            copy.set(index, stack);
            return new StackGroup(copy);
        }
    }

    public static final Codec<StoredItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    StackGroup.CODEC.listOf().fieldOf("stack_groups").forGetter(StoredItem::stackGroups),
                    Codec.INT.fieldOf("active_slot").forGetter(StoredItem::activeSlot),
                    ItemStack.OPTIONAL_CODEC.fieldOf("active_ghost").forGetter(StoredItem::activeGhost))
            .apply(instance, StoredItem::new));

    public int totalSlots() {
        return stackGroups.size() * RING_SIZE;
    }

    public ItemStack getStack(int flatIndex) {
        return stackGroups.get(flatIndex / RING_SIZE).getStack(flatIndex % RING_SIZE);
    }

    public StoredItem withStack(int flatIndex, ItemStack stack) {
        List<StackGroup> copy = new ArrayList<>(stackGroups);
        int ring = flatIndex / RING_SIZE;
        copy.set(ring, copy.get(ring).withStack(flatIndex % RING_SIZE, stack));
        return new StoredItem(copy, activeSlot, activeGhost);
    }

    public StoredItem withStackGroup(int ringIndex, StackGroup group) {
        List<StackGroup> copy = new ArrayList<>(stackGroups);
        copy.set(ringIndex, group);
        return new StoredItem(copy, activeSlot, activeGhost);
    }

    public StoredItem withActiveSlot(int slot, ItemStack ghost) {
        return new StoredItem(stackGroups, slot, ghost.copy());
    }
}
