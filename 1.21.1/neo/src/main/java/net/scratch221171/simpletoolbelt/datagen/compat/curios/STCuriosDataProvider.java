package net.scratch221171.simpletoolbelt.datagen.compat.curios;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.scratch221171.simpletoolbelt.Const;
import top.theillusivec4.curios.api.CuriosDataProvider;

public class STCuriosDataProvider extends CuriosDataProvider {
    public STCuriosDataProvider(
            PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
        super(Const.MOD_ID, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        this.createEntities("toolbelt").addPlayer().addSlots("belt");
    }
}
