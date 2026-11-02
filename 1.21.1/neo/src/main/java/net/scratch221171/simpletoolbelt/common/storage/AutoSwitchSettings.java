package net.scratch221171.simpletoolbelt.common.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.STUtils;

public final class AutoSwitchSettings {
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

    private static final Map<UUID, Settings> MAP = new ConcurrentHashMap<>();

    public static Settings get(ServerPlayer p) {
        return MAP.getOrDefault(p.getUUID(), Settings.DEFAULT);
    }

    public static void set(ServerPlayer p, Settings s) {
        MAP.put(p.getUUID(), s);
    }

    public static void remove(ServerPlayer p) {
        MAP.remove(p.getUUID());
    }
}
