package net.scratch221171.simpletoolbelt.common.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import net.scratch221171.simpletoolbelt.common.registry.STRecipeSerializers;
import org.jspecify.annotations.NonNull;

public class NetheriteToolbeltCloneRecipe extends ShapelessRecipe {
    public NetheriteToolbeltCloneRecipe(CraftingBookCategory category) {
        super(
                "",
                category,
                new ItemStack(STItems.NETHERITE_TOOLBELT.get()),
                NonNullList.of(
                        Ingredient.EMPTY,
                        Ingredient.of(STItems.NETHERITE_TOOLBELT),
                        Ingredient.of(Tags.Items.NETHER_STARS),
                        Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(Items.LEATHER),
                        Ingredient.of(Items.LEATHER),
                        Ingredient.of(Tags.Items.NUGGETS_GOLD)));
    }

    @Override
    public boolean matches(@NonNull CraftingInput input, @NonNull Level level) {
        if (!super.matches(input, level)) return false;
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).get(STDataComponents.BELT_ID) != null) return true;
        }
        return false;
    }

    @Override
    public @NonNull ItemStack assemble(CraftingInput input, HolderLookup.@NonNull Provider registries) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.is(STItems.NETHERITE_TOOLBELT)) return s.copyWithCount(1);
        }
        return ItemStack.EMPTY;
    }

    //    @Override
    //    public @NonNull NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
    //        NonNullList<ItemStack> list = NonNullList.withSize(input.size(), ItemStack.EMPTY);
    //        for (int i = 0; i < input.size(); i++) {
    //            ItemStack s = input.getItem(i);
    //            if (s.is(STItems.NETHERITE_TOOLBELT)) list.set(i, s.copyWithCount(1));
    //        }
    //        return list;
    //    }

    @Override
    public @NonNull RecipeSerializer<?> getSerializer() {
        return STRecipeSerializers.NETHERITE_TOOLBELT_CLONE.get();
    }
}
