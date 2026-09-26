package net.scratch221171.simpletoolbelt;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class STUtils {
    private STUtils() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Const.MOD_ID, path);
    }

    public static Boolean isSame(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem();
    }
}
