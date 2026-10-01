package net.scratch221171.simpletoolbelt.datagen.tag;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STTags;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import org.jspecify.annotations.NonNull;

public class STItemTagsProvider extends ItemTagsProvider {
    public STItemTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(
                output,
                lookupProvider,
                CompletableFuture.completedFuture(tag -> Optional.empty()),
                Const.MOD_ID,
                existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
        tag(STTags.Items.TOOLBELT).add(STItems.TOOLBELT.get()).add(STItems.NETHERITE_TOOLBELT.get());
        tag(STTags.Items.CURIOS_BELT).addTag(STTags.Items.TOOLBELT);
    }
}
