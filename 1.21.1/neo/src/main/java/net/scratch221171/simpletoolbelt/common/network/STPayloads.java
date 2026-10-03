package net.scratch221171.simpletoolbelt.common.network;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.network.ClientBeltCache;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;

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
        registrar.playToClient(
                SyncBeltContentsPayload.TYPE,
                SyncBeltContentsPayload.STREAM_CODEC,
                (payload, ctx) -> ClientBeltCache.handleSync(payload, ctx));
    }

    private static void handleSelect(SelectBeltSlotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                applySelection(serverPlayer, payload.uuid(), payload.slot());
            }
        });
    }

    // ホイールでの操作を反映
    private static void applySelection(ServerPlayer player, UUID beltId, int requestedSlot) {
        ToolbeltStorage.findBelt(player, beltId).ifPresent(belt -> {
            ToolbeltContents stored = ToolbeltStorage.get(player.server).resolve(player, beltId, belt);
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
                if (requestedSlot < 0 || requestedSlot >= stored.totalSlots()) return;
                if (stored.getCurrentFlat(requestedSlot).isEmpty()) return;

                boolean needsToPlaceBackInInventory = false;
                if (!mainHand.isEmpty()) {
                    int origin = -1;
                    for (int i = 0; i < stored.totalSlots(); i++) {
                        if (ItemStack.isSameItem(stored.getInitialFlat(i), mainHand)
                                && stored.getCurrentFlat(i).isEmpty()) {
                            origin = i;
                            break;
                        }
                    }
                    if (origin >= 0) {
                        stored = stored.withCurrentFlat(origin, mainHand.copy());
                    } else {
                        needsToPlaceBackInInventory = true;
                    }
                }
                player.setItemInHand(
                        InteractionHand.MAIN_HAND,
                        stored.getCurrentFlat(requestedSlot).copy());
                if (needsToPlaceBackInInventory) {
                    player.getInventory().placeItemBackInInventory(mainHand);
                }
                stored = stored.withCurrentFlat(requestedSlot, ItemStack.EMPTY);
            }

            ToolbeltStorage.update(player, beltId, stored);
            ToolbeltMenu.refreshIfOpen(player, beltId, stored);
        });
    }

    private static void requestBeltContent(RequestBeltContentsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ToolbeltStorage.findBelt(serverPlayer, payload.id())
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
                    ToolbeltItem.openMenuNextTick(serverPlayer, beltStack);
                }
            }
        });
    }
}
