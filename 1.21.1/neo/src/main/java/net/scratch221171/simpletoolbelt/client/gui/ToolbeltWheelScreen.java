package net.scratch221171.simpletoolbelt.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.STKeyMappings;
import net.scratch221171.simpletoolbelt.client.network.ClientBeltCache;
import net.scratch221171.simpletoolbelt.common.STUtils;
import net.scratch221171.simpletoolbelt.common.network.SelectBeltSlotPayload;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltStorage;
import net.scratch221171.simpletoolbelt.config.ClientConfig;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;

public class ToolbeltWheelScreen extends Screen {

    private static final int ICON_SIZE = 16;
    private static final int HIGHLIGHT_COLOR = 0x50FFFFFF;
    private static final int SECTOR_SEGMENTS = 12;
    private static final int STOW_SEGMENTS = 48;

    private static final ResourceLocation SELECTED_FRAME_SPRITE = STUtils.id("textures/hud/wheel_selected_frame.png");
    private static final ResourceLocation FRAME_SPRITE = STUtils.id("textures/hud/wheel_frame.png");
    private static final ResourceLocation EMPTY_SPRITE = STUtils.id("textures/hud/wheel_empty.png");

    private final UUID beltId;
    private final float wheelRadius, innerDeadzoneRadius, outerStowZoneRadius;
    private double scrollAccumulator = 0;

    private HoverState hoverState = HoverState.noAction();
    private int pageIndex = 0;

