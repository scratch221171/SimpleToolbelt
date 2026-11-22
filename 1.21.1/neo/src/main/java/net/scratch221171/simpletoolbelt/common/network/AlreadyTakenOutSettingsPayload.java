package net.scratch221171.simpletoolbelt.common.network;

import java.util.function.IntFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.storage.SyncedClientSettings;
import net.scratch221171.simpletoolbelt.config.ClientConfig;
import org.jspecify.annotations.NonNull;

public record AlreadyTakenOutSettingsPayload(ClientConfig.Behaviour.RadialMenu.AlreadyTakenOutMode mode)
        implements CustomPacketPayload {
    public static final Type<AlreadyTakenOutSettingsPayload> TYPE =
            new Type<>(STUtils.id(Const.ID.Payload.ALREADY_TAKEN_OUT_SETTINGS));

    public static final IntFunction<ClientConfig.Behaviour.RadialMenu.AlreadyTakenOutMode> BY_ID = ByIdMap.continuous(
            ClientConfig.Behaviour.RadialMenu.AlreadyTakenOutMode::getId,
            ClientConfig.Behaviour.RadialMenu.AlreadyTakenOutMode.values(),
            ByIdMap.OutOfBoundsStrategy.ZERO);

    public static final StreamCodec<RegistryFriendlyByteBuf, AlreadyTakenOutSettingsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.idMapper(BY_ID, ClientConfig.Behaviour.RadialMenu.AlreadyTakenOutMode::getId),
                    AlreadyTakenOutSettingsPayload::mode,
                    AlreadyTakenOutSettingsPayload::new);

    public static void refresh(AlreadyTakenOutSettingsPayload payload, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer sp) {
            SyncedClientSettings.AlreadyTakenOutSettings.set(
                    sp, new SyncedClientSettings.AlreadyTakenOutSettings.Settings(payload.mode));
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
