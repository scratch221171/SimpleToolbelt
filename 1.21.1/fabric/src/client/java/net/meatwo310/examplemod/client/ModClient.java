package net.scratch221171.simpletoolbelt.client;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.scratch221171.simpletoolbelt.Constants;
import net.scratch221171.simpletoolbelt.client.mdk.config.KeyedConfigScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class ModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigScreenFactoryRegistry.INSTANCE.register(Constants.MODID, (modId, parent) ->
                new ConfigurationScreen(modId, parent, KeyedConfigScreen::new)
        );
    }
}
