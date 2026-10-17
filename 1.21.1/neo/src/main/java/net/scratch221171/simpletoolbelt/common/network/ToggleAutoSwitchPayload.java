package net.scratch221171.simpletoolbelt.common.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.registry.STAttachments;
import org.jspecify.annotations.NonNull;

// C2S
public record ToggleAutoSwitchPayload() implements CustomPacketPayload {
    public static final Type<ToggleAutoSwitchPayload> TYPE =
            new Type<>(STUtils.id(Const.ID.Payload.TOGGLE_AUTO_SWITCH));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAutoSwitchPayload> STREAM_CODEC =
            StreamCodec.unit(new ToggleAutoSwitchPayload());

    public static void toggleAutoSwitch(IPayloadContext context) {
        Player player = context.player();
        boolean enabled = !player.getData(STAttachments.AUTO_TOOL_SWITCH);
        player.setData(STAttachments.AUTO_TOOL_SWITCH, enabled);
        player.displayClientMessage(
                Component.translatable(
                        Const.LangKey.Item.TOGGLE_AUTO_SWITCH,
                        enabled
                                ? Component.literal("ON").withStyle(ChatFormatting.GREEN)
                                : Component.literal("OFF").withStyle(ChatFormatting.RED)),
                true);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
