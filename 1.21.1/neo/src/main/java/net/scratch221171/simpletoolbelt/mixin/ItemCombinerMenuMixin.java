package net.scratch221171.simpletoolbelt.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.scratch221171.simpletoolbelt.common.menu.WrapperMenuFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 金床・鍛冶台は静的ヘルパーを使わず、自前でaccess.evaluateしてstillValidを判定しているため別途回避する。
 */
@Mixin(ItemCombinerMenu.class)
public abstract class ItemCombinerMenuMixin {

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void simpletoolbelt$bypassStillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (WrapperMenuFlag.is(this)) {
            cir.setReturnValue(true);
        }
    }
}
