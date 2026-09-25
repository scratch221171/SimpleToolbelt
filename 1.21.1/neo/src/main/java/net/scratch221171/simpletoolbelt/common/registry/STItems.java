package net.scratch221171.simpletoolbelt.common.registry;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.component.StoredItem;

public class STItems {
    public static final DeferredRegister.Items REGISTRAR = DeferredRegister.createItems(Const.MOD_ID);

    public static final DeferredItem<Item> TOOLBELT = register(
            Const.ID.Item.TOOLBELT, new Item.Properties());

    private static DeferredItem<Item> register(String name, Item.Properties properties) {
        return REGISTRAR.register(name, () -> new Item(properties));
    }

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
