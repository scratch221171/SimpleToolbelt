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
        }

        public static final class DataComponent {

            public static final String BELT_ID = "belt_id";
        }

        public static final class Menu {

            public static final String TOOLBELT = "toolbelt";
        }

        public static final class Payload {

            public static final String SELECT_BELT_SLOT = "select_belt_slot";
            public static final String REQUEST_BELT_CONTENTS = "request_belt_contents";
            public static final String OPEN_BELT_MENU_IN_CREATIVE = "open_belt_menu_in_creative";
            public static final String SYNC_BELT_CONTENTS = "sync_belt_contents";
        }
    }

    public interface LangKey {

        String MOD_MENU = "modmenu.descriptionTranslation." + MOD_ID;

        final class Item implements LangKey {
            public static final String PREFIX = "item";

            public static final String INITIALIZING_UUID = define(PREFIX, ID.Item.TOOLBELT + "initializingUUID");
            public static final String SHIFT_FOR_MORE_INFO = define(PREFIX, ID.Item.TOOLBELT + "shiftKey");
            public static final String HINT = define(PREFIX, ID.Item.TOOLBELT + "hint");
        }

        final class Screen {
            public static final String PREFIX = "gui";

            public static final String TOOLBELT_SCREEN_TITLE = define(PREFIX, "toolbelt.title");
            public static final String WHEEL_SCREEN_TITLE = define(PREFIX, "wheel.title");
            public static final String WHEEL_TOOLTIP_STOW = define(PREFIX, "wheel.tooltip.stow");
            public static final String WHEEL_PAGE_INDEX = define(PREFIX, "wheel.pageIndex");
        }

        final class Commands {
            public static final String PREFIX = "commands";

            public static final String NOT_FOUND = define(PREFIX, "list.failed.notfound");
            public static final String PRINT_ALL = define(PREFIX, "list.success.printall");
            public static final String RESTORED = define(PREFIX, "restore.success.restored");
        }

        static String define(String prefix, String key) {
            return prefix + "." + MOD_ID + "." + key;
        }
    }
}
