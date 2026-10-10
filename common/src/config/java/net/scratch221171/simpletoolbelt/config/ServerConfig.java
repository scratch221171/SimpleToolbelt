package net.scratch221171.simpletoolbelt.config;

import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntries;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntry;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntryBuilder;

public class ServerConfig {
    private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

    public static final ConfigEntries ITEM = BUILDER.category(ConfigID.Server.ITEM, Item.ENTRIES);

    public static final class Item {
        private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

        public static final ConfigEntry.IntEntry TOOLBELT_BASE_PAGES = BUILDER.comment("Number of pages in a Toolbelt.").defineInRange(ConfigID.Server.TOOLBELT_BASE_PAGES, 1, 1, 8);

        public static final ConfigEntry.IntEntry NETHERITE_BASE_PAGES = BUILDER.comment("Number of pages in a Netherite Toolbelt.").defineInRange(ConfigID.Server.NETHERITE_BASE_PAGES, 3, 1, 8);

        public static final ConfigEntry.IntEntry MAX_EXTRA_PAGES = BUILDER.comment("Maximum number of pages you can add with item").defineInRange(ConfigID.Server.MAX_EXTRA_PAGES, 5, 0, 64);

        public static final ConfigEntries ENTRIES = BUILDER.build();
    }

    public static final ConfigEntries ENTRIES = BUILDER.build();
}
