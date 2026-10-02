package net.scratch221171.simpletoolbelt.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.datagen.compat.curios.STCuriosDatagenCompat;
import net.scratch221171.simpletoolbelt.datagen.lang.STEnglishLangProvider;
import net.scratch221171.simpletoolbelt.datagen.lang.STJapaneseLangProvider;
import net.scratch221171.simpletoolbelt.datagen.model.STItemModelProvider;
import net.scratch221171.simpletoolbelt.datagen.recipe.STRecipeProvider;
import net.scratch221171.simpletoolbelt.datagen.tag.STItemTagsProvider;

@EventBusSubscriber(modid = Const.MOD_ID)
public class STDataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        Const.LOGGER.info("Loading DataGenerators");
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        event.createProvider(STRecipeProvider::new);
        event.createProvider((output, lookupProvider) -> new STItemTagsProvider(output, lookupProvider, fileHelper));
        STCuriosDatagenCompat.register(event);
        event.createProvider(output -> new STItemModelProvider(output, fileHelper));
        event.createProvider(STEnglishLangProvider::new);
        event.createProvider(STJapaneseLangProvider::new);
    }
}
