package net.scratch221171.simpletoolbelt.common.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

public final class AutoSwitchSettings {
    public record Settings(List<String> entries, boolean allowEmptyHand) {
        public static final Settings DEFAULT = new Settings(List.of(), true);

        public boolean allows(ItemStack held) {
            if (held.isEmpty()) return allowEmptyHand;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(held.getItem());
            for (String e : entries) {
                boolean isTag = e.startsWith("#");
                ResourceLocation rl = ResourceLocation.tryParse(isTag ? e.substring(1) : e);
                if (rl == null) continue;
                if (isTag ? held.is(TagKey.create(Registries.ITEM, rl)) : id.equals(rl)) return true;
            }
            return entries.isEmpty();
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
