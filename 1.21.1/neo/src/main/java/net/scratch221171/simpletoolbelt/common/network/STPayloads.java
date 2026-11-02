package net.scratch221171.simpletoolbelt.common.network;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.network.ClientBeltCache;
import net.scratch221171.simpletoolbelt.common.storage.AutoSwitchSettings;

@EventBusSubscriber(modid = Const.MOD_ID)
public class STPayloads {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                SelectBeltSlotPayload.TYPE, SelectBeltSlotPayload.STREAM_CODEC, SelectBeltSlotPayload::handleSelect);
        registrar.playToServer(
                RequestBeltContentsPayload.TYPE,
                RequestBeltContentsPayload.STREAM_CODEC,
                RequestBeltContentsPayload::requestBeltContent);
        registrar.playToServer(
                OpenBeltMenuInCreativePayload.TYPE,
                OpenBeltMenuInCreativePayload.STREAM_CODEC,
                OpenBeltMenuInCreativePayload::openBeltMenuInCreative);
        registrar.playToClient(
                SyncBeltContentsPayload.TYPE,
                SyncBeltContentsPayload.STREAM_CODEC,
                // client依存防止のため、ラムダのまま
                (payload, ctx) -> ClientBeltCache.handleSync(payload, ctx));
        registrar.playToServer(
                ToggleAutoSwitchPayload.TYPE,
                ToggleAutoSwitchPayload.STREAM_CODEC,
                (payload, ctx) -> ToggleAutoSwitchPayload.toggleAutoSwitch(ctx));
        registrar.playToServer(
                AutoSwitchSettingsPayload.TYPE, AutoSwitchSettingsPayload.STREAM_CODEC, (payload, ctx) -> {
                    if (ctx.player() instanceof ServerPlayer sp) {
                        AutoSwitchSettings.set(
                                sp,
                                new AutoSwitchSettings.Settings(
                                        List.copyOf(payload.entries()), payload.allowEmptyHand()));
                    }
                });
    }
}
