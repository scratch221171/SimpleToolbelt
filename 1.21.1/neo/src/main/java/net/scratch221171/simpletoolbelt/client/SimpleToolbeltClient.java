package net.scratch221171.simpletoolbelt.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.gui.ToolbeltScreen;
import net.scratch221171.simpletoolbelt.client.mdk.config.KeyedConfigScreen;
import net.scratch221171.simpletoolbelt.common.registry.STMenus;

@Mod(value = Const.MOD_ID, dist = Dist.CLIENT)
public class SimpleToolbeltClient {
    public SimpleToolbeltClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (mod, parent) -> new ConfigurationScreen(mod, parent, KeyedConfigScreen::new));

        modEventBus.addListener(
                (RegisterMenuScreensEvent event) -> event.register(STMenus.TOOLBELT.get(), ToolbeltScreen::new));
    }
}
