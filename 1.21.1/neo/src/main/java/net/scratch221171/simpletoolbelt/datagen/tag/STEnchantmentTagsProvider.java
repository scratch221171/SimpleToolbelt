package net.scratch221171.simpletoolbelt.datagen.tag;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.STResourceKeys;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class STEnchantmentTagsProvider extends EnchantmentTagsProvider {
    public STEnchantmentTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Const.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
        tag(EnchantmentTags.TRADEABLE).add(STResourceKeys.Enchantments.HANDY);
        tag(EnchantmentTags.IN_ENCHANTING_TABLE).add(STResourceKeys.Enchantments.HANDY);
    }
}
