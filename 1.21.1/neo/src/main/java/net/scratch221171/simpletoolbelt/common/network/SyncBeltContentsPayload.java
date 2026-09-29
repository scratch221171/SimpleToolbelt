package net.scratch221171.simpletoolbelt.common.network;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.STUtils;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import org.jspecify.annotations.NonNull;

// S2C
public record SyncBeltContentsPayload(UUID id, ToolbeltContents contents) implements CustomPacketPayload {
    public static final Type<SyncBeltContentsPayload> TYPE =
            new Type<>(STUtils.id(Const.ID.Payload.SYNC_BELT_CONTENTS));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBeltContentsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    SyncBeltContentsPayload::id,
                    ToolbeltContents.STREAM_CODEC,
                    SyncBeltContentsPayload::contents,
                    SyncBeltContentsPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
