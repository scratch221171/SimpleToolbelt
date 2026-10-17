package net.scratch221171.simpletoolbelt.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STUtils;
import org.jspecify.annotations.NonNull;

// C2S
public record ToggleAutoSwitchPayload() implements CustomPacketPayload {
    public static final Type<ToggleAutoSwitchPayload> TYPE =
            new Type<>(STUtils.id(Const.ID.Payload.TOGGLE_AUTO_SWITCH));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAutoSwitchPayload> STREAM_CODEC =
            StreamCodec.unit(new ToggleAutoSwitchPayload());

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
