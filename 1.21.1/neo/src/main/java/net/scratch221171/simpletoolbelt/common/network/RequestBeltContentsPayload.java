package net.scratch221171.simpletoolbelt.common.network;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;
import org.jspecify.annotations.NonNull;

// C2S
public record RequestBeltContentsPayload(UUID id) implements CustomPacketPayload {
    public static final Type<RequestBeltContentsPayload> TYPE =
            new Type<>(STUtils.id(Const.ID.Payload.REQUEST_BELT_CONTENTS));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestBeltContentsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, RequestBeltContentsPayload::id, RequestBeltContentsPayload::new);

    public static void requestBeltContent(RequestBeltContentsPayload payload, IPayloadContext context) {
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

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
