package net.scratch221171.simpletoolbelt.common.item;

import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.ClientBeltActions;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.network.SelectBeltSlotPayload;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import net.scratch221171.simpletoolbelt.common.storage.SyncedClientSettings;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;
import net.scratch221171.simpletoolbelt.config.ServerConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ToolbeltItem extends Item {

    private static final int NORMAL_BELT_PAGE_SIZE = 4;
    private static final int NETHERITE_BELT_PAGE_SIZE = 8;

    public ToolbeltItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(
            @NonNull ItemStack stack, @NonNull Level level, @NonNull Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof ServerPlayer player && !stack.has(STDataComponents.BELT_ID.get())) {
            ToolbeltStorage.get(player.server).ensureId(player, stack);
        }
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(
            @NonNull Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack beltStack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            openMenu(
                    serverPlayer,
                    ToolbeltStorage.get(serverPlayer.server).ensureId(serverPlayer, beltStack),
                    beltStack);
        }
        return InteractionResultHolder.sidedSuccess(beltStack, level.isClientSide);
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            @NonNull ItemStack beltStack,
            @NonNull ItemStack other,
            @NonNull Slot slot,
            @NonNull ClickAction action,
            @NonNull Player player,
            @NonNull SlotAccess access) {
        if (action != ClickAction.SECONDARY || !other.isEmpty()) return false;
        if (player instanceof ServerPlayer serverPlayer) {
            openMenuNextTick(serverPlayer, beltStack);
        } else {
            ClientBeltActions.requestOpenInCreative(slot);
        }
        return true;
    }

    @Override
    public void appendHoverText(
            @NonNull ItemStack stack,
            Item.@NonNull TooltipContext context,
            @NonNull List<Component> tooltipComponents,
            @NonNull TooltipFlag tooltipFlag) {
        if (tooltipFlag.hasShiftDown()) {
            tooltipComponents.add(Component.translatable(
                            Const.LangKey.Item.SHIFT_FOR_MORE_INFO,
                            Component.literal("Shift").withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.DARK_GRAY));
            tooltipComponents.add(
                    Component.translatable(Const.LangKey.Item.TOOLBELT_HINT).withStyle(ChatFormatting.GRAY));
            int extra = stack.getOrDefault(STDataComponents.EXTRA_PAGES, 0);
            if (extra > 0) {
                tooltipComponents.add(Component.translatable(
                                Const.LangKey.Item.EXTRA_PAGE,
                                Component.literal(String.valueOf(extra)).withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.GRAY));
            }
            tooltipComponents.add(
                    Component.literal("UUID: " + stack.getOrDefault(STDataComponents.BELT_ID, "undefined"))
                            .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltipComponents.add(Component.translatable(
                            Const.LangKey.Item.SHIFT_FOR_MORE_INFO,
                            Component.literal("Shift").withStyle(ChatFormatting.GRAY))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public int getEnchantmentValue(@NonNull ItemStack stack) {
        return 10;
    }

    @Override
    public boolean isEnchantable(@NonNull ItemStack stack) {
        return true;
    }

    @Override
    public boolean hasCraftingRemainingItem(@NonNull ItemStack stack) {
        return this == STItems.NETHERITE_TOOLBELT.get();
    }

    @Override
    public @NonNull ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copyWithCount(1);
    }

    public static void openMenuNextTick(ServerPlayer player, ItemStack beltStack) {
        UUID id = ToolbeltStorage.get(player.server).ensureId(player, beltStack);
        player.server.tell(new TickTask(player.server.getTickCount(), () -> openMenu(player, id, beltStack)));
    }

    private static void openMenu(ServerPlayer player, UUID id, ItemStack stack) {
        ToolbeltContents contents = ToolbeltStorage.get(player.server).resolve(player, id, stack);
        player.openMenu(
                new SimpleMenuProvider(
                        (containerId, inv, p) -> ToolbeltMenu.forUUID(containerId, inv, id, stack),
                        Component.translatable(Const.LangKey.Screen.TOOLBELT_SCREEN_TITLE)),
                buf -> {
                    buf.writeUUID(id);
                    buf.writeInt(ToolbeltItem.getPageSize(stack));
                    ToolbeltContents.STREAM_CODEC.encode(buf, contents);
                });
    }

    public static int getRequiredSize(ItemStack stack) {
        int base = 0;
        if (stack.is(STItems.TOOLBELT)) base = ServerConfig.Item.TOOLBELT_BASE_PAGES.getAsInt();
        if (stack.is(STItems.NETHERITE_TOOLBELT)) base = ServerConfig.Item.NETHERITE_BASE_PAGES.getAsInt();
        return (base + stack.getOrDefault(STDataComponents.EXTRA_PAGES, 0)) * getPageSize(stack);
    }

    public static int getPageSize(ItemStack stack) {
        if (stack.is(STItems.TOOLBELT)) return NORMAL_BELT_PAGE_SIZE;
        if (stack.is(STItems.NETHERITE_TOOLBELT)) return NETHERITE_BELT_PAGE_SIZE;
        throw new IllegalArgumentException("Invalid belt stack!");
    }

    public static void applySelection(ServerPlayer player, UUID beltId, int requestedSlot, Component failed) {
        STUtils.findBelt(player, beltId).ifPresent(belt -> {
            ToolbeltContents stored = ToolbeltStorage.get(player.server).resolve(player, beltId, belt);

            if (requestedSlot == SelectBeltSlotPayload.STOW_INDEX) {
                stored = stowAll(player, stored);
            } else {
                if (requestedSlot < 0 || requestedSlot >= stored.totalSize()) return;

                ToolbeltContents result = selectSlot(player, stored, requestedSlot, failed);
                if (result == null) return;
                stored = result;
            }

            ToolbeltStorage.update(player, beltId, stored);
            ToolbeltMenu.refreshIfOpen(player, beltId, stored);
        });
    }

    /** 「取り出し済みだが戻していない」スロットを全てインベントリから回収する */
    private static ToolbeltContents stowAll(ServerPlayer player, ToolbeltContents stored) {
        boolean stowedAny = false;
        for (int i = 0; i < stored.totalSize(); i++) {
            if (!stored.getInitial(i).isEmpty() && stored.getCurrent(i).isEmpty()) {
                stored = findAndStow(player, stored, i);
                if (!stored.getCurrent(i).isEmpty()) stowedAny = true;
            }
        }
        if (stowedAny) playSound(player, SoundEvents.BUNDLE_INSERT);

        return stored;
    }

    /** 通常のスロット選択 */
    @Nullable private static ToolbeltContents selectSlot(
            ServerPlayer player, ToolbeltContents stored, int slot, Component failed) {
        ItemStack current = stored.getCurrent(slot);
        if (!current.isEmpty()) {
            return takeOut(player, stored, slot, current, failed);
        }

        ItemStack initial = stored.getInitial(slot);
        if (initial.isEmpty()) return null;

        return switch (SyncedClientSettings.AlreadyTakenOutSettings.get(player).getMode()) {
            case DO_NOTHING -> stored;
            case FIND_AND_STOW -> {
                stored = findAndStow(player, stored, slot);
                playSound(player, SoundEvents.BUNDLE_INSERT);
                yield stored;
            }
            case FIND_AND_EQUIP -> findAndEquip(player, stored, initial, failed);
        };
    }

    /** 取り出し */
    @Nullable private static ToolbeltContents takeOut(
            ServerPlayer player, ToolbeltContents stored, int slot, ItemStack current, Component failed) {
        if (current.getItem() instanceof MenuOpenerItem) {
            MenuOpenerItem.use((ServerLevel) player.level(), player, current);
            return stored;
        }

        int originalSelected = player.getInventory().selected;
        applyForcedHotbarSlot(player);

        ToolbeltContents next = stashMainHand(player, stored);
        if (next == null) {
            player.getInventory().selected = originalSelected;
            player.connection.send(new ClientboundSetCarriedItemPacket(originalSelected));
            player.displayClientMessage(failed, true);
            return null;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, current.copy());
        playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
        return next.withCurrent(slot, ItemStack.EMPTY);
    }

    @Nullable private static ToolbeltContents findAndEquip(
            ServerPlayer player, ToolbeltContents stored, ItemStack initial, Component failed) {
        // すでに持っているなら何もしない
        if (STUtils.isSameItem(initial, player.getMainHandItem())) return stored;

        Inventory inv = player.getInventory();
        int found = findInInventory(inv, initial);
        if (found < 0) return stored;

        // メインハンドが変わる
        applyForcedHotbarSlot(player);

        // 移動先に既に目的のアイテムがあるなら完了
        if (STUtils.isSameItem(initial, player.getMainHandItem())) return stored;

        ItemStack foundStack = inv.getItem(found);
        inv.setItem(found, ItemStack.EMPTY);

        ToolbeltContents next = stashMainHand(player, stored);
        // 理論上は起きないが念のため
        if (next == null) {
            inv.setItem(found, foundStack);
            player.displayClientMessage(failed, true);
            return null;
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, foundStack);
        playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
        return next;
    }

    /**
     * メインハンドを退避する
     * ベルトに該当するinitialがあれば戻し、なければインベントリへ挿入
     * 挿入に失敗するとnull
     */
    @Nullable private static ToolbeltContents stashMainHand(ServerPlayer player, ToolbeltContents stored) {
        ItemStack hand = player.getMainHandItem();
        if (hand.isEmpty()) return stored;

        for (int i = 0; i < stored.totalSize(); i++) {
            if (STUtils.isSameItem(stored.getInitial(i), hand)
                    && stored.getCurrent(i).isEmpty()) {
                return stored.withCurrent(i, hand.copy());
            }
        }

        Inventory inv = player.getInventory();
        IItemHandler target = new ExcludeSlotHandler(new PlayerMainInvWrapper(inv), inv.selected);
        ItemStack moving = hand.copy();
        if (!ItemHandlerHelper.insertItemStacked(target, moving, true).isEmpty()) return null;
        ItemHandlerHelper.insertItemStacked(target, moving, false);
        return stored;
    }

    private static ToolbeltContents findAndStow(Player player, ToolbeltContents stored, int slot) {
        Inventory inv = player.getInventory();
        int found = findInInventory(inv, stored.getInitial(slot));
        if (found < 0) return stored;

        ToolbeltContents next = stored.withCurrent(slot, inv.getItem(found));
        inv.setItem(found, ItemStack.EMPTY);
        return next;
    }

    private static int findInInventory(Inventory inv, ItemStack target) {
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            if (STUtils.isSameItem(target, inv.getItem(i))) return i;
        }
        return -1;
    }

    private static void applyForcedHotbarSlot(ServerPlayer player) {
        SyncedClientSettings.ForcedHotbarSlotSettings.get(player).apply(player);
    }

    private static void playSound(ServerPlayer player, SoundEvent sound) {
        player.playNotifySound(sound, SoundSource.PLAYERS, 1, 1);
    }
}
