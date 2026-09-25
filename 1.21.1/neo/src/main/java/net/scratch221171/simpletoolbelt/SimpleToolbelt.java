package net.scratch221171.simpletoolbelt;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import net.scratch221171.simpletoolbelt.common.registry.STMenus;
import net.scratch221171.simpletoolbelt.config.ModConfigs;
import net.scratch221171.simpletoolbelt.mdk.config.PlatformConfigRegistrar;
import net.scratch221171.simpletoolbelt.mdk.config.VersionedConfigSpec;

@Mod(Const.MOD_ID)
public class SimpleToolbelt {
    public SimpleToolbelt(IEventBus modEventBus, ModContainer modContainer) {
        Const.LOGGER.debug(Const.INITIALIZING, STUtils.id("1.21.1-neo"));
        PlatformConfigRegistrar.registerAll(modContainer, VersionedConfigSpec.bindAll(ModConfigs.ALL));

        STDataComponents.register(modEventBus);
        STItems.register(modEventBus);
        STMenus.register(modEventBus);
        // STPayloads and STKeyMappings/STClientEvents are @EventBusSubscriber-annotated and are
        // picked up automatically by NeoForge's classpath scan — no explicit registration needed here.
    }
}
