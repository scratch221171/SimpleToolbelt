package net.scratch221171.simpletoolbelt.common.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.menu.WrapperMenuFlag;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 * アイテムを1つだけ入れられるラッパー。
 * 中身が「BlockState#getMenuProvider が非nullを返すブロックのアイテム」なら、右クリックでそのメニューを開く。
 */
public class MenuOpenerItem extends Item {

    public MenuOpenerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack getContained(ItemStack wrapper) {
        return wrapper.getOrDefault(STDataComponents.WRAPPED_ITEM.get(), ItemContainerContents.EMPTY)
                .copyOne();
    }

    public static void setContained(ItemStack wrapper, ItemStack stack) {
        if (stack.isEmpty()) {
            wrapper.remove(STDataComponents.WRAPPED_ITEM.get());
        } else {
            wrapper.set(
                    STDataComponents.WRAPPED_ITEM.get(),
                    ItemContainerContents.fromItems(List.of(stack.copyWithCount(1))));
        }
    }

    public static ItemStack getDisplayStack(ItemStack stack) {
        if (stack.getItem() instanceof MenuOpenerItem) {
            ItemStack inner = getContained(stack);
            if (!inner.isEmpty()) return inner;
        }
        return stack;
    }

    /**
     * ブロックアイテムからメニューを取得する。取得できなければnull(=対応外)。
     * 位置はプレイヤーの足元を使う。ブロックはその位置に存在しないため、
     * 開いた後のstillValidはMixin(WrapperMenuFlag)で回避する。
     */
    @Nullable public static MenuProvider findMenuProvider(Level level, Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return null;
        try {
            return blockItem.getBlock().defaultBlockState().getMenuProvider(level, player.blockPosition());
        } catch (RuntimeException e) {
            // 他modのgetMenuProviderが位置にBEなどを要求して例外を投げた場合は非対応扱い
            return null;
        }
    }

    public static boolean canWrap(Player player, ItemStack stack) {
        return findMenuProvider(player.level(), player, stack) != null;
    }

    @Override
    public @NonNull InteractionResultHolder<ItemStack> use(
            @NonNull Level level, Player player, @NonNull InteractionHand hand) {
        ItemStack wrapper = player.getItemInHand(hand);
        ItemStack inner = getContained(wrapper);
        if (inner.isEmpty()) return InteractionResultHolder.pass(wrapper);

        MenuProvider provider = findMenuProvider(level, player, inner);
        if (provider == null) return InteractionResultHolder.pass(wrapper);

        if (level.isClientSide) {
            // クライアントは腕を振るだけ
            return InteractionResultHolder.sidedSuccess(wrapper, true);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.openMenu(provider).isPresent()
                    && serverPlayer.containerMenu instanceof WrapperMenuFlag flag) {
                flag.simpletoolbelt$setWrapperMenu(true);
            }
        }
        return InteractionResultHolder.sidedSuccess(wrapper, false);
    }

    public static void use(ServerLevel level, ServerPlayer player, ItemStack wrapper) {
        ItemStack inner = getContained(wrapper);
        if (inner.isEmpty()) return;
        MenuProvider provider = findMenuProvider(level, player, inner);
        if (provider == null) return;
        if (player.openMenu(provider).isPresent() && player.containerMenu instanceof WrapperMenuFlag flag) {
            flag.simpletoolbelt$setWrapperMenu(true);
        }
    }

    /** オープナーをカーソルで持ち、スロットを右クリックした時 */
    @Override
    public boolean overrideStackedOnOther(
            @NonNull ItemStack wrapper, @NonNull Slot slot, @NonNull ClickAction action, @NonNull Player player) {
        if (action != ClickAction.SECONDARY) return false;

        ItemStack inner = getContained(wrapper);
        ItemStack target = slot.getItem();

        if (target.isEmpty()) {
            // 空スロットへ中身を出す
            if (!inner.isEmpty()) {
                ItemStack rest = slot.safeInsert(inner);
                setContained(wrapper, rest);
                playRemoveSound(player);
            }
        } else if (inner.isEmpty() && canWrap(player, target)) {
            // スロットのアイテムを1つ取り込む
            ItemStack taken = slot.safeTake(1, 1, player);
            if (!taken.isEmpty()) {
                setContained(wrapper, taken);
                playInsertSound(player);
            }
        }
        return true;
    }

    /** スロット内のオープナーを、アイテムを持ったカーソルで右クリックした時 */
    @Override
    public boolean overrideOtherStackedOnMe(
            @NonNull ItemStack wrapper,
            @NonNull ItemStack other,
            @NonNull Slot slot,
            @NonNull ClickAction action,
            @NonNull Player player,
            @NonNull SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;

        ItemStack inner = getContained(wrapper);
        if (other.isEmpty()) {
            // カーソルが空なら中身を取り出す
            if (!inner.isEmpty()) {
                access.set(inner);
                setContained(wrapper, ItemStack.EMPTY);
                playRemoveSound(player);
            }
        } else if (inner.isEmpty() && canWrap(player, other)) {
            // カーソルのアイテムを1つ取り込む
            setContained(wrapper, other.split(1));
            playInsertSound(player);
        }
        return true;
    }

    private static void playInsertSound(Player player) {
        player.playSound(
                SoundEvents.BUNDLE_INSERT,
                0.8F,
                0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playRemoveSound(Player player) {
        player.playSound(
                SoundEvents.BUNDLE_REMOVE_ONE,
                0.8F,
                0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    @Override
    public void appendHoverText(
            @NonNull ItemStack stack,
            @NonNull TooltipContext context,
            @NonNull List<Component> tooltip,
            @NonNull TooltipFlag flag) {
        ItemStack inner = getContained(stack);
        tooltip.add(Component.translatable(Const.LangKey.Item.MENU_OPENER_HINT).withStyle(ChatFormatting.GRAY));
        if (inner.isEmpty()) {
            tooltip.add(Component.translatable(Const.LangKey.Item.EMPTY).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable(Const.LangKey.Item.CONTAINS, inner.getHoverName())
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
