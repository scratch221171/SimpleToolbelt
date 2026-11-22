package net.scratch221171.simpletoolbelt.common.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.config.ClientConfig;
import net.scratch221171.simpletoolbelt.config.VersionedClientConfig;

public final class SyncedClientSettings {

    private static final Map<UUID, AutoSwitchSettings.Settings> AUTO_SWITCH_SETTINGS = new ConcurrentHashMap<>();
    private static final Map<UUID, ForcedHotbarSlotSettings.Settings> FORCED_HOTBAR_SLOT_SETTINGS =
            new ConcurrentHashMap<>();
    private static final Map<UUID, AlreadyTakenOutSettings.Settings> ALREADY_TAKEN_OUT_SETTINGS =
            new ConcurrentHashMap<>();

    public static void remove(ServerPlayer p) {
        AutoSwitchSettings.remove(p);
        ForcedHotbarSlotSettings.remove(p);
        AlreadyTakenOutSettings.remove(p);
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
            public static final Settings DEFAULT = new Settings(
                    VersionedClientConfig.Behaviour.AutoSwitchCondition.HELD_ITEM_WHITELIST.defaultValue(),
                    ClientConfig.Behaviour.AutoSwitchCondition.ALLOW_EMPTY_HAND.defaultValue());

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
            public static final Settings DEFAULT =
                    new Settings(ClientConfig.Behaviour.RadialMenu.FORCED_HOTBAR_SLOT.defaultValue());

            public void apply(ServerPlayer p) {
                if (slot < 1 || slot > 9) return;
                p.getInventory().selected = slot - 1;
                p.connection.send(new ClientboundSetCarriedItemPacket(slot - 1));
            }
        }
    }

    public static final class AlreadyTakenOutSettings {
        public static Settings get(ServerPlayer p) {
            return ALREADY_TAKEN_OUT_SETTINGS.getOrDefault(p.getUUID(), Settings.DEFAULT);
        }

        public static void set(ServerPlayer p, Settings s) {
            ALREADY_TAKEN_OUT_SETTINGS.put(p.getUUID(), s);
        }

        public static void remove(ServerPlayer p) {
            ALREADY_TAKEN_OUT_SETTINGS.remove(p.getUUID());
        }

        public record Settings(ClientConfig.Behaviour.RadialMenu.AlreadyTakenOutMode mode) {
            public static final Settings DEFAULT =
                    new Settings(ClientConfig.Behaviour.RadialMenu.ALREADY_TAKEN_OUT.defaultValue());

            public ClientConfig.Behaviour.RadialMenu.AlreadyTakenOutMode getMode() {
                return this.mode;
            }
        }
    }
}
