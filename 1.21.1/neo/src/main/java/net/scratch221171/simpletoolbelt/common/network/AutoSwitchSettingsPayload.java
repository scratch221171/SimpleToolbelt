package net.scratch221171.simpletoolbelt.common.network;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.scratch221171.simpletoolbelt.common.STUtils;
import org.jspecify.annotations.NonNull;

public record AutoSwitchSettingsPayload(List<String> entries, boolean allowEmptyHand) implements CustomPacketPayload {
    public static final Type<AutoSwitchSettingsPayload> TYPE = new Type<>(STUtils.id("auto_switch_settings"));

    public static final StreamCodec<ByteBuf, AutoSwitchSettingsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(256).apply(ByteBufCodecs.list(64)),
            AutoSwitchSettingsPayload::entries,
            ByteBufCodecs.BOOL,
            AutoSwitchSettingsPayload::allowEmptyHand,
            AutoSwitchSettingsPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
