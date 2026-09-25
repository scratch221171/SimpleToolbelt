package net.scratch221171.simpletoolbelt;

import net.scratch221171.simpletoolbelt.config.ModConfigs;
import net.scratch221171.simpletoolbelt.mdk.config.PlatformConfigRegistrar;
import net.scratch221171.simpletoolbelt.mdk.config.VersionedConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MODID)
public class ModMain {
    public ModMain() {
        Constants.LOGGER.debug(Constants.INITIALIZING, ModUtils.loc("1.18.2-forge"));
        PlatformConfigRegistrar.registerAll(VersionedConfigSpec.bindAll(ModConfigs.ALL));
    }
}
