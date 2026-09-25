package net.scratch221171.simpletoolbelt;

import net.scratch221171.simpletoolbelt.config.ModConfigs;
import net.scratch221171.simpletoolbelt.mdk.config.PlatformConfigRegistrar;
import net.scratch221171.simpletoolbelt.mdk.config.VersionedConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MODID)
public class ModMain {
    public ModMain(FMLJavaModLoadingContext ctx) {
        Constants.LOGGER.debug(Constants.INITIALIZING, ModUtils.loc("1.21.8-forge"));
        PlatformConfigRegistrar.registerAll(ctx.getContainer(), VersionedConfigSpec.bindAll(ModConfigs.ALL));
    }
}
