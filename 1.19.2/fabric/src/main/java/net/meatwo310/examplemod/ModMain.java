package net.scratch221171.simpletoolbelt;

import net.fabricmc.api.ModInitializer;
import net.scratch221171.simpletoolbelt.config.ModConfigs;
import net.scratch221171.simpletoolbelt.mdk.config.PlatformConfigRegistrar;
import net.scratch221171.simpletoolbelt.mdk.config.VersionedConfigSpec;

public class ModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOGGER.debug(Constants.INITIALIZING, ModUtils.loc("1.19.2-fabric"));
        PlatformConfigRegistrar.registerAll(Constants.MODID, VersionedConfigSpec.bindAll(ModConfigs.ALL));
    }
}
