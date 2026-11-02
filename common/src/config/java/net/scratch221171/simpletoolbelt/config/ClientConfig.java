package net.scratch221171.simpletoolbelt.config;

import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntries;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntry;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntryBuilder;

public class ClientConfig {
    private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

    public static final ConfigEntries SCREEN = BUILDER.category(ConfigID.Client.SCREEN, Screen.ENTRIES);

    public static final ConfigEntries RADIAL_MENU_OPEN_CONDITION =
            BUILDER.category(ConfigID.Client.RADIAL_MENU_OPEN_CONDITION, RadialMenuOpenCondition.ENTRIES);

    public static final ConfigEntries AUTO_SWITCH_CONDITION =
            BUILDER.category(ConfigID.Client.AUTO_SWITCH_CONDITION, AutoSwitchCondition.ENTRIES);

    public static final class Screen {
        private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

        public static final ConfigEntries TOOLBELT_RADIAL_MENU =
                BUILDER.category(ConfigID.Client.RADIAL_MENU, ToolbeltRadialMenu.ENTRIES);

        public static final class ToolbeltRadialMenu {
            private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

            public static final ConfigEntry.BooleanEntry PAUSE_GAME = BUILDER.comment(
                            "Works only in single-player mode. May cause some lag!")
                    .define(ConfigID.Client.PAUSE_GAME, false);

            public static final ConfigEntry.BooleanEntry REMEMBER_PAGE =
                    BUILDER.comment("Remember the last page you opened.").define(ConfigID.Client.REMEMBER_PAGE, true);

            public static final ConfigEntry.DoubleEntry WHEEL_RADIUS =
                    BUILDER.defineInRange(ConfigID.Client.WHEEL_RADIUS, 70, 0, Double.MAX_VALUE);

            public static final ConfigEntry.DoubleEntry INNER_DEADZONE_RADIUS =
                    BUILDER.defineInRange(ConfigID.Client.INNER_DEADZONE_RADIUS, 20, 0, Double.MAX_VALUE);

            public static final ConfigEntry.DoubleEntry OUTER_STOW_ZONE_RADIUS =
                    BUILDER.defineInRange(ConfigID.Client.OUTER_STOW_ZONE_RADIUS, 150, 0, Double.MAX_VALUE);

            public static final ConfigEntry.BooleanEntry ENABLE_HIGHLIGHT = BUILDER.comment(
                            "Enable highlighting for the selected item.")
                    .define(ConfigID.Client.ENABLE_HIGHLIGHT, true);

            public static final ConfigEntry.BooleanEntry ENABLE_BLUR = BUILDER.comment(
                            "Enable the blur effect on the background.")
                    .define(ConfigID.Client.ENABLE_BLUR, true);

            public static final ConfigEntry.BooleanEntry SHOW_KEY_BINDINGS =
                    BUILDER.define(ConfigID.Client.SHOW_KEY_BINDINGS, true);

            public static final ConfigEntries ENTRIES = BUILDER.build();
        }

        public static final ConfigEntries ENTRIES = BUILDER.build();
    }

    public static final class RadialMenuOpenCondition {
        private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

        public static final ConfigEntry.BooleanEntry ALLOW_EMPTY_HAND =
                BUILDER.define(ConfigID.Client.ALLOW_EMPTY_HAND, true);

        public static final ConfigEntries ENTRIES = BUILDER.build();
    }

    public static final class AutoSwitchCondition {
        private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

        public static final ConfigEntry.BooleanEntry ALLOW_EMPTY_HAND =
                BUILDER.define(ConfigID.Client.ALLOW_EMPTY_HAND, false);

        public static final ConfigEntries ENTRIES = BUILDER.build();
    }

    public static final ConfigEntries ENTRIES = BUILDER.build();
}
