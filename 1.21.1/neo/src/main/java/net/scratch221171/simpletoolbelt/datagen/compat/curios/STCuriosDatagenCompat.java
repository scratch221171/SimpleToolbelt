package net.scratch221171.simpletoolbelt.datagen.compat.curios;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class STCuriosDatagenCompat {
    private STCuriosDatagenCompat() {}

    public static void register(GatherDataEvent event) {
        var fileHelper = event.getExistingFileHelper();
        event.createProvider((output, lookupProvider) -> new STCuriosDataProvider(output, lookupProvider, fileHelper));
    }
}
