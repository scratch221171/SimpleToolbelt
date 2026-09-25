package net.scratch221171.simpletoolbelt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Const {
    private Const() {}

    public static final String MOD_ID = "simpletoolbelt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String INITIALIZING = "Initializing {}";

    public static final class ID {
        private ID() {}

        public static final class Item {
            private Item() {}

            public static final String TOOLBELT = "toolbelt";
        }

        public static final class DataComponent {
            private DataComponent() {}

            public static final String STORED_ITEM = "stored_item";
        }
    }
}
