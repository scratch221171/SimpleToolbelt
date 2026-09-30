package net.scratch221171.simpletoolbelt.compat.curios;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.common.registry.STDataComponents;
import net.scratch221171.simpletoolbelt.common.registry.STItems;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class STCuriosHelper {

    public static Optional<ItemStack> getBeltInCurios(LivingEntity livingEntity) {
        return CuriosApi.getCuriosInventory(livingEntity).flatMap(iCuriosItemHandler -> iCuriosItemHandler
                .findFirstCurio(STItems.TOOLBELT.get())
                .map(SlotResult::stack));
    }

    public static boolean hasBeltInCurios(LivingEntity livingEntity, UUID uuid) {
        return CuriosApi.getCuriosInventory(livingEntity)
                .map(iCuriosItemHandler -> iCuriosItemHandler.isEquipped(
                        stack -> stack.is(STItems.TOOLBELT.get()) && uuid.equals(stack.get(STDataComponents.BELT_ID))))
                .orElse(false);
    }
}
