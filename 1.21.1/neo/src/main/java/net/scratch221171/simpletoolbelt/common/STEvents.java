package net.scratch221171.simpletoolbelt.common;

import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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
            if (state.getDestroySpeed(event.getLevel(), event.getPos()) > 0 && !tool.isCorrectToolForDrops(state)) {
                ToolbeltStorage.findAnyNetheriteBelt(player).ifPresent(belt -> {
                    ToolbeltStorage storage = ToolbeltStorage.get(player.server);
                    UUID beltId = storage.ensureId(player, belt);
                    ToolbeltContents stored = storage.resolve(player, beltId, belt);
                    for (int i = 0; i < stored.totalSlots(); i++) {
                        if (stored.getCurrentFlat(i).isCorrectToolForDrops(state)) {
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
    }
}
