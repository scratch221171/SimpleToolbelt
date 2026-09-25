package net.scratch221171.simpletoolbelt;

import net.minecraft.resources.ResourceLocation;

public final class STUtils {
    private STUtils() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, path);
    }
}
