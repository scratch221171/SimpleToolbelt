package net.scratch221171.simpletoolbelt.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import net.scratch221171.simpletoolbelt.common.menu.WrapperMenuFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin implements WrapperMenuFlag {

    @Unique private boolean simpletoolbelt$wrapperMenu;

    @Override
    public boolean simpletoolbelt$isWrapperMenu() {
        return simpletoolbelt$wrapperMenu;
    }

    @Override
    public void simpletoolbelt$setWrapperMenu(boolean value) {
        simpletoolbelt$wrapperMenu = value;
    }

    /**
     * 作業台・砥石・石切台・機織り機・製図台などが使う静的ヘルパー。
     * ブロックの有無と距離の検査を、ラッパー経由のメニューに限って無効化する。
     */
    @Inject(
            method =
                    "stillValid(Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/Block;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void simpletoolbelt$bypassStillValid(
            ContainerLevelAccess access, Player player, Block block, CallbackInfoReturnable<Boolean> cir) {
        if (WrapperMenuFlag.is(player.containerMenu)) {
            cir.setReturnValue(true);
        }
    }
}
