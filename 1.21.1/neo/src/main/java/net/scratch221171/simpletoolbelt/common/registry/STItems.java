package net.scratch221171.simpletoolbelt.common.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;

public class STItems {
    public static final DeferredRegister.Items REGISTRAR = DeferredRegister.createItems(Const.MOD_ID);

    public static final DeferredItem<ToolbeltItem> TOOLBELT =
            REGISTRAR.registerItem(Const.ID.Item.TOOLBELT, ToolbeltItem::new);

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
