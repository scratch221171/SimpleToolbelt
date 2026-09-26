package net.scratch221171.simpletoolbelt.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * Toolbeltの中身を管理するためのデータコンポーネント
 *
 * @param ring 1周につき1ring。将来的にList<Ring>に変更するかも
 */
public record ToolbeltContents(Ring ring) {

    public static final int RING_SIZE = 8;
    public static final ToolbeltContents DEFAULT = new ToolbeltContents(Ring.DEFAULT);

    public static final Codec<ToolbeltContents> CODEC = Ring.CODEC.xmap(ToolbeltContents::new, ToolbeltContents::ring);

    public record Ring(StackGroup initial, StackGroup current) {
        public static final Ring DEFAULT = new Ring(StackGroup.EMPTY, StackGroup.EMPTY);

        public static final Codec<Ring> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        StackGroup.CODEC.fieldOf("initial").forGetter(Ring::initial),
                        StackGroup.CODEC.fieldOf("current").forGetter(Ring::current))
                .apply(instance, Ring::new));
    }

    public record StackGroup(List<ItemStack> stacks) {
        public static final StackGroup EMPTY = new StackGroup(Collections.nCopies(RING_SIZE, ItemStack.EMPTY));

        public static final Codec<StackGroup> CODEC =
                ItemStack.OPTIONAL_CODEC.listOf(RING_SIZE, RING_SIZE).xmap(StackGroup::new, StackGroup::stacks);

        public ItemStack getStack(int index) {
            return stacks.get(index);
        }

        public StackGroup withStack(int index, ItemStack stack) {
            List<ItemStack> copy = new ArrayList<>(stacks);
            copy.set(index, stack);
            return new StackGroup(copy);
        }
    }
}
