package net.scratch221171.simpletoolbelt.common.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.recipe.NetheriteToolbeltCloneRecipe;
import net.scratch221171.simpletoolbelt.common.recipe.ToolbeltPageUpgradeRecipe;

public class STRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRAR =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Const.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ToolbeltPageUpgradeRecipe>> PAGE_UPGRADE =
            REGISTRAR.register(Const.ID.Recipe.TOOLBELT_PAGE_UPGRADE, ToolbeltPageUpgradeRecipe.Serializer::new);

    public static final DeferredHolder<
                    RecipeSerializer<?>, SimpleCraftingRecipeSerializer<NetheriteToolbeltCloneRecipe>>
            NETHERITE_TOOLBELT_CLONE = REGISTRAR.register(
                    Const.ID.Recipe.NETHERITE_TOOLBELT_CLONE,
                    () -> new SimpleCraftingRecipeSerializer<>(NetheriteToolbeltCloneRecipe::new));

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
