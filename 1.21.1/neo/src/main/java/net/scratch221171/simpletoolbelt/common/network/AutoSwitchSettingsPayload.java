package net.scratch221171.simpletoolbelt.common.network;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.storage.SyncedClientSettings;
import org.jspecify.annotations.NonNull;

public record AutoSwitchSettingsPayload(List<String> entries, boolean allowEmptyHand) implements CustomPacketPayload {
    public static final Type<AutoSwitchSettingsPayload> TYPE =
            new Type<>(STUtils.id(Const.ID.Payload.AUTO_SWITCH_SETTINGS));

    public static final StreamCodec<ByteBuf, AutoSwitchSettingsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256).apply(ByteBufCodecs.list(64)),
            AutoSwitchSettingsPayload::entries,
            ByteBufCodecs.BOOL,
            AutoSwitchSettingsPayload::allowEmptyHand,
            AutoSwitchSettingsPayload::new);

    public static void refresh(AutoSwitchSettingsPayload payload, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer sp) {
            SyncedClientSettings.AutoSwitchSettings.set(
                    sp,
                    new SyncedClientSettings.AutoSwitchSettings.Settings(
                            List.copyOf(payload.entries()), payload.allowEmptyHand()));
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
