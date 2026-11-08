package net.scratch221171.simpletoolbelt.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.gui.ToolbeltScreen;
import net.scratch221171.simpletoolbelt.client.mdk.config.KeyedConfigScreen;
import net.scratch221171.simpletoolbelt.client.tooltip.ClientToolbeltTooltip;
import net.scratch221171.simpletoolbelt.client.tooltip.ToolbeltTooltip;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.item.MenuOpenerItem;
import net.scratch221171.simpletoolbelt.common.network.AutoSwitchSettingsPayload;
import net.scratch221171.simpletoolbelt.common.network.ForcedHotbarSlotSettingsPayload;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import net.scratch221171.simpletoolbelt.common.registry.STMenus;
import net.scratch221171.simpletoolbelt.config.ClientConfig;
import net.scratch221171.simpletoolbelt.config.VersionedClientConfig;

@Mod(value = Const.MOD_ID, dist = Dist.CLIENT)
public class SimpleToolbeltClient {
    public SimpleToolbeltClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (mod, parent) -> new ConfigurationScreen(mod, parent, KeyedConfigScreen::new));

        modEventBus.addListener(
                (RegisterMenuScreensEvent event) -> event.register(STMenus.TOOLBELT.get(), ToolbeltScreen::new));
        modEventBus.addListener((RegisterClientTooltipComponentFactoriesEvent event) ->
                event.register(ToolbeltTooltip.class, ClientToolbeltTooltip::new));
        modEventBus.addListener((ModConfigEvent event) -> {
            if (event instanceof ModConfigEvent.Reloading && event.getConfig().getType() == ModConfig.Type.CLIENT)
                sendClientSettings();
        });
        modEventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> ItemProperties.register(
                STItems.MENU_OPENER.get(),
                STUtils.id("filled"),
                (stack, level, entity, seed) ->
                        MenuOpenerItem.getContained(stack).isEmpty() ? 0.0f : 1.0f)));
    }

    static void sendClientSettings() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.getConnection() == null) return;
            PacketDistributor.sendToServer(new AutoSwitchSettingsPayload(
                    List.copyOf(VersionedClientConfig.Behaviour.AutoSwitchCondition.HELD_ITEM_WHITELIST.get()),
                    ClientConfig.Behaviour.AutoSwitchCondition.ALLOW_EMPTY_HAND.get()));
        });
        mc.execute(() -> {
            if (mc.getConnection() == null) return;
            PacketDistributor.sendToServer(new ForcedHotbarSlotSettingsPayload(
                    ClientConfig.Behaviour.RadialMenu.FORCED_HOTBAR_SLOT.getAsInt()));
        });
    }
}
