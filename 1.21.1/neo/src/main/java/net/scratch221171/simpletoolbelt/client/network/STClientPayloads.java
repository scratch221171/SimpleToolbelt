package net.scratch221171.simpletoolbelt.client.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.network.SyncBeltContentsPayload;

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class STClientPayloads {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                SyncBeltContentsPayload.TYPE, SyncBeltContentsPayload.STREAM_CODEC, ClientBeltCache::handleSync);
    }
}
