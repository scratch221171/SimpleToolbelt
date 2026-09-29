package net.scratch221171.simpletoolbelt.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.scratch221171.simpletoolbelt.Const;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class STKeyMappings {

    public static final String CATEGORY = "key.categories." + Const.MOD_ID;

    public static final KeyMapping OPEN_WHEEL = new KeyMapping(
            "key." + Const.MOD_ID + ".wheel",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY);

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_WHEEL);
    }
}
