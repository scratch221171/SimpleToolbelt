package net.scratch221171.simpletoolbelt.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.storage.SyncedClientSettings;
import org.jspecify.annotations.NonNull;

public record ForcedHotbarSlotSettingsPayload(int slot) implements CustomPacketPayload {
    public static final Type<ForcedHotbarSlotSettingsPayload> TYPE =
            new Type<>(STUtils.id(Const.ID.Payload.FORCED_HOTBAR_SLOT_SETTINGS));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForcedHotbarSlotSettingsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ForcedHotbarSlotSettingsPayload::slot, ForcedHotbarSlotSettingsPayload::new);

    public static void refresh(ForcedHotbarSlotSettingsPayload payload, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer sp) {
            SyncedClientSettings.ForcedHotbarSlotSettings.set(
                    sp, new SyncedClientSettings.ForcedHotbarSlotSettings.Settings(payload.slot));
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
