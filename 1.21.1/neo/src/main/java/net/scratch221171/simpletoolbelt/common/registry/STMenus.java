package net.scratch221171.simpletoolbelt.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;

public class STMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRAR =
            DeferredRegister.create(Registries.MENU, Const.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ToolbeltMenu>> TOOLBELT =
            REGISTRAR.register("toolbelt", () -> IMenuTypeExtension.create(ToolbeltMenu::new));

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
