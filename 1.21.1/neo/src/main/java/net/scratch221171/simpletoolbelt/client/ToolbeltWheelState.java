package net.scratch221171.simpletoolbelt.client;

import net.scratch221171.simpletoolbelt.common.component.StoredItem;

/** Pure client-side UI state — never touched server-side, never persisted. */
public class ToolbeltWheelState {

    private static boolean open = false;
    private static int hoveredSlot = StoredItem.NO_ACTIVE_SLOT;

    private ToolbeltWheelState() {}

    public static boolean isOpen() {
        return open;
    }

    public static void setOpen(boolean value) {
        open = value;
        if (!open) {
            hoveredSlot = StoredItem.NO_ACTIVE_SLOT;
        }
    }

    public static int getHoveredSlot() {
        return hoveredSlot;
    }

    public static void setHoveredSlot(int slot) {
        hoveredSlot = slot;
    }
}
