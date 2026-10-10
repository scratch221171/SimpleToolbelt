package net.scratch221171.simpletoolbelt.datagen.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.scratch221171.simpletoolbelt.common.recipe.ToolbeltPageUpgradeRecipe;

public class ToolbeltPageUpgradeRecipeBuilder {
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final RecipeCategory category;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    private ToolbeltPageUpgradeRecipeBuilder(
            Ingredient template, Ingredient base, Ingredient addition, RecipeCategory category) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.category = category;
    }

    public static ToolbeltPageUpgradeRecipeBuilder upgrade(Ingredient template, Ingredient base, Ingredient addition) {
        return new ToolbeltPageUpgradeRecipeBuilder(template, base, addition, RecipeCategory.TOOLS);
    }

    public ToolbeltPageUpgradeRecipeBuilder unlockedBy(String key, Criterion<?> criterion) {
        this.criteria.put(key, criterion);
        return this;
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        if (criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        criteria.forEach(advancement::addCriterion);
        output.accept(
                id,
                new ToolbeltPageUpgradeRecipe(template, base, addition),
                advancement.build(id.withPrefix("recipes/" + category.getFolderName() + "/")));
    }
}
