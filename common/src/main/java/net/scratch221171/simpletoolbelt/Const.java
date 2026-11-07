package net.scratch221171.simpletoolbelt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Const {

    public static final String MOD_ID = "simpletoolbelt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String INITIALIZING = "Initializing {}";

    public static final class ID {

        public static final class Item {

            public static final String TOOLBELT = "toolbelt";
            public static final String NETHERITE_TOOLBELT = "netherite_toolbelt";
            public static final String MENU_OPENER = "menu_opener";
        }

        public static final class DataComponent {

            public static final String BELT_ID = "belt_id";
            public static final String EXTRA_PAGES = "extra_pages";
            public static final String WRAPPED_ITEM = "wrapped_item";
        }

        public static final class Menu {

            public static final String TOOLBELT = "toolbelt";
        }

        public static final class Payload {

            public static final String SELECT_BELT_SLOT = "select_belt_slot";
            public static final String REQUEST_BELT_CONTENTS = "request_belt_contents";
            public static final String OPEN_BELT_MENU_IN_CREATIVE = "open_belt_menu_in_creative";
            public static final String SYNC_BELT_CONTENTS = "sync_belt_contents";
            public static final String TOGGLE_AUTO_SWITCH = "toggle_auto_switch";
            public static final String AUTO_SWITCH_SETTINGS = "auto_switch_settings";
            public static final String FORCED_HOTBAR_SLOT_SETTINGS = "forced_hotbar_slot_settings";
        }

        public static final class Recipe {
            public static final String TOOLBELT_PAGE_UPGRADE = "toolbelt_page_upgrade";
        }

        public static final class Attachment {
            public static final String AUTO_TOOL_SWITCH = "auto_tool_switch";
        }
    }

    public interface LangKey {

        String MOD_MENU = "modmenu.descriptionTranslation." + MOD_ID;

        final class Item implements LangKey {
            public static final String PREFIX = "item";

            public static final String INITIALIZING_UUID = define(PREFIX, ID.Item.TOOLBELT + "InitializingUUID");
            public static final String SHIFT_FOR_MORE_INFO = define(PREFIX, ID.Item.TOOLBELT + "ShiftKey");
            public static final String TOOLBELT_HINT = define(PREFIX, ID.Item.TOOLBELT + "Hint");
            public static final String EXTRA_PAGE = define(PREFIX, ID.Item.TOOLBELT + "ExtraPage");
            public static final String TOGGLE_AUTO_SWITCH = define(PREFIX, ID.Item.TOOLBELT + "ToggleAutoSwitch");
            public static final String INVENTORY_FULL = define(PREFIX, ID.Item.TOOLBELT + "InventoryFull");
            public static final String AUTO_SWITCH_FAILED = define(PREFIX, ID.Item.TOOLBELT + "AutoSwitchFailed");
            public static final String MENU_OPENER_HINT = define(PREFIX, ID.Item.MENU_OPENER + "Hint");
            public static final String EMPTY = define(PREFIX, ID.Item.MENU_OPENER + "Empty");
            public static final String CONTAINS = define(PREFIX, ID.Item.MENU_OPENER + "Contains");
        }

        final class Screen {
            public static final String PREFIX = "gui";

            public static final String TOOLBELT_SCREEN_TITLE = define(PREFIX, "toolbelt.title");
            public static final String RADIAL_MENU_SCREEN_TITLE = define(PREFIX, "radialMenu.title");
            public static final String RADIAL_MENU_TOOLTIP_STOW_ALL = define(PREFIX, "radialMenu.tooltip.stowAll");
            public static final String RADIAL_MENU_TOOLTIP_STOW = define(PREFIX, "radialMenu.tooltip.stow");
            public static final String RADIAL_MENU_PAGE_INDEX = define(PREFIX, "radialMenu.pageIndex");
            public static final String KEYMAP_OPEN = define(PREFIX, "keymap.open");
            public static final String KEYMAP_TITLE = define(PREFIX, "keymap.title");
            public static final String KEYMAP_HINT = define(PREFIX, "keymap.hint");
        }

        final class Command {
            public static final String PREFIX = "commands";

            public static final String NOT_FOUND = define(PREFIX, "list.failed.notfound");
            public static final String PRINT_ALL = define(PREFIX, "list.success.printall");
            public static final String RESTORED = define(PREFIX, "restore.success.restored");
        }

        final class Key {
            public static final String PREFIX = "key";

            public static final String CATEGORY = define(PREFIX, "category");
            public static final String RADIAL_MENU = define(PREFIX, "radialMenu");
            public static final String TOGGLE_AUTO_SWITCH = define(PREFIX, "toggleAutoSwitch");
        }

        static String define(String prefix, String key) {
            return prefix + "." + MOD_ID + "." + key;
        }
    }
}
