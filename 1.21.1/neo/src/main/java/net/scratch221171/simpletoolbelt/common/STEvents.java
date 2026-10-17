package net.scratch221171.simpletoolbelt.common;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.common.item.ToolbeltItem;
import net.scratch221171.simpletoolbelt.common.registry.STAttachments;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;
import net.scratch221171.simpletoolbelt.config.ServerConfig;

@EventBusSubscriber(modid = Const.MOD_ID)
public class STEvents {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (ServerConfig.Item.ALLOW_AUTO_SWITCH.getAsBoolean()
                && event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START
                && event.getEntity() instanceof ServerPlayer player
                && player.getData(STAttachments.AUTO_TOOL_SWITCH)
                && !player.getAbilities().instabuild) {
            ItemStack tool = event.getItemStack();
            BlockState state = event.getLevel().getBlockState(event.getPos());
            float bare = progressWith(player, ItemStack.EMPTY, state, event.getLevel(), event.getPos());
            if (isSuitableTool(player, tool, state, event.getLevel(), event.getPos(), bare)) return;

            ToolbeltStorage.findAnyNetheriteBelt(player).ifPresent(belt -> {
                ToolbeltStorage storage = ToolbeltStorage.get(player.server);

                UUID beltId = storage.ensureId(player, belt);
                ToolbeltContents stored = storage.resolve(player, beltId, belt);
                for (int i = 0; i < stored.totalSlots(); i++) {
                    if (isSuitableTool(
                            player, stored.getCurrentFlat(i), state, event.getLevel(), event.getPos(), bare)) {
                        ToolbeltItem.applySelection(
                                player,
                                beltId,
                                i,
                                Component.translatable(
                                        Const.LangKey.Item.AUTO_SWITCH_FAILED,
                                        Component.translatable(Const.LangKey.Item.INVENTORY_FULL)));
                        return;
                    }
                }
            });
        }
    }

    private static boolean isSuitableTool(
            ServerPlayer player, ItemStack candidate, BlockState state, Level level, BlockPos pos, float bareProgress) {
        if (candidate.isEmpty()) {
            return false;
        }
        if (state.requiresCorrectToolForDrops() && !candidate.isCorrectToolForDrops(state)) {
            return false;
        }
        return progressWith(player, candidate, state, level, pos) > bareProgress;
    }

    private static float progressWith(
            ServerPlayer player, ItemStack tool, BlockState state, Level level, BlockPos pos) {
        Inventory inv = player.getInventory();
        int slot = inv.selected;
        ItemStack original = inv.items.get(slot);
        // BlockState#getDestroyProgressはプレイヤーのmainhandを参照するので、やむを得ず仮配置
        try {
            inv.items.set(slot, tool);
            return state.getDestroyProgress(player, level, pos);
        } finally {
            inv.items.set(slot, original);
        }
    }
}
