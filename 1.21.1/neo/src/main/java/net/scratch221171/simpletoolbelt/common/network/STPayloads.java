package net.scratch221171.simpletoolbelt.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.STUtils;
import net.scratch221171.simpletoolbelt.common.component.ToolbeltContents;
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
                SelectBeltSlotPayload.TYPE, SelectBeltSlotPayload.STREAM_CODEC, STPayloads::handleSelect);
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

        ToolbeltContents stored = ToolbeltItem.getContent(belt);
        ItemStack mainHand = player.getMainHandItem();
        ToolbeltContents.StackGroup initial = stored.ring().initial();
        ToolbeltContents.StackGroup current = stored.ring().current();

        if (requestedSlot == SelectBeltSlotPayload.STOW_INDEX) {
            for (int i = 0; i < initial.stacks().size(); i++) {
                if (!initial.stacks().get(i).isEmpty() && current.stacks().get(i).isEmpty()) {
                    for (int j = 0; j < Inventory.INVENTORY_SIZE; j++) {
                        if (STUtils.isSame(player.getInventory().getItem(j), initial.stacks().get(i))) {
                            current = current.withStack(i, player.getInventory().getItem(j));
                            player.getInventory().setItem(j, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        } else {
            // ベルト自身を手に持ったまま操作するのは意味が無い(自己参照になる)ので弾く
            if (mainHand == belt) {
                return;
            }

            // 選択したアイテムが空なら何もしない
            if (current.getStack(requestedSlot).isEmpty()) {
                return;
            }

            // mainhandが空でなければ一旦退避させる
            if (!mainHand.isEmpty()) {
                int origin = -1;
                for (int i = 0; i < initial.stacks().size(); i++) {
                    if (STUtils.isSame(initial.stacks().get(i), mainHand)
                            && current.stacks().get(i).isEmpty()) {
                        origin = i;
                        break;
                    }
                }

                if (origin >= 0) {
                    current = current.withStack(origin, mainHand.copy());
                } else {
                    // ベルトに収納できる場所がない -> 通常のインベントリへ退避
                    player.getInventory().placeItemBackInInventory(mainHand);
                }
            }

            if (requestedSlot >= 0 && requestedSlot < current.stacks().size()) {
                // mainhandが空で、あるスロットを選択 -> スロットから手に移動
                player.setItemInHand(InteractionHand.MAIN_HAND, current.stacks().get(requestedSlot));
                current = current.withStack(requestedSlot, ItemStack.EMPTY);
            } else {
                return;
            }
        }

        belt.set(
                STDataComponents.TOOLBELT_CONTENTS.get(),
                new ToolbeltContents(new ToolbeltContents.Ring(initial, current)));
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