    public ToolbeltWheelScreen(UUID uuid) {
        super(Component.translatable(Const.LangKey.Screen.WHEEL_SCREEN_TITLE));
        this.beltId = uuid;
        this.wheelRadius = (float) ClientConfig.Screen.ToolbeltWheel.WHEEL_RADIUS.getAsDouble();
        this.innerDeadzoneRadius = (float) ClientConfig.Screen.ToolbeltWheel.INNER_DEADZONE_RADIUS.getAsDouble();
        this.outerStowZoneRadius = (float) ClientConfig.Screen.ToolbeltWheel.OUTER_STOW_ZONE_RADIUS.getAsDouble();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = this.minecraft;
        if (mc == null || mc.player == null) return;
        if (ToolbeltStorage.findBelt(mc.player, this.beltId).isEmpty()) {
            this.onClose();
            return;
        }

        if (ClientConfig.Screen.ToolbeltWheel.ENABLE_HIGHLIGHT.getAsBoolean()) {
            renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            renderMenuBackground(guiGraphics);
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float stowThreshold = Math.min(outerStowZoneRadius, Math.min(this.width, this.height) * 0.45f);

        boolean stowHover = distance > stowThreshold;
        if (ClientConfig.Screen.ToolbeltWheel.ENABLE_HIGHLIGHT.getAsBoolean()) {
            renderHoverHighlight(guiGraphics, centerX, centerY, stowThreshold, stowHover);
        }
        if (stowHover) {
            hoverState = HoverState.stow();
            guiGraphics.renderTooltip(
                    font, Component.translatable(Const.LangKey.Screen.WHEEL_TOOLTIP_STOW), mouseX, mouseY);
        } else if (distance < innerDeadzoneRadius) {
            hoverState = HoverState.noAction();
        } else {
            double angleDeg = Math.toDegrees(Math.atan2(dx, -dy));
            if (angleDeg < 0) angleDeg += 360;
            hoverState =
                    HoverState.select(Math.floorMod(Math.round((float) angleDeg / 45f), ToolbeltContents.PAGE_SIZE));
        }

        ClientBeltCache.read(beltId).ifPresent(cached -> {
            if (cached.pagesSize() > 0) {
                if (cached.pagesSize() > 1) {
                    Component text = Component.translatable(
                            Const.LangKey.Screen.WHEEL_PAGE_INDEX, pageIndex + 1, cached.pagesSize());
                    guiGraphics.drawString(
                            font, text, centerX - font.width(text) / 2, centerY - font.lineHeight / 2, 16777215);
                }
                for (int i = 0; i < ToolbeltContents.PAGE_SIZE; i++) {
                    double angleRad = Math.toRadians(i * 45.0);
                    int x = centerX + (int) (wheelRadius * Math.sin(angleRad)) - ICON_SIZE / 2;
                    int y = centerY - (int) (wheelRadius * Math.cos(angleRad)) - ICON_SIZE / 2;
                    boolean isSelected =
                            hoverState.action() == HoverState.HoverAction.SELECT && i == hoverState.index();
                    ItemStack init = cached.getPage(pageIndex).initial().getStack(i);
                    ItemStack cur = cached.getPage(pageIndex).current().getStack(i);

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    if (cur.isEmpty()) {
                        if (init.isEmpty()) {
                            guiGraphics.blit(
                                    EMPTY_SPRITE,
                                    x - (10 - ICON_SIZE) / 2,
                                    y - (10 - ICON_SIZE) / 2,
                                    0,
                                    0,
                                    10,
                                    10,
                                    10,
                                    10);
                        } else {
                            guiGraphics.blit(
                                    FRAME_SPRITE,
                                    x - (22 - ICON_SIZE) / 2,
                                    y - (22 - ICON_SIZE) / 2,
                                    0,
                                    0,
                                    22,
                                    22,
                                    22,
                                    22);
                            GhostItemRenderer.render(guiGraphics, font, init, x, y);
                        }
                    } else {
                        guiGraphics.blit(
                                FRAME_SPRITE, x - (22 - ICON_SIZE) / 2, y - (22 - ICON_SIZE) / 2, 0, 0, 22, 22, 22, 22);
                        if (isSelected) {
                            guiGraphics.blit(
                                    SELECTED_FRAME_SPRITE,
                                    x - (24 - ICON_SIZE) / 2,
                                    y - (24 - ICON_SIZE) / 2,
                                    0,
                                    0,
                                    24,
                                    24,
                                    24,
                                    24);
                        }
                        guiGraphics.renderItem(cur, x, y);
                        guiGraphics.renderItemDecorations(this.font, cur, x, y);
                        if (isSelected) {
                            guiGraphics.renderTooltip(this.font, cur, mouseX, mouseY);
                        }
                    }
                    RenderSystem.disableBlend();
                }
            }
        });
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (STKeyMappings.OPEN_WHEEL.matches(keyCode, scanCode)) {
            if (hoverState.action() == HoverState.HoverAction.SELECT) {
                PacketDistributor.sendToServer(
                        new SelectBeltSlotPayload(beltId, pageIndex * ToolbeltContents.PAGE_SIZE + hoverState.index()));
            } else if (hoverState.action() == HoverState.HoverAction.STOW) {
                PacketDistributor.sendToServer(new SelectBeltSlotPayload(beltId, SelectBeltSlotPayload.STOW_INDEX));
            }
            this.onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        // 向きが変わったら溜めた分を捨てる
        if (scrollAccumulator != 0 && Math.signum(scrollY) != Math.signum(scrollAccumulator)) {
            scrollAccumulator = 0;
        }
        scrollAccumulator += scrollY;
        int steps = (int) scrollAccumulator; // 0方向へ切り捨て
        if (steps == 0) {
            return true;
        }
        scrollAccumulator -= steps;

        int pages =
                ClientBeltCache.read(beltId).map(ToolbeltContents::pagesSize).orElse(0);
        if (pages > 1) {
            pageIndex = Math.floorMod(pageIndex + (steps > 0 ? -1 : 1), pages);
        }
        return true;
    }

    private void renderHoverHighlight(
            GuiGraphics guiGraphics, int centerX, int centerY, float stowThreshold, boolean stowHover) {
        if (stowHover) {
            // 画面の対角長を外半径にすれば、円の外側は画面全体をカバーできる
            float far = (float) Math.hypot(this.width, this.height);
            drawAnnularSector(guiGraphics, centerX, centerY, stowThreshold, far, 0f, 360f, STOW_SEGMENTS);
        } else if (hoverState.action() == HoverState.HoverAction.SELECT) {
            float center = hoverState.index() * 45f;
            drawAnnularSector(
                    guiGraphics,
                    centerX,
                    centerY,
                    innerDeadzoneRadius,
                    stowThreshold,
                    center - 22.5f,
                    center + 22.5f,
                    SECTOR_SEGMENTS);
        }
    }

    /** 角度は12時方向を0°として時計回り。rIn〜rOut の環状扇形を1回の描画で塗る */
    private static void drawAnnularSector(
            GuiGraphics guiGraphics,
            float cx,
            float cy,
            float rIn,
            float rOut,
            float startDeg,
            float endDeg,
            int segments) {
        guiGraphics.flush(); // guiGraphics 側に溜まっている描画を先に確定させ、重ね順を保つ
        Matrix4f matrix = guiGraphics.pose().last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buf =
                Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int s = 0; s <= segments; s++) {
            double rad = Math.toRadians(startDeg + (endDeg - startDeg) * s / segments);
            float sin = (float) Math.sin(rad);
            float cos = (float) Math.cos(rad);
            buf.addVertex(matrix, cx + rIn * sin, cy - rIn * cos, 0f).setColor(HIGHLIGHT_COLOR);
            buf.addVertex(matrix, cx + rOut * sin, cy - rOut * cos, 0f).setColor(HIGHLIGHT_COLOR);
        }
        BufferUploader.drawWithShader(buf.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static final class HoverState {

        private final HoverAction action;
        private final int index;

        private HoverState(HoverAction action, int index) {
            this.action = action;
            this.index = index;
        }

        public static HoverState noAction() {
            return new HoverState(HoverAction.NO_ACTION, -1);
        }

        public static HoverState select(int index) {
            return new HoverState(HoverAction.SELECT, index);
        }

        public static HoverState stow() {
            return new HoverState(HoverAction.STOW, -1);
        }

        public HoverAction action() {
            return action;
        }

        public int index() {
            if (action != HoverAction.SELECT) {
                throw new IllegalStateException("Index is only available for SELECT!");
            }

            return index;
        }

        public enum HoverAction {
            NO_ACTION,
            SELECT,
            STOW
        }
    }
}
