package net.scratch221171.simpletoolbelt.compat.curios;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class STCuriosHelper {

    public static Optional<ItemStack> getBeltInCurios(LivingEntity livingEntity) {
        return CuriosApi.getCuriosInventory(livingEntity).flatMap(iCuriosItemHandler -> iCuriosItemHandler
                .findFirstCurio(stack -> stack.getItem() instanceof ToolbeltItem)
                .map(SlotResult::stack));
    }

    public static Optional<ItemStack> getBeltInCurios(LivingEntity livingEntity, UUID uuid) {
        return CuriosApi.getCuriosInventory(livingEntity).flatMap(iCuriosItemHandler -> iCuriosItemHandler
                .findFirstCurio(stack ->
                        stack.getItem() instanceof ToolbeltItem && uuid.equals(stack.get(STDataComponents.BELT_ID)))
                .map(SlotResult::stack));
    }
}
