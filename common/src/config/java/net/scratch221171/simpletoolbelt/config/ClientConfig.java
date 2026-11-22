package net.scratch221171.simpletoolbelt.config;

import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntries;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntry;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntryBuilder;

public class ClientConfig {
    private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

    public static ConfigEntries APPEARANCE = BUILDER.category(ConfigID.Client.APPEARANCE, Appearance.ENTRIES);

    public static ConfigEntries BEHAVIOR = BUILDER.category(ConfigID.Client.BEHAVIOR, Behaviour.ENTRIES);

    public static final class Appearance {
        private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

        public static final ConfigEntries SCREEN = BUILDER.category(ConfigID.Client.SCREEN, Screen.ENTRIES);

        public static final ConfigEntries HUD = BUILDER.category(ConfigID.Client.HUD, Hud.ENTRIES);

        public static final class Screen {
            private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

            public static final ConfigEntries TOOLBELT_RADIAL_MENU =
                    BUILDER.category(ConfigID.Client.RADIAL_MENU, ToolbeltRadialMenu.ENTRIES);

            public static final class ToolbeltRadialMenu {
                private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

                public static final ConfigEntry.DoubleEntry WHEEL_RADIUS =
                        BUILDER.defineInRange(ConfigID.Client.WHEEL_RADIUS, 70, 0, Double.MAX_VALUE);

                public static final ConfigEntry.DoubleEntry INNER_DEADZONE_RADIUS =
                        BUILDER.defineInRange(ConfigID.Client.INNER_DEADZONE_RADIUS, 20, 0, Double.MAX_VALUE);

                public static final ConfigEntry.DoubleEntry OUTER_STOW_ZONE_RADIUS =
                        BUILDER.defineInRange(ConfigID.Client.OUTER_STOW_ZONE_RADIUS, 150, 0, Double.MAX_VALUE);

                public static final ConfigEntry.DoubleEntry MAX_RADIUS_RATIO = BUILDER.comment(
                                "Clamp the outer radius to (this value) * min(screen width, height). 0 = disabled")
                        .defineInRange(ConfigID.Client.MAX_RADIUS_RATIO, 0.45, 0, 1);

                public static final ConfigEntry.BooleanEntry ENABLE_HIGHLIGHT = BUILDER.comment(
                                "Enable highlighting for the selected item.")
                        .define(ConfigID.Client.ENABLE_HIGHLIGHT, true);

                public static final ConfigEntry.BooleanEntry ENABLE_BLUR = BUILDER.comment(
                                "Enable the blur effect on the background.")
                        .define(ConfigID.Client.ENABLE_BLUR, true);

                public static final ConfigEntry.BooleanEntry SHOW_KEY_BINDINGS =
                        BUILDER.define(ConfigID.Client.SHOW_KEY_BINDINGS, true);

                public static final ConfigEntry.BooleanEntry HIDE_UNUSED_SLOTS = BUILDER.comment(
                                "Eliminate the space occupied by empty slots and allocate a larger area to the occupied slots.")
                        .define(ConfigID.Client.SHOW_UNUSED_SLOTS, false);

                public static final ConfigEntries ENTRIES = BUILDER.build();
            }

            public static final ConfigEntries ENTRIES = BUILDER.build();
        }

        public static final class Hud {
            private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

            public static final ConfigEntry.BooleanEntry ENABLED = BUILDER.comment(
                            "A list of tools recently used that have hotkeys assigned to them.")
                    .define(ConfigID.Client.ENABLED, true);

            public static final ConfigEntry.IntEntry X_OFFSET = BUILDER.define(ConfigID.Client.X_OFFSET, 0);

            public static final ConfigEntry.IntEntry Y_OFFSET = BUILDER.define(ConfigID.Client.Y_OFFSET, 0);

            public static final ConfigEntry.DoubleEntry DAMPING_RATIO = BUILDER.comment(
                            "A constant used to sort tools by how recently they were used. The closer the value is to 0, the more weight is given to recent usage.")
                    .defineInRange(ConfigID.Client.DAMPING_RATIO, 0.97, 0, 1);

            public static final ConfigEntry.IntEntry LENGTH =
                    BUILDER.defineInRange(ConfigID.Client.LENGTH, 3, 1, Integer.MAX_VALUE);

            public static final ConfigEntries ENTRIES = BUILDER.build();
        }

        public static final ConfigEntries ENTRIES = BUILDER.build();
    }

    public static final class Behaviour {
        private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

        public static final ConfigEntries RADIAL_MENU =
                BUILDER.category(ConfigID.Client.RADIAL_MENU, RadialMenu.ENTRIES);

        public static final ConfigEntries AUTO_SWITCH_CONDITION =
                BUILDER.category(ConfigID.Client.AUTO_SWITCH_CONDITION, AutoSwitchCondition.ENTRIES);

        public static final class RadialMenu {
            private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

            public static final ConfigEntry.BooleanEntry PAUSE_GAME = BUILDER.comment(
                            "Works only in single-player mode. May cause some lag!")
                    .define(ConfigID.Client.PAUSE_GAME, false);

            public static final ConfigEntry.BooleanEntry REMEMBER_PAGE =
                    BUILDER.comment("Remember the last page you opened.").define(ConfigID.Client.REMEMBER_PAGE, true);

            public static final ConfigEntry.IntEntry FORCED_HOTBAR_SLOT = BUILDER.comment(
                            "Moves the selected hotbar slot to the specified position when switching tools. 0 = disabled")
                    .defineInRange(ConfigID.Client.FORCED_HOTBAR_SLOT, 0, 0, 9);

            public static final ConfigEntry.EnumEntry<AlreadyTakenOutMode> ALREADY_TAKEN_OUT = BUILDER.comment(
                            "What happens when an item in a slot selected via the radial menu or a hotkey has already been extracted.")
                    .defineEnum(ConfigID.Client.ALREADY_TAKEN_OUT, AlreadyTakenOutMode.FIND_AND_EQUIP);

            public static final ConfigEntries RADIAL_MENU_OPEN_CONDITION =
                    BUILDER.category(ConfigID.Client.RADIAL_MENU_OPEN_CONDITION, RadialMenuOpenCondition.ENTRIES);

            public enum AlreadyTakenOutMode {
                DO_NOTHING(0),
                FIND_AND_STOW(1),
                FIND_AND_EQUIP(2);

                private final int id;

                AlreadyTakenOutMode(int id) {
                    this.id = id;
                }

                public int getId() {
                    return id;
                }
            }

            public static final class RadialMenuOpenCondition {
                private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

                public static final ConfigEntry.BooleanEntry ALLOW_EMPTY_HAND =
                        BUILDER.define(ConfigID.Client.ALLOW_EMPTY_HAND, true);

                public static final ConfigEntries ENTRIES = BUILDER.build();
            }

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

    public static final ConfigEntries ENTRIES = BUILDER.build();
}
