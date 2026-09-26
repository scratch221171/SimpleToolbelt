package net.scratch221171.simpletoolbelt.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.scratch221171.simpletoolbelt.common.component.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import org.jspecify.annotations.NonNull;

public class ToolbeltItem extends Item {

    public ToolbeltItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inventory, p) -> ToolbeltMenu.forHeldItem(containerId, inventory, stack),
                            Component.translatable(this.getDescriptionId(stack))),
                    buf -> buf.writeEnum(hand)); // ← 追加データとしてhandだけ書き込む
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean canFitInsideContainerItems(@NonNull ItemStack stack) {
        return false;
    }

    /** Convenience accessor used by client code (wheel overlay, etc.). */
    public static ToolbeltContents getContent(ItemStack stack) {
        return stack.getOrDefault(STDataComponents.TOOLBELT_CONTENTS.get(), ToolbeltContents.DEFAULT);
    }
}
