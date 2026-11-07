package net.scratch221171.simpletoolbelt.common.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.STUtils;

public final class SyncedClientSettings {

    private static final Map<UUID, AutoSwitchSettings.Settings> AUTO_SWITCH_SETTINGS = new ConcurrentHashMap<>();
    private static final Map<UUID, ForcedHotbarSlotSettings.Settings> FORCED_HOTBAR_SLOT_SETTINGS =
            new ConcurrentHashMap<>();

    public static void remove(ServerPlayer p) {
        AutoSwitchSettings.remove(p);
        ForcedHotbarSlotSettings.remove(p);
    }

    public static final class AutoSwitchSettings {

        public static Settings get(ServerPlayer p) {
            return AUTO_SWITCH_SETTINGS.getOrDefault(p.getUUID(), Settings.DEFAULT);
        }

        public static void set(ServerPlayer p, Settings s) {
            AUTO_SWITCH_SETTINGS.put(p.getUUID(), s);
        }

        public static void remove(ServerPlayer p) {
            AUTO_SWITCH_SETTINGS.remove(p.getUUID());
        }

        public record Settings(List<String> entries, boolean allowEmptyHand) {
            public static final Settings DEFAULT = new Settings(List.of(), true);

            public boolean allows(ItemStack held) {
                if (held.isEmpty()) return allowEmptyHand;
                if (entries.isEmpty()) {
                    return true;
                } else {
                    return STUtils.anyMatch(held, entries);
                }
            }
        }
    }

    public static final class ForcedHotbarSlotSettings {

        public static Settings get(ServerPlayer p) {
            return FORCED_HOTBAR_SLOT_SETTINGS.getOrDefault(p.getUUID(), Settings.DEFAULT);
        }

        public static void set(ServerPlayer p, Settings s) {
            FORCED_HOTBAR_SLOT_SETTINGS.put(p.getUUID(), s);
        }

        public static void remove(ServerPlayer p) {
            FORCED_HOTBAR_SLOT_SETTINGS.remove(p.getUUID());
        }

        public record Settings(int slot) {
            public static final Settings DEFAULT = new Settings(0);

            public void apply(ServerPlayer p) {
                if (slot < 1 || slot > 9) return;
                p.getInventory().selected = slot - 1;
                p.connection.send(new ClientboundSetCarriedItemPacket(slot - 1));
            }
        }
    }
}
