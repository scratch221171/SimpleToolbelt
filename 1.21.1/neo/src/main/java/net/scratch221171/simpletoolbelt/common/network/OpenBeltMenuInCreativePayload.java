package net.scratch221171.simpletoolbelt.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import org.jspecify.annotations.NonNull;

public record OpenBeltMenuInCreativePayload(int slot) implements CustomPacketPayload {
    public static final Type<OpenBeltMenuInCreativePayload> TYPE =
            new Type<>(STUtils.id(Const.ID.Payload.OPEN_BELT_MENU_IN_CREATIVE));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBeltMenuInCreativePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OpenBeltMenuInCreativePayload::slot, OpenBeltMenuInCreativePayload::new);

    public static void openBeltMenuInCreative(OpenBeltMenuInCreativePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                int slot = payload.slot();
                Inventory inv = serverPlayer.getInventory();
                if (slot < 0 || slot >= inv.getContainerSize()) {
                    return;
                }
                ItemStack beltStack = inv.getItem(slot);
                if (beltStack.getItem() instanceof ToolbeltItem) {
                    ToolbeltItem.openMenuNextTick(serverPlayer, beltStack);
                }
            }
        });
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
