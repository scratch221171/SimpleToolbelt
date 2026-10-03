package net.scratch221171.simpletoolbelt.common.network;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STUtils;
import org.jspecify.annotations.NonNull;

// C2S
public record SelectBeltSlotPayload(UUID uuid, int slot) implements CustomPacketPayload {

    public static final int STOW_INDEX = -1;

    public static final Type<SelectBeltSlotPayload> TYPE = new Type<>(STUtils.id(Const.ID.Payload.SELECT_BELT_SLOT));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectBeltSlotPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    SelectBeltSlotPayload::uuid,
                    ByteBufCodecs.VAR_INT,
                    SelectBeltSlotPayload::slot,
                    SelectBeltSlotPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
