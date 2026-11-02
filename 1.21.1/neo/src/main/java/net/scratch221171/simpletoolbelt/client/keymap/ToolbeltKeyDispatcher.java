package net.scratch221171.simpletoolbelt.client.keymap;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.gui.ToolbeltRadialMenuScreen;
import net.scratch221171.simpletoolbelt.common.network.SelectBeltSlotPayload;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public final class ToolbeltKeyDispatcher {
    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return; // リピート・解放は無視
        dispatch(InputConstants.getKey(event.getKey(), event.getScanCode()));
    }

    @SubscribeEvent
    public static void onMouse(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        dispatch(InputConstants.Type.MOUSE.getOrCreate(event.getButton()));
    }

    private static void dispatch(InputConstants.Key key) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (mc.screen == null || mc.screen instanceof ToolbeltRadialMenuScreen)) {
            ToolbeltKeyMap.get(key)
                    .ifPresent(
                            ref -> PacketDistributor.sendToServer(new SelectBeltSlotPayload(ref.beltId(), ref.slot())));
        }
    }
}
