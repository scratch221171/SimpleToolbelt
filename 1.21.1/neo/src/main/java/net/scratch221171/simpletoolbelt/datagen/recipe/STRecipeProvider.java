package net.scratch221171.simpletoolbelt.datagen.recipe;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
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
    }
}
