package net.scratch221171.simpletoolbelt.datagen.recipe;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import org.jspecify.annotations.NonNull;

public class STRecipeProvider extends RecipeProvider {
    public STRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NonNull RecipeOutput output, HolderLookup.@NonNull Provider holderLookup) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, STItems.TOOLBELT)
                .pattern("#,#")
                .define('#', Items.LEATHER)
                .define(',', Items.GOLD_NUGGET)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(output);

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(STItems.TOOLBELT),
                        Ingredient.of(Tags.Items.INGOTS_NETHERITE),
                        RecipeCategory.MISC,
                        STItems.NETHERITE_TOOLBELT.get())
                .unlocks("has_template", has(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE))
                .save(output, Const.ID.Item.NETHERITE_TOOLBELT);
    }
}
