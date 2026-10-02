package net.scratch221171.simpletoolbelt.common.item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.ClientBeltActions;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;
import net.scratch221171.simpletoolbelt.config.ServerConfig;
import org.jspecify.annotations.NonNull;

public class ToolbeltItem extends Item {

    public ToolbeltItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(
            @NonNull ItemStack stack, Level level, @NonNull Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide
                && entity instanceof ServerPlayer player
                && !stack.has(STDataComponents.BELT_ID.get())) {
            ToolbeltStorage.get(player.server).ensureId(player, stack);
        }
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(
            @NonNull Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack beltStack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            openMenu(
                    serverPlayer,
                    ToolbeltStorage.get(serverPlayer.server).ensureId(serverPlayer, beltStack),
                    beltStack);
        }
        return InteractionResultHolder.sidedSuccess(beltStack, level.isClientSide);
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            @NonNull ItemStack beltStack,
            @NonNull ItemStack other,
            @NonNull Slot slot,
            @NonNull ClickAction action,
            @NonNull Player player,
            @NonNull SlotAccess access) {
        if (action != ClickAction.SECONDARY || !other.isEmpty()) return false;
        if (player instanceof ServerPlayer serverPlayer) {
            UUID id = ToolbeltStorage.get(serverPlayer.server).ensureId(serverPlayer, beltStack);
            serverPlayer.server.tell(
                    new TickTask(serverPlayer.server.getTickCount(), () -> openMenu(serverPlayer, id, beltStack)));
        } else if (player.level().isClientSide) {
            ClientBeltActions.requestOpenInCreative(slot);
        }
        return true;
    }

    public static void openMenu(ServerPlayer player, UUID id, ItemStack stack) {
        Optional.ofNullable(ToolbeltStorage.get(player.server).getOrNull(id))
                .ifPresent(contents -> player.openMenu(
                        new SimpleMenuProvider(
                                (containerId, inv, p) -> ToolbeltMenu.forUUID(containerId, inv, id, stack),
                                Component.translatable(Const.LangKey.Screen.TOOLBELT_SCREEN_TITLE)),
                        buf -> {
                            buf.writeUUID(id);
                            ToolbeltContents.STREAM_CODEC.encode(buf, contents);
                        }));
    }

    @Override
    public void appendHoverText(
            @NonNull ItemStack stack,
            Item.@NonNull TooltipContext context,
            @NonNull List<Component> tooltipComponents,
            @NonNull TooltipFlag tooltipFlag) {
        if (tooltipFlag.hasShiftDown()) {
            tooltipComponents.add(Component.translatable(
                            Const.LangKey.Item.SHIFT_FOR_MORE_INFO,
                            Component.literal("Shift").withStyle(ChatFormatting.WHITE))
                    .withStyle(ChatFormatting.DARK_GRAY));
            tooltipComponents.add(
                    Component.translatable(Const.LangKey.Item.HINT).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(
                    Component.literal("UUID: " + stack.getOrDefault(STDataComponents.BELT_ID, "undefined"))
                            .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltipComponents.add(Component.translatable(
                            Const.LangKey.Item.SHIFT_FOR_MORE_INFO,
                            Component.literal("Shift").withStyle(ChatFormatting.GRAY))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public int getPageCount(ItemStack stack) {
        if (stack.is(STItems.TOOLBELT)) return ServerConfig.Item.TOOLBELT_PAGES.getAsInt();
        if (stack.is(STItems.NETHERITE_TOOLBELT)) return ServerConfig.Item.NETHERITE_PAGES.getAsInt();
        return 0;
    }
}
