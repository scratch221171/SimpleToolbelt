package net.scratch221171.simpletoolbelt.common.network;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;
import net.scratch221171.simpletoolbelt.compat.curios.STCuriosHelper;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Const.MOD_ID)
public class STPayloads {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                SelectBeltSlotPayload.TYPE, SelectBeltSlotPayload.STREAM_CODEC, STPayloads::handleSelect);
        registrar.playToServer(
                RequestBeltContentsPayload.TYPE,
                RequestBeltContentsPayload.STREAM_CODEC,
                STPayloads::requestBeltContent);
        registrar.playToServer(
                OpenBeltMenuInCreativePayload.TYPE,
                OpenBeltMenuInCreativePayload.STREAM_CODEC,
                STPayloads::openBeltMenuInCreative);
    }

    private static void handleSelect(SelectBeltSlotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                applySelection(serverPlayer, payload.slot());
            }
        });
    }

    // ホイールでの操作を反映
    private static void applySelection(ServerPlayer player, int requestedSlot) {
        Optional.ofNullable(findToolbeltStack(player)).ifPresent(belt -> {
            ToolbeltStorage storage = ToolbeltStorage.get(player.server);
            UUID beltId = storage.ensureId(player, belt);
            Optional.ofNullable(storage.getOrNull(beltId)).ifPresent(stored -> {
                ItemStack mainHand = player.getMainHandItem();

                if (requestedSlot == SelectBeltSlotPayload.STOW_INDEX) {
                    for (int i = 0; i < stored.totalSlots(); i++) {
                        if (!stored.getInitialFlat(i).isEmpty()
                                && stored.getCurrentFlat(i).isEmpty()) {
                            for (int j = 0; j < Inventory.INVENTORY_SIZE; j++) {
                                if (ItemStack.isSameItem(
                                        stored.getInitialFlat(i),
                                        player.getInventory().getItem(j))) {
                                    stored = stored.withCurrentFlat(
                                            i, player.getInventory().getItem(j));
                                    player.getInventory().setItem(j, ItemStack.EMPTY);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    if (requestedSlot >= 0 && requestedSlot < stored.totalSlots()) {
                        // 選択したアイテムが空なら何もしない
                        if (stored.getCurrentFlat(requestedSlot).isEmpty()) {
                            return;
                        }

                        // mainhandが空でなければ一旦退避させる
                        boolean needsToPlaceBackInInventory = false;
                        if (!mainHand.isEmpty()) {
                            int origin = -1;
                            for (int i = 0; i < stored.totalSlots(); i++) {
                                if (STUtils.canStow(stored.getInitialFlat(i), mainHand)
                                        && stored.getCurrentFlat(i).isEmpty()) {
                                    origin = i;
                                    break;
                                }
                            }

                            if (origin >= 0) {
                                // ベルトに収納できるスロットがある
                                stored = stored.withCurrentFlat(origin, mainHand.copy());
                            } else {
                                needsToPlaceBackInInventory = true;
                                // ベルトに収納できるスロットがない -> 通常のインベントリへ退避
                                // メインハンドの消去がまだなので待機
                            }
                        }

                        // mainhandが空で、あるスロットを選択 -> スロットから手に移動
                        player.setItemInHand(InteractionHand.MAIN_HAND, stored.getCurrentFlat(requestedSlot));
                        if (needsToPlaceBackInInventory) {
                            if (player.isAlive() && !player.hasDisconnected()) {
                                player.getInventory().placeItemBackInInventory(mainHand);
                            } else {
                                player.drop(mainHand, false);
                            }
                        }
                        stored = stored.withCurrentFlat(requestedSlot, ItemStack.EMPTY);

                    } else {
                        // 無効なrequestedSlot
                        return;
                    }
                }

                ToolbeltStorage.update(player, beltId, stored);
                ToolbeltMenu.refreshIfOpen(player, beltId, storage.getOrNull(beltId));
            });
        });
    }

    @Nullable private static ItemStack findToolbeltStack(ServerPlayer player) {
        if (ModList.get().isLoaded("curios")) {
            Optional<ItemStack> belt = STCuriosHelper.getBeltInCurios(player);
            if (belt.isPresent()) {
                return belt.get();
            }
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ToolbeltItem) {
                return stack;
            }
        }
        return null;
    }

    private static void requestBeltContent(RequestBeltContentsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                Optional.ofNullable(ToolbeltStorage.findBelt(serverPlayer, payload.id()))
                        .ifPresent(stack -> PacketDistributor.sendToPlayer(
                                serverPlayer,
                                new SyncBeltContentsPayload(
                                        payload.id(),
                                        ToolbeltStorage.get(serverPlayer.server)
                                                .resolve(serverPlayer, payload.id(), stack))));
            }
        });
    }

    private static void openBeltMenuInCreative(OpenBeltMenuInCreativePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                int slot = payload.slot();
                Inventory inv = serverPlayer.getInventory();
                if (slot < 0 || slot >= inv.getContainerSize()) {
                    return;
                }
                ItemStack beltStack = inv.getItem(slot);
                if (beltStack.getItem() instanceof ToolbeltItem) {
                    UUID id = ToolbeltStorage.get(serverPlayer.server).ensureId(serverPlayer, beltStack);
                    serverPlayer.server.tell(new TickTask(
                            serverPlayer.server.getTickCount(),
                            () -> ToolbeltItem.openMenu(serverPlayer, id, beltStack)));
                }
            }
        });
    }
}
