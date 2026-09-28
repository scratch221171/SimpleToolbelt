package net.scratch221171.simpletoolbelt.common.menu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.STUtils;
import net.scratch221171.simpletoolbelt.common.component.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import net.scratch221171.simpletoolbelt.common.registry.STMenus;
import org.jspecify.annotations.NonNull;

public class ToolbeltMenu extends AbstractContainerMenu {

    public static final int BELT_SLOTS = ToolbeltContents.RING_SIZE;

    private final Container beltContainer;
    private final ItemStack beltStack;

    private boolean quickMoveContext = false;

    /** Client-side constructor */
    public ToolbeltMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(BELT_SLOTS),
                playerInventory.player.getItemInHand(extraData.readEnum(InteractionHand.class)));
    }

    /** Server-side constructor */
    public static ToolbeltMenu forHeldItem(int containerId, Inventory playerInventory, ItemStack beltStack) {
        ToolbeltContents contents = ToolbeltItem.getContent(beltStack);
        SimpleContainer container = new SimpleContainer(BELT_SLOTS);
        for (int i = 0; i < BELT_SLOTS; i++) {
            container.setItem(i, contents.ring().current().getStack(i).copy());
        }
        return new ToolbeltMenu(containerId, playerInventory, container, beltStack);
    }

    private ToolbeltMenu(int containerId, Inventory playerInventory, Container beltContainer, ItemStack beltStack) {
        super(STMenus.TOOLBELT.get(), containerId);
        this.beltContainer = beltContainer;
        this.beltStack = beltStack;
        checkContainerSize(beltContainer, BELT_SLOTS);
        beltContainer.startOpen(playerInventory.player);

        for (int i = 0; i < BELT_SLOTS; i++) {
            ItemStack initial = ToolbeltItem.getContent(beltStack)
                    .ring()
                    .initial()
                    .getStack(i)
                    .copy();
            this.addSlot(new ToolbeltSlot(beltContainer, i, 8 + i * 18, 20, initial, () -> quickMoveContext));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();

        quickMoveContext = true;
        try {
            if (index < BELT_SLOTS) {
                if (!this.moveItemStackTo(original, BELT_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!(this.moveItemStackTo(original, 0, BELT_SLOTS, false))) {
                return ItemStack.EMPTY;
            }
        } finally {
            quickMoveContext = false; // 例外時も必ず戻す
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
        return true;
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        this.beltContainer.stopOpen(player);
        if (!player.level().isClientSide) {
            if (beltStack.is(STItems.TOOLBELT.get())) {
                saveToStack(beltStack);
            }
        }
    }

    public @NonNull ItemStack getBeltStack() {
        return beltStack;
    }

    private void saveToStack(ItemStack beltStack) {

        ToolbeltContents original = ToolbeltItem.getContent(beltStack);
        // modified: GUIでcurrentが編集された内容
        // original: 編集前のinitial/current
        // -> new contents:
        // initial: currentと比べて編集された箇所が存在すれば変更、それ以外は保留
        // current: modifiedを全て反映

        ToolbeltContents.StackGroup updatedInitial = original.ring().initial();
        ToolbeltContents.StackGroup current = original.ring().current();

        List<ItemStack> modified = new ArrayList<>(BELT_SLOTS);
        for (int i = 0; i < BELT_SLOTS; i++) {
            ItemStack stack = this.beltContainer.getItem(i).copy();
            modified.add(stack);
            if (!stack.isEmpty() && !STUtils.isSame(stack, current.getStack(i))) {
                updatedInitial = updatedInitial.withStack(i, stack);
            }
        }
        ToolbeltContents.StackGroup updatedCurrent = new ToolbeltContents.StackGroup(modified);
        ToolbeltContents updated = new ToolbeltContents(new ToolbeltContents.Ring(updatedInitial, updatedCurrent));

        if (!original.equals(updated)) {
            beltStack.set(STDataComponents.TOOLBELT_CONTENTS.get(), updated);
        }
    }
}
