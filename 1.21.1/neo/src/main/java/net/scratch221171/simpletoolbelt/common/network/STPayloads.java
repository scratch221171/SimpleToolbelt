package net.scratch221171.simpletoolbelt.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.component.StoredItem;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;

@EventBusSubscriber(modid = Const.MOD_ID)
public class STPayloads {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        // NOTE: method name/shape (playToServer vs playBidirectional) needs verification against
        // your NeoForge version's PayloadRegistrar — this is a client->server-only payload.
        registrar.playToServer(
                SelectToolbeltSlotPayload.TYPE, SelectToolbeltSlotPayload.STREAM_CODEC, STPayloads::handleSelect);
    }

    private static void handleSelect(SelectToolbeltSlotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                applySelection(serverPlayer, payload.slot());
            }
        });
    }

    private static void applySelection(ServerPlayer player, int requestedSlot) {
        ItemStack belt = findToolbeltStack(player);
        if (belt.isEmpty()) {
            return;
        }

        StoredItem stored = ToolbeltItem.getStored(belt);
        ItemStack mainHand = player.getMainHandItem();

        if (!mainHand.isEmpty()) {
            int origin = stored.activeSlot();
            boolean canReturnToOrigin = origin != StoredItem.NO_ACTIVE_SLOT
                    && matchesGhost(mainHand, stored.activeGhost())
                    && stored.getStack(origin).isEmpty(); // GUIで既に埋められていないか確認

            if (canReturnToOrigin) {
                stored = stored.withStack(origin, mainHand.copy());
            } else {
                // ベルト由来と確認できない、または元の場所が塞がれている -> 通常のインベントリへ退避
                ItemStack toStash = mainHand.copy();
                if (!player.getInventory().add(toStash)) {
                    return; // 入りきらないなら選択自体を中止し、手持ちはそのまま
                }
            }
        }

        if (requestedSlot == StoredItem.NO_ACTIVE_SLOT) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            stored = stored.withActiveSlot(StoredItem.NO_ACTIVE_SLOT, ItemStack.EMPTY);
        } else if (requestedSlot >= 0 && requestedSlot < stored.totalSlots()) {
            ItemStack selected = stored.getStack(requestedSlot);
            player.setItemInHand(InteractionHand.MAIN_HAND, selected.copy());
            stored = stored.withStack(requestedSlot, ItemStack.EMPTY)
                    .withActiveSlot(requestedSlot, selected.copy());
        } else {
            return;
        }

        belt.set(STDataComponents.STORED_ITEM.get(), stored);
    }

    private static boolean matchesGhost(ItemStack mainHand, ItemStack ghost) {
        if (mainHand.isEmpty() || ghost.isEmpty()) {
            return false;
        }
        return mainHand.getItem() == ghost.getItem();
    }

    private static ItemStack findToolbeltStack(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(STItems.TOOLBELT.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
        // TODO: also scan Curios slots once that integration is added (see your roadmap step 7).
    }
}
