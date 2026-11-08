package net.scratch221171.simpletoolbelt.config;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntries;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntry;
import net.scratch221171.simpletoolbelt.mdk.config.ConfigEntryBuilder;

public class VersionedClientConfig {
    private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

    public static ConfigEntries BEHAVIOR = BUILDER.category(ConfigID.Client.BEHAVIOR, Behaviour.ENTRIES);

    public static final class Behaviour {
        private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

        public static final ConfigEntries RADIAL_MENU =
                BUILDER.category(ConfigID.Client.RADIAL_MENU, RadialMenu.ENTRIES);

        public static final class RadialMenu {
            private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

            public static final ConfigEntries RADIAL_MENU_OPEN_CONDITION =
                    BUILDER.category(ConfigID.Client.RADIAL_MENU_OPEN_CONDITION, RadialMenuOpenCondition.ENTRIES);

            public static final class RadialMenuOpenCondition {
                private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

                public static final ConfigEntry.ListEntry<String> HELD_ITEM_WHITELIST = BUILDER.comment(
                                "Auto switch only when the held item matches. Item id or #tag. Empty = always")
                        .defineListAllowEmpty(
                                ConfigID.Client.HELD_ITEM_WHITELIST,
                                List.of(),
                                () -> "#c:tools",
                                o -> o instanceof String s
                                        && ResourceLocation.tryParse(s.startsWith("#") ? s.substring(1) : s) != null);

                public static final ConfigEntries ENTRIES = BUILDER.build();
            }

            public static final ConfigEntries ENTRIES = BUILDER.build();
        }

        public static final ConfigEntries AUTO_SWITCH_CONDITION =
                BUILDER.category(ConfigID.Client.AUTO_SWITCH_CONDITION, AutoSwitchCondition.ENTRIES);

        public static final class AutoSwitchCondition {
            private static final ConfigEntryBuilder BUILDER = new ConfigEntryBuilder();

            public static final ConfigEntry.ListEntry<String> HELD_ITEM_WHITELIST = BUILDER.comment(
                            "Auto switch only when the held item matches. Item id or #tag. Empty = always")
                    .defineListAllowEmpty(
                            ConfigID.Client.HELD_ITEM_WHITELIST,
                            List.of("#c:tools"),
                            () -> "#c:tools",
                            o -> o instanceof String s
                                    && ResourceLocation.tryParse(s.startsWith("#") ? s.substring(1) : s) != null);

            public static final ConfigEntries ENTRIES = BUILDER.build();
        }

        public static final ConfigEntries ENTRIES = BUILDER.build();
    }

    public static final ConfigEntries ENTRIES = BUILDER.build();
}
