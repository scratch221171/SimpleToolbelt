package net.scratch221171.simpletoolbelt.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.STUtils;
import org.jspecify.annotations.NonNull;

public record OpenBeltMenuInCreativePayload(int slotIndex) implements CustomPacketPayload {
    public static final Type<OpenBeltMenuInCreativePayload> TYPE =
            new Type<>(STUtils.id(Const.ID.Payload.OPEN_BELT_MENU_IN_CREATIVE));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBeltMenuInCreativePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    OpenBeltMenuInCreativePayload::slotIndex,
                    OpenBeltMenuInCreativePayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
