package net.scratch221171.simpletoolbelt.client;

import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.gui.ToolbeltWheelScreen;
import net.scratch221171.simpletoolbelt.client.network.ClientBeltCache;
import net.scratch221171.simpletoolbelt.client.tooltip.ToolbeltTooltip;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import net.scratch221171.simpletoolbelt.compat.curios.STCuriosHelper;
import org.lwjgl.glfw.GLFW;

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

    public static ItemStack findToolbeltInInventory(Minecraft mc) {
        if (mc.player == null) {
            return ItemStack.EMPTY;
        }
        if (ModList.get().isLoaded("curios")) {
            Optional<ItemStack> belt = STCuriosHelper.getBeltInCurios(mc.player);
            if (belt.isPresent()) {
                return belt.get();
            }
        }
        for (ItemStack stack : mc.player.getInventory().items) {
            if (stack.getItem() instanceof ToolbeltItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() && Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen) {
            Slot slot = screen.getSlotUnderMouse();
            if (slot != null) stack = slot.getItem();
        }
        if (stack.getItem() instanceof ToolbeltItem) {
            ClientBeltCache.read(stack).ifPresent(contents -> {
                if (ToolbeltContents.isEmpty(contents) || !Screen.hasShiftDown()) return;
                List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
                int index = -1;
                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i).left().orElse(null) instanceof Component component
                            && component.getContents() instanceof TranslatableContents translatable
                            && translatable.getKey().equals("item.simpletoolbelt.toolbeltshiftKey")) {
                        index = i;
                        break;
                    }
                }
                elements.add(index + 1, Either.right(new ToolbeltTooltip(contents)));
            });
        }
    }

    @SubscribeEvent
    public static void clearCache(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientBeltCache.clear();
    }
}
