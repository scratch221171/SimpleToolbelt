package net.scratch221171.simpletoolbelt.common.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.recipe.ToolbeltPageUpgradeRecipe;

public class STRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRAR =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Const.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ToolbeltPageUpgradeRecipe>> PAGE_UPGRADE =
            REGISTRAR.register(Const.ID.Recipe.TOOLBELT_PAGE_UPGRADE, ToolbeltPageUpgradeRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
