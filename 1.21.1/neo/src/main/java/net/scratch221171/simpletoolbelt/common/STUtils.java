package net.scratch221171.simpletoolbelt.common;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.compat.curios.STCuriosHelper;

public final class STUtils extends STCommonUtils {

    public static Optional<ItemStack> findAnyBelt(Player player) {
        return find(player, () -> STCuriosHelper.getBeltInCurios(player), stack -> true);
    }

    public static Optional<ItemStack> findAnyBeltWithCondition(Player player, Predicate<ItemStack> filter) {
        return find(player, () -> STCuriosHelper.getBeltInCurios(player), filter);
    }

    public static Optional<ItemStack> findBelt(Player player, UUID id) {
        return find(
                player,
                () -> STCuriosHelper.getBeltInCurios(player, id),
                stack -> id.equals(stack.get(STDataComponents.BELT_ID)));
    }

    private static Optional<ItemStack> find(
            Player player, Supplier<Optional<ItemStack>> inCurios, Predicate<ItemStack> filter) {
        ItemStack mainhand = player.getMainHandItem();
        if (mainhand.getItem() instanceof ToolbeltItem && filter.test(mainhand)) return Optional.of(mainhand);
        Optional<ItemStack> curios = ModList.get().isLoaded("curios") ? inCurios.get() : Optional.empty();
        return curios.or(() -> {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.getItem() instanceof ToolbeltItem && filter.test(stack)) {
                    return Optional.of(stack);
                }
            }
            return Optional.empty();
        });
    }
}
