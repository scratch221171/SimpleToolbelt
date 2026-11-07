package net.scratch221171.simpletoolbelt.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.scratch221171.simpletoolbelt.Const;

public class STResourceKeys {

    public static final class Enchantments {
        public static final ResourceKey<Enchantment> HANDY =
                ResourceKey.create(Registries.ENCHANTMENT, STCommonUtils.id(Const.ID.Enchantments.HANDY));
    }
}
