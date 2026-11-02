package net.scratch221171.examplemod.client;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.scratch221171.examplemod.Constants;
import net.scratch221171.examplemod.client.mdk.config.KeyedConfigScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class ModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigScreenFactoryRegistry.INSTANCE.register(Constants.MODID, (modId, parent) ->
                new ConfigurationScreen(modId, parent, KeyedConfigScreen::new)
        );
    }
}
