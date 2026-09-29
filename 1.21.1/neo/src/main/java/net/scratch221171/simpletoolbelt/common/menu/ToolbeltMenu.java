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
import net.scratch221171.simpletoolbelt.STUtils;
import net.scratch221171.simpletoolbelt.common.registry.STMenus;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;
import org.jspecify.annotations.NonNull;

public class ToolbeltMenu extends AbstractContainerMenu {

    public static final int BELT_SLOTS = ToolbeltContents.RING_SIZE;

    private final Container beltContainer;
    private final UUID beltId;
    private final Player player;
    private boolean suppressWriteBack = false;

    /** Client-side constructor */
    public ToolbeltMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(id, inv, buf.readUUID(), ToolbeltContents.STREAM_CODEC.decode(buf), false);
    }

    /** Server-side constructor */
    public static ToolbeltMenu forUUID(int containerId, Inventory inv, UUID beltId) {
        ToolbeltContents contents =
                ToolbeltStorage.get(((ServerPlayer) inv.player).server).get(beltId);
        return new ToolbeltMenu(containerId, inv, beltId, contents, true);
    }

    private ToolbeltMenu(int containerId, Inventory inv, UUID beltId, ToolbeltContents contents, boolean isServer) {
        super(STMenus.TOOLBELT.get(), containerId);
        this.beltId = beltId;
        this.player = inv.player;

        SimpleContainer container = new SimpleContainer(BELT_SLOTS);
        for (int i = 0; i < BELT_SLOTS; i++) {
            container.setItem(i, contents.ring().current().getStack(i).copy());
        }
        if (isServer) {
            container.addListener(this::writeBack);
            container.addListener(c -> {
                if (!suppressWriteBack) {
                    writeBack(c);
                }
            });
        }

        checkContainerSize(container, BELT_SLOTS);
        this.beltContainer = container;
        beltContainer.startOpen(inv.player);
        for (int i = 0; i < BELT_SLOTS; i++) {
            final int idx = i;
            Supplier<ItemStack> initial = isServer
                    ? () -> ToolbeltStorage.get(((ServerPlayer) player).server)
                            .get(beltId)
                            .ring()
                            .initial()
                            .getStack(idx)
                    : () -> contents.ring().initial().getStack(idx);
            this.addSlot(new ToolbeltSlot(container, i, 8 + i * 18, 20, initial));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 109));
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

        if (index < BELT_SLOTS) {
            if (!this.moveItemStackTo(original, BELT_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!(this.moveItemStackTo(original, 0, BELT_SLOTS, false))) {
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
        if (player instanceof ServerPlayer serverPlayer
                && (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE)
                && slotId >= 0
                && slotId < BELT_SLOTS
                && getCarried().isEmpty()) {
            ToolbeltContents stored = ToolbeltStorage.get(serverPlayer.server).get(beltId);
            ToolbeltContents.StackGroup init = stored.ring().initial();
            ToolbeltContents.StackGroup cur = stored.ring().current();
            if (!init.getStack(slotId).isEmpty() && cur.getStack(slotId).isEmpty()) {
                ToolbeltStorage.update(
                        serverPlayer,
                        beltId,
                        new ToolbeltContents(new ToolbeltContents.Ring(init.withStack(slotId, ItemStack.EMPTY), cur)));
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        beltContainer.stopOpen(player);
    }

    public UUID getBeltId() {
        return beltId;
    }

    public static void refreshIfOpen(ServerPlayer player, UUID beltId, ToolbeltContents contents) {
        if (player.containerMenu instanceof ToolbeltMenu menu && menu.beltId.equals(beltId)) {
            menu.applyExternalUpdate(contents);
        }
    }

    private void applyExternalUpdate(ToolbeltContents contents) {
        suppressWriteBack = true;
        try {
            for (int i = 0; i < BELT_SLOTS; i++) {
                beltContainer.setItem(i, contents.ring().current().getStack(i).copy());
            }
        } finally {
            suppressWriteBack = false;
        }
    }

    private void writeBack(Container container) {
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ToolbeltContents stored = ToolbeltStorage.get(serverPlayer.server).get(beltId);
        ToolbeltContents.StackGroup init = stored.ring().initial();
        ToolbeltContents.StackGroup cur = stored.ring().current();

        for (int i = 0; i < BELT_SLOTS; i++) {
            ItemStack now = container.getItem(i);
            if (ItemStack.matches(now, cur.getStack(i))) {
                continue; // 変化なし -> 無視
            }
            cur = cur.withStack(i, now.copy());
            if (!now.isEmpty() && !STUtils.isSame(init.getStack(i), now)) {
                init = init.withStack(i, now.copy()); // 変更 -> 更新
            } // 返却 ->、無視
        }

        ToolbeltContents updated = new ToolbeltContents(new ToolbeltContents.Ring(init, cur));
        if (!stored.equals(updated)) {
            ToolbeltStorage.update(serverPlayer, beltId, updated);
        }
    }
}
