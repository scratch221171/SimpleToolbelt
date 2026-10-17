package net.scratch221171.simpletoolbelt.common.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
import net.scratch221171.simpletoolbelt.common.registry.STAttachments;
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
        registrar.playToServer(
                ToggleAutoSwitchPayload.TYPE, ToggleAutoSwitchPayload.STREAM_CODEC, STPayloads::toggleAutoSwitch);
    }

    private static void handleSelect(SelectBeltSlotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ToolbeltItem.applySelection(
                        serverPlayer,
                        payload.uuid(),
                        payload.slot(),
                        Component.translatable(Const.LangKey.Item.INVENTORY_FULL));
            }
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

    private static void toggleAutoSwitch(ToggleAutoSwitchPayload payload, IPayloadContext context) {
        Player player = context.player();
        boolean enabled = !player.getData(STAttachments.AUTO_TOOL_SWITCH);
        player.setData(STAttachments.AUTO_TOOL_SWITCH, enabled);
        player.displayClientMessage(
                Component.translatable(
                        Const.LangKey.Item.TOGGLE_AUTO_SWITCH,
                        enabled
                                ? Component.literal("ON").withStyle(ChatFormatting.GREEN)
                                : Component.literal("OFF").withStyle(ChatFormatting.RED)),
                true);
    }
}
