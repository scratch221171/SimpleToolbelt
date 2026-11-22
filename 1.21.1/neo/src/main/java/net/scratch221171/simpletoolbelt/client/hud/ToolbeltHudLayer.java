package net.scratch221171.simpletoolbelt.client.hud;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.scratch221171.simpletoolbelt.client.gui.GhostItemRenderer;
import net.scratch221171.simpletoolbelt.client.gui.ToolbeltRadialMenuScreen;
import net.scratch221171.simpletoolbelt.client.keymap.ToolbeltKeyMap;
import net.scratch221171.simpletoolbelt.client.network.ClientBeltCache;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.item.MenuOpenerItem;
import net.scratch221171.simpletoolbelt.common.network.SelectBeltSlotPayload;
import net.scratch221171.simpletoolbelt.config.ClientConfig;
import org.jspecify.annotations.NonNull;

public final class ToolbeltHudLayer implements LayeredDraw.Layer {
    public static final ResourceLocation ID = STUtils.id("toolbelt_hud");
    private static final int ITEM = 16;
    private static final int PITCH = 21;

    @Override
    public void render(@NonNull GuiGraphics g, @NonNull DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || mc.screen instanceof ToolbeltRadialMenuScreen) return;
        if (!ClientConfig.Appearance.Hud.ENABLED.getAsBoolean()) return;

        var entries = ToolbeltUsageHistory.getRecentKeys(
                ClientConfig.Appearance.Hud.LENGTH.getAsInt(),
                k -> ToolbeltKeyMap.get(k)
                        .filter(r -> r.slot() != SelectBeltSlotPayload.STOW_INDEX)
                        .flatMap(r -> ClientBeltCache.read(r.beltId()))
                        .isPresent());
        if (entries.isEmpty()) return;

        // ホットバー左端: w/2 - 91, オフハンド: 29
        int right = g.guiWidth() / 2 - 91 - 29 - 7 + ClientConfig.Appearance.Hud.X_OFFSET.getAsInt();
        int y = g.guiHeight() - 19 + ClientConfig.Appearance.Hud.Y_OFFSET.getAsInt();

        var pose = g.pose();
        for (int i = 0; i < entries.size(); i++) {
            var key = entries.get(i);
            var refOpt = ToolbeltKeyMap.get(key);
            if (refOpt.isEmpty()) continue;
            var ref = refOpt.get();
            if (ref.slot() == SelectBeltSlotPayload.STOW_INDEX) return;

            var contentsOpt = ClientBeltCache.read(ref.beltId()); // optional
            if (contentsOpt.isEmpty()) continue;
            var initStack = contentsOpt.get().getInitial(ref.slot());
            var curStack = contentsOpt.get().getCurrent(ref.slot());

            int x = right - (entries.size() - i) * PITCH + 3; // +3: 枠の左端からアイテム原点まで

            boolean isSelected = !initStack.isEmpty()
                    && curStack.isEmpty()
                    && STUtils.isSameItem(initStack, player.getMainHandItem());

            pose.pushPose();
            pose.translate(x, y, 0);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            if (curStack.isEmpty()) {
                if (initStack.isEmpty()) {
                    g.blit(
                            ToolbeltRadialMenuScreen.EMPTY_SPRITE,
                            -(10 - ITEM) / 2,
                            -(10 - ITEM) / 2,
                            0,
                            0,
                            10,
                            10,
                            10,
                            10);
                } else {
                    g.blit(
                            ToolbeltRadialMenuScreen.FRAME_SPRITE,
                            -(22 - ITEM) / 2,
                            -(22 - ITEM) / 2,
                            0,
                            0,
                            22,
                            22,
                            22,
                            22);
                    GhostItemRenderer.render(g, initStack, 0, 0, false);
                }
            } else {
                g.blit(ToolbeltRadialMenuScreen.FRAME_SPRITE, -(22 - ITEM) / 2, -(22 - ITEM) / 2, 0, 0, 22, 22, 22, 22);

                ItemStack displayStack = MenuOpenerItem.getDisplayStack(curStack);
                g.renderItem(displayStack, 0, 0);
                g.renderItemDecorations(mc.font, displayStack, 0, 0);
            }

            if (isSelected) {
                g.blit(
                        ToolbeltRadialMenuScreen.SELECTED_FRAME_SPRITE,
                        -(24 - ITEM) / 2,
                        -(24 - ITEM) / 2,
                        0,
                        0,
                        24,
                        24,
                        24,
                        24);
            }

            ToolbeltRadialMenuScreen.renderKeyLabel(g, mc, key.getDisplayName(), 0, 0);
            pose.popPose();
            RenderSystem.disableBlend();
        }
    }
}
