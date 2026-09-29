package net.scratch221171.simpletoolbelt.client;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.network.OpenBeltMenuInCreativePayload;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;

public final class ClientBeltActions {
    private ClientBeltActions() {}

    public static void requestOpenInCreative(Slot slot) {
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen) {
            // クリエインベントリで初期化前に開かれないように
            if (slot.getItem().has(STDataComponents.BELT_ID)) {
                PacketDistributor.sendToServer(new OpenBeltMenuInCreativePayload(slot.getContainerSlot()));
            } else {
                // 謝罪
                Optional.ofNullable(Minecraft.getInstance().player)
                        .ifPresent(player -> player.displayClientMessage(
                                Component.translatable(Const.LangKey.Item.INITIALIZING_UUID), true));
            }
        }
    }
}
