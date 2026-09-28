package net.scratch221171.simpletoolbelt.datagen.model;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
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
            if (!(entry.get() instanceof BlockItem)) basicItem(entry.get());
        });
    }
}
