package net.scratch221171.simpletoolbelt.common.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Toolbeltの中身を管理するためのレコード
 *
 * @param pages 1周につき1ring。将来的にList<Ring>に変更するかも
 */
public record ToolbeltContents(List<Page> pages) {

    public static final int PAGE_SIZE = 8;
    public static final ToolbeltContents EMPTY = new ToolbeltContents(List.of());

    public static final Codec<ToolbeltContents> CODEC =
            Page.CODEC.listOf().xmap(ToolbeltContents::new, ToolbeltContents::pages);
    public static final StreamCodec<RegistryFriendlyByteBuf, ToolbeltContents> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(ToolbeltContents.CODEC);

    public record Page(StackGroup initial, StackGroup current) {
        public static final Page EMPTY = new Page(StackGroup.EMPTY, StackGroup.EMPTY);

        public static final Codec<Page> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        StackGroup.CODEC.fieldOf("initial").forGetter(Page::initial),
                        StackGroup.CODEC.fieldOf("current").forGetter(Page::current))
                .apply(instance, Page::new));

        public Page withInitial(int index, ItemStack stack) {
            return new Page(initial.withStack(index, stack), current);
        }

        public Page withCurrent(int index, ItemStack stack) {
            return new Page(initial, current.withStack(index, stack));
        }
    }

    public record StackGroup(List<ItemStack> stacks) {
        public static final StackGroup EMPTY = new StackGroup(Collections.nCopies(PAGE_SIZE, ItemStack.EMPTY));

        public static final Codec<StackGroup> CODEC =
                ItemStack.OPTIONAL_CODEC.listOf(PAGE_SIZE, PAGE_SIZE).xmap(StackGroup::new, StackGroup::stacks);

        public ItemStack getStack(int index) {
            return stacks.get(index);
        }

        public StackGroup withStack(int index, ItemStack stack) {
            List<ItemStack> copy = new ArrayList<>(stacks);
            copy.set(index, stack);
            return new StackGroup(copy);
        }
    }

    public int pagesSize() {
        return pages.size();
    }

    public Page getPage(int index) {
        return pages.get(index);
    }

    public ToolbeltContents withPage(int index, Page page) {
        List<Page> copy = new ArrayList<>(pages);
        copy.set(index, page);
        return new ToolbeltContents(copy);
    }

    public int totalSlots() {
        return pages.size() * PAGE_SIZE;
    }

    public ItemStack getInitialFlat(int flatIndex) {
        return pages.get(flatIndex / PAGE_SIZE).initial().getStack(flatIndex % PAGE_SIZE);
    }

    public ToolbeltContents withInitialFlat(int flatIndex, ItemStack stack) {
        return withPage(
                flatIndex / PAGE_SIZE, getPage(flatIndex / PAGE_SIZE).withInitial(flatIndex % PAGE_SIZE, stack));
    }

    public ItemStack getCurrentFlat(int flatIndex) {
        return pages.get(flatIndex / PAGE_SIZE).current().getStack(flatIndex % PAGE_SIZE);
    }

    public ToolbeltContents withCurrentFlat(int flatIndex, ItemStack stack) {
        return withPage(
                flatIndex / PAGE_SIZE, getPage(flatIndex / PAGE_SIZE).withCurrent(flatIndex % PAGE_SIZE, stack));
    }

    public ToolbeltContents increasePageTo(int n) {
        List<Page> copy = new ArrayList<>(pages);
        while (copy.size() < n) {
            copy.add(Page.EMPTY);
        }
        return new ToolbeltContents(copy);
    }

    public boolean isEmpty() {
        for (int i = 0; i < totalSlots(); i++) {
            if (!getInitialFlat(i).isEmpty() || !getCurrentFlat(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
