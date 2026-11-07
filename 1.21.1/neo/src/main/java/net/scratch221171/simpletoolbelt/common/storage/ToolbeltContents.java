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
 * @param initial ベルトに入れたアイテムの原本
 * @param current 今入っているアイテム
 */
public record ToolbeltContents(List<ItemStack> initial, List<ItemStack> current) {

    private static final ToolbeltContents EMPTY = new ToolbeltContents(List.of(), List.of());

    public static final Codec<ToolbeltContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemStack.OPTIONAL_CODEC.listOf().fieldOf("initial").forGetter(ToolbeltContents::initial),
                    ItemStack.OPTIONAL_CODEC.listOf().fieldOf("current").forGetter(ToolbeltContents::current))
            .apply(instance, ToolbeltContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToolbeltContents> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(ToolbeltContents.CODEC);

    public static ToolbeltContents create(int size) {
        return EMPTY.addSlot(size);
    }

    public int totalSize() {
        if (initial.size() != current.size())
            throw new IllegalStateException("The sizes of 'initial' and 'current' don't match!");
        return initial().size();
    }

    public ItemStack getInitial(int index) {
        return initial.get(index);
    }

    public ToolbeltContents withInitial(int index, ItemStack stack) {
        List<ItemStack> copy = new ArrayList<>(initial);
        copy.set(index, stack);
        return new ToolbeltContents(copy, current);
    }

    public ItemStack getCurrent(int index) {
        return current.get(index);
    }

    public ToolbeltContents withCurrent(int index, ItemStack stack) {
        List<ItemStack> copy = new ArrayList<>(current);
        copy.set(index, stack);
        return new ToolbeltContents(initial, copy);
    }

    public ToolbeltContents addSlot(int n) {
        List<ItemStack> add = Collections.nCopies(n, ItemStack.EMPTY);
        List<ItemStack> ini = new ArrayList<>(initial);
        List<ItemStack> cur = new ArrayList<>(current);
        ini.addAll(add);
        cur.addAll(add);
        return new ToolbeltContents(ini, cur);
    }

    public boolean isEmpty() {
        for (int i = 0; i < totalSize(); i++) {
            if (!getInitial(i).isEmpty() || !getCurrent(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
