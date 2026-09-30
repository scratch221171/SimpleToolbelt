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
import net.scratch221171.simpletoolbelt.STUtils;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;
import net.scratch221171.simpletoolbelt.compat.curios.STCuriosHelper;

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
        ItemStack belt = findToolbeltStack(player);
        if (belt.isEmpty()) {
            return;
        }

        ToolbeltStorage storage = ToolbeltStorage.get(player.server);
        UUID beltId = ToolbeltStorage.ensureId(belt);
        ToolbeltContents stored = storage.get(beltId);
        ItemStack mainHand = player.getMainHandItem();
        ToolbeltContents.StackGroup init = stored.ring().initial();
        ToolbeltContents.StackGroup cur = stored.ring().current();

        if (requestedSlot == SelectBeltSlotPayload.STOW_INDEX) {
            for (int i = 0; i < init.stacks().size(); i++) {
                if (!init.stacks().get(i).isEmpty() && cur.stacks().get(i).isEmpty()) {
                    for (int j = 0; j < Inventory.INVENTORY_SIZE; j++) {
                        if (STUtils.isSame(
                                player.getInventory().getItem(j), init.stacks().get(i))) {
                            cur = cur.withStack(i, player.getInventory().getItem(j));
                            player.getInventory().setItem(j, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        } else {
            if (requestedSlot >= 0 && requestedSlot < cur.stacks().size()) {
                // ベルト自身を手に持ったまま操作するのを防ぐ
                if (mainHand == belt) {
                    return;
                }

                // 選択したアイテムが空なら何もしない
                if (cur.getStack(requestedSlot).isEmpty()) {
                    return;
                }

                // mainhandが空でなければ一旦退避させる
                boolean needsToPlaceBackInInventory = false;
                if (!mainHand.isEmpty()) {
                    int origin = -1;
                    for (int i = 0; i < init.stacks().size(); i++) {
                        if (STUtils.isSame(init.stacks().get(i), mainHand)
                                && cur.stacks().get(i).isEmpty()) {
                            origin = i;
                            break;
                        }
                    }

                    if (origin >= 0) {
                        // ベルトに収納できるスロットがある
                        cur = cur.withStack(origin, mainHand.copy());
                    } else {
                        needsToPlaceBackInInventory = true;
                        // ベルトに収納できるスロットがない -> 通常のインベントリへ退避
                        // メインハンドの消去がまだなので待機
                    }
                }

                // mainhandが空で、あるスロットを選択 -> スロットから手に移動
                player.setItemInHand(InteractionHand.MAIN_HAND, cur.stacks().get(requestedSlot));
                if (needsToPlaceBackInInventory) {
                    if (player.isAlive() && !player.hasDisconnected()) {
                        player.getInventory().placeItemBackInInventory(mainHand);
                    } else {
                        player.drop(mainHand, false);
                    }
                }
                cur = cur.withStack(requestedSlot, ItemStack.EMPTY);

            } else {
                // 無効なrequestedSlot
                return;
            }
        }

        ToolbeltStorage.update(player, beltId, new ToolbeltContents(new ToolbeltContents.Ring(init, cur)));
        ToolbeltMenu.refreshIfOpen(player, beltId, storage.get(beltId));
    }

    private static ItemStack findToolbeltStack(ServerPlayer player) {
        if (ModList.get().isLoaded("curios")) {
            Optional<ItemStack> belt = STCuriosHelper.getBeltInCurios(player);
            if (belt.isPresent()) {
                return belt.get();
            }
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(STItems.TOOLBELT.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void requestBeltContent(RequestBeltContentsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && ToolbeltStorage.playerHasBelt(serverPlayer, payload.id())) {
                PacketDistributor.sendToPlayer(
                        serverPlayer,
                        new SyncBeltContentsPayload(
                                payload.id(),
                                ToolbeltStorage.get(serverPlayer.server).get(payload.id())));
            }
        });
    }

    private static void openBeltMenuInCreative(OpenBeltMenuInCreativePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                int slot = payload.slotIndex();
                Inventory inv = serverPlayer.getInventory();
                if (slot < 0 || slot >= inv.getContainerSize()) {
                    return;
                }
                ItemStack beltStack = inv.getItem(slot);
                if (beltStack.is(STItems.TOOLBELT.get())) {
                    UUID id = ToolbeltStorage.ensureId(beltStack);
                    serverPlayer.server.tell(new TickTask(
                            serverPlayer.server.getTickCount(), () -> ToolbeltItem.openMenu(serverPlayer, id)));
                }
            }
        });
    }
}
