package net.scratch221171.simpletoolbelt.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.component.StoredItem;

/**
 * Sent client -> server when the player releases R over a wheel slot (slot = flat index) or
 * over the "stow" zone near the screen edge (slot = StoredItem.NO_ACTIVE_SLOT).
 */
public record SelectToolbeltSlotPayload(int slot) implements CustomPacketPayload {

    public static final Type<SelectToolbeltSlotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, "select_toolbelt_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SelectToolbeltSlotPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SelectToolbeltSlotPayload::slot, SelectToolbeltSlotPayload::new);

    public boolean isStow() {
        return slot == StoredItem.NO_ACTIVE_SLOT;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
