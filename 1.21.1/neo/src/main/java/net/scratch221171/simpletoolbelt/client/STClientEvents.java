package net.scratch221171.simpletoolbelt.client;

import com.mojang.datafixers.util.Either;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.gui.ToolbeltWheelScreen;
import net.scratch221171.simpletoolbelt.client.tooltip.ToolbeltTooltip;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import org.lwjgl.glfw.GLFW;

// bus = GAME (the default/"FORGE" bus) because InputEvent fires on the main NeoForge event bus,
// not the mod event bus. Only STKeyMappings/STPayloads use the mod bus (registration events).
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class STClientEvents {

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        if (!STKeyMappings.OPEN_WHEEL.matches(event.getKey(), event.getScanCode())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null) {
            return;
        }
        if (findToolbeltInInventory(mc).isEmpty()) {
            return;
        }
        mc.setScreen(new ToolbeltWheelScreen());
    }

    /** TODO: also check Curios slots once that integration exists — inventory-only for now. */
    public static ItemStack findToolbeltInInventory(Minecraft mc) {
        if (mc.player == null) {
            return ItemStack.EMPTY;
        }
        for (ItemStack stack : mc.player.getInventory().items) {
            if (stack.is(STItems.TOOLBELT.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(STItems.TOOLBELT.get()) || !Screen.hasShiftDown()) {
            return;
        }
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        elements.add(Math.min(1, elements.size()), Either.right(new ToolbeltTooltip(ToolbeltItem.getContent(stack))));
    }
}
