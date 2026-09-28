package net.scratch221171.simpletoolbelt.config;

import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntries;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntry;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntryBuilder;

public class ClientConfig {
    private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

    public static final ConfigEntries SCREEN = BUILDER
            .category(ConfigID.Client.SCREEN, Screen.ENTRIES);

    public static final class Screen {
        private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

        public static final ConfigEntries TOOLBELT_WHEEL = BUILDER
                .category(ConfigID.Client.TOOLBELT_WHEEL, ToolbeltWheel.ENTRIES);

        public static final class ToolbeltWheel {
            private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

            public static final ConfigEntry.DoubleEntry WHEEL_RADIUS = BUILDER.defineInRange(
                    ConfigID.Client.WHEEL_RADIUS, 70, 0, Double.MAX_VALUE
            );

            public static final ConfigEntry.DoubleEntry INNER_DEADZONE_RADIUS = BUILDER.defineInRange(
                    ConfigID.Client.INNER_DEADZONE_RADIUS, 30, 0, Double.MAX_VALUE
            );

            public static final ConfigEntry.DoubleEntry OUTER_STOW_ZONE_RADIUS = BUILDER.defineInRange(
                    ConfigID.Client.OUTER_STOW_ZONE_RADIUS, 150, 0, Double.MAX_VALUE
            );

            public static final ConfigEntry.BooleanEntry ENABLE_HIGHLIGHT = BUILDER
                    .comment("Enable highlighting for the selected item.")
                    .define(ConfigID.Client.ENABLE_HIGHLIGHT, true);

            public static final ConfigEntry.BooleanEntry ENABLE_BLUR = BUILDER
                    .comment("Enable the blur effect on the background.")
                    .define(ConfigID.Client.ENABLE_BLUR, true);

            public static final ConfigEntries ENTRIES = BUILDER.build();
        }

        public static final ConfigEntries ENTRIES = BUILDER.build();
    }

    public static final ConfigEntries ENTRIES = BUILDER.build();
}
