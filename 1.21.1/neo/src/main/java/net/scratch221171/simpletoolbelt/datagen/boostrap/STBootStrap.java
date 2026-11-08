package net.scratch221171.simpletoolbelt.datagen.boostrap;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STResourceKeys;
import net.scratch221171.simpletoolbelt.common.STTags;
import net.scratch221171.simpletoolbelt.common.STUtils;

public class STBootStrap {

    public static RegistrySetBuilder getRegistrySetBuilder() {
        RegistrySetBuilder BUILDER = new RegistrySetBuilder();
        BUILDER.add(
                Registries.ENCHANTMENT,
                bootstrap -> bootstrap.register(
                        STResourceKeys.Enchantments.HANDY,
                        Enchantment.enchantment(new Enchantment.EnchantmentDefinition(
                                        bootstrap.lookup(Registries.ITEM).getOrThrow(STTags.Items.TOOLBELT),
                                        Optional.of(bootstrap
                                                .lookup(Registries.ITEM)
                                                .getOrThrow(STTags.Items.TOOLBELT)),
                                        30,
                                        1,
                                        new Enchantment.Cost(10, 0),
                                        new Enchantment.Cost(50, 0),
                                        2,
                                        List.of(EquipmentSlotGroup.ANY)))
                                .build(STUtils.id(Const.ID.Enchantments.HANDY))));
        return BUILDER;
    }
}
