package net.scratch221171.simpletoolbelt.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.Const;

public final class STUtils {
    private STUtils() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, path);
    }

    // TODO: メソッドの必要性のチェック
    public static Boolean canStow(ItemStack initial, ItemStack stack) {
        return ItemStack.isSameItem(initial, stack);
    }
}
