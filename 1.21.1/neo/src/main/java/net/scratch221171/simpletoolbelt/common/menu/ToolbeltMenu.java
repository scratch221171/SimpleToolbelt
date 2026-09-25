package net.scratch221171.simpletoolbelt.common.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.component.StoredItem;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import net.scratch221171.simpletoolbelt.common.registry.STMenus;
import org.jspecify.annotations.NonNull;

public class ToolbeltMenu extends AbstractContainerMenu {

    public static final int BELT_SLOTS = StoredItem.RING_SIZE;

    private final Container beltContainer;
    /** Null on the client-only construction path (from MenuType); only used server-side to write back. */
    private final Player owner;

    private final InteractionHand sourceHand;
    private final ItemStack sourceStackRef;
    private final int excludedSlot;

    /** Client-side constructor used by MenuType — content arrives via the normal slot-sync packets. */
    public ToolbeltMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(BELT_SLOTS), null, null, ItemStack.EMPTY);
    }

    /** Server-side constructor: builds the belt container from the actual held stack's data component. */
    public static ToolbeltMenu forHeldItem(
            int containerId, Inventory playerInventory, ItemStack beltStack, InteractionHand hand) {
        StoredItem stored = ToolbeltItem.getStored(beltStack);
        SimpleContainer container = new SimpleContainer(BELT_SLOTS);
        for (int i = 0; i < BELT_SLOTS; i++) {
            container.setItem(i, stored.getStack(i).copy());
        }
        // ここではaddSlotを呼ばない。コンストラクタに委譲するだけ。
        return new ToolbeltMenu(containerId, playerInventory, container, playerInventory.player, hand, beltStack);
    }

    private ToolbeltMenu(
            int containerId,
            Inventory playerInventory,
            Container beltContainer,
            Player owner,
            InteractionHand sourceHand,
            ItemStack sourceStackRef) {
        super(STMenus.TOOLBELT.get(), containerId);
        this.beltContainer = beltContainer;
        this.owner = owner;
        this.sourceHand = sourceHand;
        this.sourceStackRef = sourceStackRef;
        checkContainerSize(beltContainer, BELT_SLOTS);
        beltContainer.startOpen(playerInventory.player);

        for (int i = 0; i < BELT_SLOTS; i++) {
            this.addSlot(new ToolbeltSlot(beltContainer, i, 8 + i * 18, 20));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }

        this.excludedSlot = sourceStackRef.isEmpty()
                ? StoredItem.NO_ACTIVE_SLOT
                : ToolbeltItem.getStored(sourceStackRef).activeSlot();
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();

        if (index < BELT_SLOTS) {
            this.moveItemStackTo(original, BELT_SLOTS, this.slots.size(), true);
        } else if (excludedSlot < 0) {
            this.moveItemStackTo(original, 0, BELT_SLOTS, false);
        } else {
            this.moveItemStackTo(original, 0, excludedSlot, false);
            if (!original.isEmpty()) {
                this.moveItemStackTo(original, excludedSlot + 1, BELT_SLOTS, false);
            }
        }

        if (original.getCount() == copy.getCount()) {
            return ItemStack.EMPTY; // 何も動かなかった
        }
        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        if (this.owner == null || this.sourceHand == null) {
            return true; // クライアント側の仮コンストラクタ経路
        }
        return player.getItemInHand(this.sourceHand) == this.sourceStackRef;
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        this.beltContainer.stopOpen(player);
        if (!player.level().isClientSide && this.owner != null && this.sourceHand != null) {
            ItemStack current = this.owner.getItemInHand(this.sourceHand);
            if (current.is(STItems.TOOLBELT.get())) {
                saveToStack(current);
            }
        }
    }

    public int getExcludedSlot() {
        return this.excludedSlot;
    }

    private void saveToStack(ItemStack beltStack) {
        List<ItemStack> stacks = new ArrayList<>(BELT_SLOTS);
        for (int i = 0; i < BELT_SLOTS; i++) {
            stacks.add(this.beltContainer.getItem(i).copy());
        }
        StoredItem current = ToolbeltItem.getStored(beltStack);
        int activeSlot = current.activeSlot();
        ItemStack ghost = current.activeGhost();

        if (activeSlot != StoredItem.NO_ACTIVE_SLOT && !stacks.get(activeSlot).isEmpty()) {
            activeSlot = StoredItem.NO_ACTIVE_SLOT;
            ghost = ItemStack.EMPTY;
        }

        StoredItem updated =
                current.withStackGroup(0, new StoredItem.StackGroup(stacks)).withActiveSlot(activeSlot, ghost);
        beltStack.set(STDataComponents.STORED_ITEM.get(), updated);
    }
}
