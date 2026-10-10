package net.scratch221171.simpletoolbelt.common.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import net.scratch221171.simpletoolbelt.common.registry.STRecipeSerializers;
import net.scratch221171.simpletoolbelt.config.ServerConfig;
import org.jspecify.annotations.NonNull;

public class ToolbeltPageUpgradeRecipe implements SmithingRecipe {
    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;

    public ToolbeltPageUpgradeRecipe(Ingredient template, Ingredient base, Ingredient addition) {
        this.template = template;
        this.base = base;
        this.addition = addition;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, @NonNull Level level) {
        ItemStack belt = input.base();
        return template.test(input.template())
                && base.test(belt)
                && addition.test(input.addition())
                && belt.getItem() instanceof ToolbeltItem
                && belt.getOrDefault(STDataComponents.EXTRA_PAGES, 0) < ServerConfig.Item.MAX_EXTRA_PAGES.getAsInt();
    }

    @Override
    public @NonNull ItemStack assemble(SmithingRecipeInput input, HolderLookup.@NonNull Provider registries) {
        ItemStack result = input.base().copy();
        result.set(STDataComponents.EXTRA_PAGES, result.getOrDefault(STDataComponents.EXTRA_PAGES, 0) + 1);
        return result;
    }

    @Override
    public @NonNull ItemStack getResultItem(HolderLookup.@NonNull Provider registries) {
        return new ItemStack(STItems.TOOLBELT.get());
    }

    @Override
    public boolean isTemplateIngredient(@NonNull ItemStack stack) {
        return template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(@NonNull ItemStack stack) {
        return base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(@NonNull ItemStack stack) {
        return addition.test(stack);
    }

    @Override
    public boolean isIncomplete() {
        return template.hasNoItems() || base.hasNoItems() || addition.hasNoItems();
    }

    @Override
    public @NonNull RecipeSerializer<?> getSerializer() {
        return STRecipeSerializers.PAGE_UPGRADE.get();
    }

    public static final MapCodec<ToolbeltPageUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Ingredient.CODEC
                            .optionalFieldOf("template", Ingredient.EMPTY)
                            .forGetter(r -> r.template),
                    Ingredient.CODEC.fieldOf("base").forGetter(r -> r.base),
                    Ingredient.CODEC.fieldOf("addition").forGetter(r -> r.addition))
            .apply(i, ToolbeltPageUpgradeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolbeltPageUpgradeRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC,
                    r -> r.template,
                    Ingredient.CONTENTS_STREAM_CODEC,
                    r -> r.base,
                    Ingredient.CONTENTS_STREAM_CODEC,
                    r -> r.addition,
                    ToolbeltPageUpgradeRecipe::new);

    public static class Serializer implements RecipeSerializer<ToolbeltPageUpgradeRecipe> {
        @Override
        public @NonNull MapCodec<ToolbeltPageUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NonNull StreamCodec<RegistryFriendlyByteBuf, ToolbeltPageUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
