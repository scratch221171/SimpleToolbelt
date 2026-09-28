package net.scratch221171.simpletoolbelt.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.scratch221171.simpletoolbelt.Const;
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
        ItemStack beltStack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> ToolbeltMenu.forBeltStack(id, inv, beltStack),
                            Component.translatable(Const.LangKey.TOOLBELT_SCREEN_TITLE)),
                    buf -> buf.writeVarInt(slot));
        }
        return InteractionResultHolder.sidedSuccess(beltStack, level.isClientSide);
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
