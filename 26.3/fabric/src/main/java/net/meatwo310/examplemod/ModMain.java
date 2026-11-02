package net.scratch221171.examplemod;

import net.fabricmc.api.ModInitializer;
import net.scratch221171.examplemod.config.ModConfigs;
import net.scratch221171.examplemod.mdk.config.PlatformConfigRegistrar;
import net.scratch221171.examplemod.mdk.config.VersionedConfigSpec;

public class ModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOGGER.debug(Constants.INITIALIZING, ModUtils.id("26.3-fabric"));
        PlatformConfigRegistrar.registerAll(Constants.MODID, VersionedConfigSpec.bindAll(ModConfigs.ALL));
    }
}
