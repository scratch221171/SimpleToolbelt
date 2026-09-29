package net.scratch221171.simpletoolbelt.common.network;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.STUtils;
import org.jspecify.annotations.NonNull;

// C2S
public record RequestBeltContentsPayload(UUID id) implements CustomPacketPayload {
    public static final Type<RequestBeltContentsPayload> TYPE =
            new Type<>(STUtils.id(Const.ID.Payload.REQUEST_BELT_CONTENTS));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestBeltContentsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, RequestBeltContentsPayload::id, RequestBeltContentsPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
