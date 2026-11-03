package net.scratch221171.simpletoolbelt.datagen.model;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.registry.STItems;

public class STItemModelProvider extends ItemModelProvider {
    public STItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Const.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        STItems.REGISTRAR.getEntries().forEach(entry -> {
            Item item = entry.get();
            if (item instanceof BlockItem) return;
            if (item == STItems.MENU_OPENER.get()) return; // 下で個別に生成
            basicItem(item);
        });
        menuOpener();
    }

    private void menuOpener() {
        ItemModelBuilder filled = withExistingParent("menu_opener_filled", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/menu_opener_filled"));

        withExistingParent(Const.ID.Item.MENU_OPENER, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/menu_opener"))
                .override()
                .predicate(modLoc("filled"), 1.0F)
                .model(filled)
                .end();
    }
}
