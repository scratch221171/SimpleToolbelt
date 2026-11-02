package net.scratch221171.simpletoolbelt.common;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.Const;

public final class STUtils {
    private STUtils() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, path);
    }

    public static boolean anyMatch(ItemStack stack, List<String> rlEntries) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (String e : rlEntries) {
            boolean isTag = e.startsWith("#");
            ResourceLocation rl = ResourceLocation.tryParse(isTag ? e.substring(1) : e);
            if (rl == null) continue;
            if (isTag ? stack.is(TagKey.create(Registries.ITEM, rl)) : id.equals(rl)) return true;
        }
        return false;
    }
}
