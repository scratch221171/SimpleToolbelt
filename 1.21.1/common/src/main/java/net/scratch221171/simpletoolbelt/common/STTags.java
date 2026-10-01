package net.scratch221171.simpletoolbelt.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class STTags {

    private static final String COMMON = "c";

    private static ResourceLocation commonId(String path) {
        return ResourceLocation.fromNamespaceAndPath(COMMON, path);
    }

    public static final class Items {

        public static final TagKey<Item> TOOLBELT = createItem(STUtils.id("toolbelt"));
        public static final TagKey<Item> CURIOS_BELT =
                createItem(ResourceLocation.fromNamespaceAndPath("curios", "belt"));
    }

    private static TagKey<Item> createItem(final ResourceLocation name) {
        return TagKey.create(Registries.ITEM, name);
    }
}
