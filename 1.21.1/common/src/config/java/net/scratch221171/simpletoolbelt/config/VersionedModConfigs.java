package net.scratch221171.simpletoolbelt.config;

import java.util.List;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigDeclaration;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigDeclarations;

public final class VersionedModConfigs {
    public static final List<ConfigDeclaration> ALL =
            ConfigDeclarations.append(ModConfigs.ALL, ModConfigs.CLIENT, VersionedClientConfig.ENTRIES);
}
