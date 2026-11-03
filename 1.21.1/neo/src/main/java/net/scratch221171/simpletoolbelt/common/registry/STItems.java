package net.scratch221171.simpletoolbelt.common.registry;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.item.MenuOpenerItem;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;

public class STItems {
    public static final DeferredRegister.Items REGISTRAR = DeferredRegister.createItems(Const.MOD_ID);

    public static final DeferredItem<ToolbeltItem> TOOLBELT =
            REGISTRAR.registerItem(Const.ID.Item.TOOLBELT, ToolbeltItem::new);

    public static final DeferredItem<ToolbeltItem> NETHERITE_TOOLBELT = REGISTRAR.registerItem(
            Const.ID.Item.NETHERITE_TOOLBELT, ToolbeltItem::new, new Item.Properties().fireResistant());

    public static final DeferredItem<MenuOpenerItem> MENU_OPENER =
            REGISTRAR.register(Const.ID.Item.MENU_OPENER, () -> new MenuOpenerItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
