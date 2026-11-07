package net.scratch221171.simpletoolbelt.common.menu;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.registry.STMenus;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;
import org.jspecify.annotations.NonNull;

public class ToolbeltMenu extends AbstractContainerMenu {

    private final Container beltContainer;
    private final UUID beltId;
    private final int pageSize;
    private final Player player;
    private ToolbeltContents clientContents;
    private final int totalSize;
    private boolean suppressWriteBack = false;

    /** Client-side constructor */
    public ToolbeltMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(id, inv, buf.readUUID(), buf.readInt(), ToolbeltContents.STREAM_CODEC.decode(buf), false);
    }

    /** Server-side constructor */
    public static ToolbeltMenu forUUID(int containerId, Inventory inv, UUID beltId, ItemStack stack) {
        ServerPlayer player = (ServerPlayer) inv.player;
        ToolbeltContents contents = ToolbeltStorage.get(player.server).resolve(player, beltId, stack);
        return new ToolbeltMenu(containerId, inv, beltId, ToolbeltItem.getPageSize(stack), contents, true);
    }

    private ToolbeltMenu(
            int containerId, Inventory inv, UUID beltId, int pageSize, ToolbeltContents contents, boolean isServer) {
        super(STMenus.TOOLBELT.get(), containerId);
        this.beltId = beltId;
        this.pageSize = pageSize;
        this.player = inv.player;
        this.totalSize = contents.totalSize();
        this.clientContents = contents;

        SimpleContainer container = new SimpleContainer(totalSize);
        for (int i = 0; i < totalSize; i++) {
            container.setItem(i, contents.getCurrent(i).copy());
        }
        if (isServer) {
            container.addListener(c -> {
                if (!suppressWriteBack) {
                    writeBack(c);
                }
            });
        }

        this.beltContainer = container;
        for (int i = 0; i < totalSize; i++) {
            final int index = i;
            Supplier<ItemStack> initial = isServer
                    ? () -> ToolbeltStorage.get(((ServerPlayer) player).server)
                            .getOrInit(beltId, totalSize)
                            .getInitial(index)
                    : () -> clientContents.getInitial(index);
            this.addSlot(new ToolbeltSlot(container, i, 8 + i % pageSize * 18, 20 + i / pageSize * 18, initial));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(
                        new Slot(inv, col + row * 9 + 9, 8 + col * 18, 51 + (row + totalSize / pageSize - 1) * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 109 + (totalSize / pageSize - 1) * 18));
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

        if (index < totalSize) {
            if (!this.moveItemStackTo(original, totalSize, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!(this.moveItemStackTo(original, 0, totalSize, false))) {
            return ItemStack.EMPTY;
        }

        if (original.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        if (original.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public void clicked(int slotId, int button, @NonNull ClickType clickType, @NonNull Player player) {

        if (player instanceof ServerPlayer serverPlayer) {
            applyExternalUpdate(ToolbeltStorage.get(serverPlayer.server).getOrInit(beltId, totalSize));
            if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE)
                    && slotId >= 0
                    && slotId < totalSize
                    && getCarried().isEmpty()) {
                // applyExternalUpdateで更新済みの可能性
                ToolbeltContents stored =
                        ToolbeltStorage.get(serverPlayer.server).getOrInit(beltId, totalSize);
                if (!stored.getInitial(slotId).isEmpty()
                        && stored.getCurrent(slotId).isEmpty()) {
                    ToolbeltStorage.update(serverPlayer, beltId, stored.withInitial(slotId, ItemStack.EMPTY));
                    return;
                }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }

    @Override
    public void broadcastChanges() {
        if (player instanceof ServerPlayer serverPlayer) {
            applyExternalUpdate(ToolbeltStorage.get(serverPlayer.server).getOrInit(beltId, totalSize));
        }
        super.broadcastChanges();
    }

    public UUID getBeltId() {
        return beltId;
    }

    public int getTotalPages() {
        return totalSize / pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public ToolbeltContents getClientContents() {
        return clientContents;
    }

    public void applyClientSync(ToolbeltContents contents) {
        if (contents.totalSize() >= totalSize) {
            this.clientContents = contents;
        }
    }

    public static void refreshIfOpen(ServerPlayer player, UUID beltId, ToolbeltContents contents) {
        if (player.containerMenu instanceof ToolbeltMenu menu && menu.beltId.equals(beltId)) {
            menu.applyExternalUpdate(contents);
        }
    }

    private void applyExternalUpdate(ToolbeltContents contents) {
        suppressWriteBack = true;
        try {
            for (int i = 0; i < totalSize; i++) {
                ItemStack fresh = contents.getCurrent(i);
                if (!ItemStack.isSameItemSameComponents(beltContainer.getItem(i), fresh)) {
                    beltContainer.setItem(i, fresh.copy());
                }
            }
        } finally {
            suppressWriteBack = false;
        }
    }

    private void writeBack(Container container) {
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ToolbeltContents stored = ToolbeltStorage.get(serverPlayer.server).getOrInit(beltId, totalSize);
        for (int i = 0; i < totalSize; i++) {
            ItemStack now = container.getItem(i);
            if (ItemStack.matches(now, stored.getCurrent(i))) {
                continue; // 変化なし -> 無視
            }
            stored = stored.withCurrent(i, now.copy());
            if (!now.isEmpty() && !ItemStack.isSameItemSameComponents(stored.getInitial(i), now)) {
                stored = stored.withInitial(i, now.copy()); // 変更 -> 更新
            } // 返却 ->、無視
        }

        ToolbeltStorage.update(serverPlayer, beltId, stored);
    }
}
