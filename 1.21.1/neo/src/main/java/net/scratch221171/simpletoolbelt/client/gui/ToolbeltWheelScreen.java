package net.scratch221171.simpletoolbelt.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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

    private static final Map<UUID, Integer> LAST_PAGE = new HashMap<>();

    private final float wheelRadius, innerDeadzoneRadius, outerStowZoneRadius;
    private HoverState hoverState = HoverState.noAction();
    private int pageIndex;
    private final UUID beltId;
    private int tickCount = 0;
    private double scrollAccumulator = 0;

    public ToolbeltWheelScreen(UUID uuid) {
        super(Component.translatable(Const.LangKey.Screen.WHEEL_SCREEN_TITLE));
        this.beltId = uuid;
        this.pageIndex =
                ClientConfig.Screen.ToolbeltWheel.REMEMBER_PAGE.getAsBoolean() ? LAST_PAGE.getOrDefault(uuid, 0) : 0;
        this.wheelRadius = (float) ClientConfig.Screen.ToolbeltWheel.WHEEL_RADIUS.getAsDouble();
        this.innerDeadzoneRadius = (float) ClientConfig.Screen.ToolbeltWheel.INNER_DEADZONE_RADIUS.getAsDouble();
        this.outerStowZoneRadius = (float) ClientConfig.Screen.ToolbeltWheel.OUTER_STOW_ZONE_RADIUS.getAsDouble();
    }

    @Override
    public void tick() {
        tickCount++;
    }

    @Override
    public boolean isPauseScreen() {
        return ClientConfig.Screen.ToolbeltWheel.PAUSE_GAME.getAsBoolean();
    }

    @Override
    public void removed() {
        LAST_PAGE.put(beltId, pageIndex);
        super.removed();
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = this.minecraft;
        if (mc == null || mc.player == null) return;
        if (ToolbeltStorage.findBelt(mc.player, this.beltId).isEmpty()) {
            this.onClose();
            return;
        }

        if (ClientConfig.Screen.ToolbeltWheel.ENABLE_BLUR.getAsBoolean()) {
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

        final HoverState prevHoverState = hoverState;
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
        if (!hoverState.equals(prevHoverState)) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), 2f, 0.1f));
        }

        ClientBeltCache.read(beltId).ifPresent(cached -> {
            if (cached.pagesSize() > 0) {
                if (pageIndex >= cached.pagesSize()) {
                    pageIndex = Math.max(0, cached.pagesSize() - 1);
                }
                if (cached.pagesSize() > 1) {
                    Component text = Component.translatable(
                            Const.LangKey.Screen.WHEEL_PAGE_INDEX, pageIndex + 1, cached.pagesSize());
                    guiGraphics.drawString(
                            font, text, centerX - font.width(text) / 2, centerY - font.lineHeight / 2, 16777215);
                }

                PoseStack pose = guiGraphics.pose();
                ItemStack selected = ItemStack.EMPTY;

                double progress = Math.clamp((tickCount + partialTick) / 10, 0, 1);
                double animatedRad = wheelRadius - 5 * (Math.pow(1 - progress, 8));
                for (int i = 0; i < ToolbeltContents.PAGE_SIZE; i++) {
                    double angleRad = Math.toRadians(i * 45);
                    double x = centerX + (animatedRad * Math.sin(angleRad)) - ICON_SIZE / 2.0;
                    double y = centerY - (animatedRad * Math.cos(angleRad)) - ICON_SIZE / 2.0;
                    boolean isSelected =
                            hoverState.action() == HoverState.HoverAction.SELECT && i == hoverState.index();
                    ItemStack init = cached.getPage(pageIndex).initial().getStack(i);
                    ItemStack cur = cached.getPage(pageIndex).current().getStack(i);

                    pose.pushPose();
                    pose.translate(x, y, 0);

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    if (cur.isEmpty()) {
                        if (init.isEmpty()) {

                            guiGraphics.blit(
                                    EMPTY_SPRITE, -(10 - ICON_SIZE) / 2, -(10 - ICON_SIZE) / 2, 0, 0, 10, 10, 10, 10);
                        } else {
                            guiGraphics.blit(
                                    FRAME_SPRITE, -(22 - ICON_SIZE) / 2, -(22 - ICON_SIZE) / 2, 0, 0, 22, 22, 22, 22);
                            GhostItemRenderer.render(guiGraphics, font, init, 0, 0);
                        }
                    } else {
                        if (ClientConfig.Screen.ToolbeltWheel.SHOW_KEY_BINDINGS.getAsBoolean()) {
                            renderKeyLabel(guiGraphics, mc, i, 0, 0);
                        }
                        guiGraphics.blit(
                                FRAME_SPRITE, -(22 - ICON_SIZE) / 2, -(22 - ICON_SIZE) / 2, 0, 0, 22, 22, 22, 22);
                        if (isSelected) {
                            guiGraphics.blit(
                                    SELECTED_FRAME_SPRITE,
                                    -(24 - ICON_SIZE) / 2,
                                    -(24 - ICON_SIZE) / 2,
                                    0,
                                    0,
                                    24,
                                    24,
                                    24,
                                    24);
                        }
                        guiGraphics.renderItem(cur, 0, 0);
                        guiGraphics.renderItemDecorations(this.font, cur, 0, 0);
                        if (isSelected) {
                            selected = cur;
                        }
                    }

                    pose.popPose();
                    RenderSystem.disableBlend();
                }

                if (!selected.isEmpty()) {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, 100);
                    guiGraphics.renderTooltip(this.font, selected, mouseX, mouseY);
                    guiGraphics.pose().popPose();
                }
            }
        });
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Minecraft mc = this.minecraft;
        if (mc != null) {
            KeyMapping[] hotbar = mc.options.keyHotbarSlots;
            int count = Math.min(ToolbeltContents.PAGE_SIZE, hotbar.length);
            for (int i = 0; i < count; i++) {
                if (hotbar[i].matches(keyCode, scanCode)) {
                    final int index = i;
                    if (ClientBeltCache.read(beltId)
                            .filter(c -> pageIndex < c.pagesSize())
                            .map(c -> !c.getPage(pageIndex)
                                    .current()
                                    .getStack(index)
                                    .isEmpty())
                            .orElse(false)) {
                        PacketDistributor.sendToServer(
                                new SelectBeltSlotPayload(beltId, pageIndex * ToolbeltContents.PAGE_SIZE + i));
                        this.onClose();
                    }
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 2 && pageIndex != 0) {
            tickCount = 0;
            pageIndex = 0;
            Optional.ofNullable(this.minecraft).ifPresent(minecraft -> minecraft
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), 1.5f, 0.1f)));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (scrollAccumulator != 0 && Math.signum(scrollY) != Math.signum(scrollAccumulator)) {
            scrollAccumulator = 0;
        }
        scrollAccumulator += scrollY;
        int steps = (int) scrollAccumulator;
        if (steps == 0) {
            return true;
        }
        scrollAccumulator -= steps;

        int pages =
                ClientBeltCache.read(beltId).map(ToolbeltContents::pagesSize).orElse(0);
        if (pages > 1) {
            tickCount = 0;
            pageIndex = Math.floorMod(pageIndex + (steps > 0 ? -1 : 1), pages);
            Optional.ofNullable(this.minecraft).ifPresent(minecraft -> minecraft
                    .getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT.value(), 1.5f, 0.1f)));
        }
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (STKeyMappings.OPEN_WHEEL.matches(keyCode, scanCode)) {
            confirmSelection();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // サイドボタン対応
        if (STKeyMappings.OPEN_WHEEL.matchesMouse(button)) {
            confirmSelection();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void confirmSelection() {
        if (hoverState.action() == HoverState.HoverAction.SELECT) {
            PacketDistributor.sendToServer(
                    new SelectBeltSlotPayload(beltId, pageIndex * ToolbeltContents.PAGE_SIZE + hoverState.index()));
        } else if (hoverState.action() == HoverState.HoverAction.STOW) {
            PacketDistributor.sendToServer(new SelectBeltSlotPayload(beltId, SelectBeltSlotPayload.STOW_INDEX));
        }
        this.onClose();
    }

    private void renderKeyLabel(GuiGraphics g, Minecraft mc, int index, int slotX, int slotY) {
        Component label = mc.options.keyHotbarSlots[index].getTranslatedKeyMessage();

        int width = mc.font.width(label);
        int height = mc.font.lineHeight;

        g.pose().pushPose();
        g.pose().translate(slotX - ICON_SIZE / 2f + 6, slotY - ICON_SIZE / 2f, 0f);
        g.pose().scale(0.75f, 0.75f, 1f);
        TooltipRenderUtil.renderTooltipBackground(
                g, -width, 0, width, height, 400, 0xF0100010, 0xF0100010, 0x505000FF, 0x5028007f);
        g.pose().translate(0f, 0f, 400f);
        g.drawString(font, label, -width, 0, 0xFFFFFFFF);
        g.pose().popPose();
    }

    private void renderHoverHighlight(
            GuiGraphics guiGraphics, int centerX, int centerY, float stowThreshold, boolean stowHover) {
        if (stowHover) {
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

    private static void drawAnnularSector(
            GuiGraphics guiGraphics,
            float cx,
            float cy,
            float rIn,
            float rOut,
            float startDeg,
            float endDeg,
            int segments) {
        guiGraphics.flush();
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

    public record HoverState(HoverAction action, int index) {

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
