package net.scratch221171.simpletoolbelt.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.mdk.config.KeyedConfigScreen;

@Mod(value = Const.MOD_ID, dist = Dist.CLIENT)
public class SimpleToolbeltClient {
    public SimpleToolbeltClient(ModContainer container) {
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (mod, parent) -> new ConfigurationScreen(mod, parent, KeyedConfigScreen::new));
    }
}
