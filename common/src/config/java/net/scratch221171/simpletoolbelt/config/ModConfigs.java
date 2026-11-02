package net.scratch221171.simpletoolbelt.config;

import java.util.List;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigDeclaration;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigSide;

public final class ModConfigs {
    public static final ConfigDeclaration SERVER = ConfigDeclaration.of(ConfigSide.SYNCED, ServerConfig.ENTRIES);
    public static final ConfigDeclaration CLIENT = ConfigDeclaration.of(ConfigSide.CLIENT, ClientConfig.ENTRIES);

    public static final List<ConfigDeclaration> ALL = List.of(SERVER, CLIENT);

    private ModConfigs() {}
}
