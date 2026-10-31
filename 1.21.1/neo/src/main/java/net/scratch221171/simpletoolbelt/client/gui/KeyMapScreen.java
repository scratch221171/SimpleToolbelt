package net.scratch221171.simpletoolbelt.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.scratch221171.simpletoolbelt.Const;
import net.scratch221171.simpletoolbelt.client.keymap.ToolbeltKeyMap;
import net.scratch221171.simpletoolbelt.common.menu.ToolbeltMenu;
import net.scratch221171.simpletoolbelt.common.network.SelectBeltSlotPayload;
import net.scratch221171.simpletoolbelt.common.storage.ToolbeltContents;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

public class KeyMapScreen extends Screen {

    private static final int NONE = Integer.MIN_VALUE;
    private static final int STOW_SLOT = SelectBeltSlotPayload.STOW_INDEX;
    private static final int STOW_GAP = 14;
    private static final int CELL_W = 26, CELL_H = 30, GAP = 2;
    private static final int COL_W = CELL_W + GAP, ROW_H = CELL_H + GAP * 2;
    private static final int HEADER = 28, FOOTER = 24;

    private final Screen parent;
    private final ToolbeltMenu menu;
    private int listening = NONE;
    private int scrollRow = 0;

    public KeyMapScreen(Screen parent, ToolbeltMenu menu) {
        super(Component.translatable(Const.LangKey.Screen.KEYMAP_TITLE));
        this.parent = parent;
        this.menu = menu;
    }

    @Override
    protected void init() {
        super.init();
        scrollRow = Mth.clamp(scrollRow, 0, maxScroll());
    }

    private int visibleRows() {
        return Math.clamp(menu.getPages(), 1, (height - HEADER - FOOTER) / ROW_H);
    }

    private int maxScroll() {
        return Math.max(0, menu.getPages() - visibleRows());
    }

    private int gridLeft() {
        return (width - (ToolbeltContents.PAGE_SIZE * COL_W - GAP)) / 2;
    }

    private int stowX() {
        return gridLeft() - STOW_GAP * 2 - CELL_W;
    }

    /** マウス位置のセル → スロット番号（なければNONE） */
    private int cellAt(double mx, double my) {

        int ix = Mth.floor(mx), iy = Mth.floor(my);
        if (ix >= stowX() && ix < stowX() + CELL_W && iy >= HEADER && iy < HEADER + CELL_H) {
            return STOW_SLOT;
        }
        int rx = ix - gridLeft(), ry = iy - HEADER;
        if (rx < 0 || ry < 0) return NONE;
        int col = rx / COL_W, row = ry / ROW_H;
        if (col >= ToolbeltContents.PAGE_SIZE || row >= visibleRows()) return NONE;
        if (rx % COL_W >= CELL_W || ry % ROW_H >= CELL_H) return NONE;
        int slot = (scrollRow + row) * ToolbeltContents.PAGE_SIZE + col;
        return slot < menu.getClientContents().totalSlots() ? slot : NONE;
    }

    @Override
    public void render(@NonNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);

        ToolbeltContents contents = menu.getClientContents();
        UUID beltId = menu.getBeltId();
        int hovered = cellAt(mouseX, mouseY);

        for (int r = 0; r < visibleRows(); r++) {
            int page = scrollRow + r;
            int y = HEADER + r * ROW_H;
            g.drawString(font, String.valueOf(page + 1), gridLeft() - 15, y + CELL_H / 2 - 4, 0xAAAAAA, false);
            for (int c = 0; c < ToolbeltContents.PAGE_SIZE; c++) {
                int slot = page * ToolbeltContents.PAGE_SIZE + c;
                int x = gridLeft() + c * COL_W;
                renderCell(g, x, y, slot, hovered, beltId);

                ItemStack stack = contents.getInitialFlat(slot);
                if (!stack.isEmpty()) {
                    g.renderItem(stack, x + 5, y + 3);
                }
            }
        }

        renderCell(g, stowX(), HEADER, STOW_SLOT, hovered, beltId);
        g.blit(ToolbeltScreen.STOW, stowX() + 7, HEADER + 5, 0, 0, 12, 12, 12, 12);

        g.drawCenteredString(
                font,
                Component.translatable(Const.LangKey.Screen.KEYMAP_HINT).withStyle(ChatFormatting.GRAY),
                width / 2,
                height - FOOTER + 8,
                0);

        if (hovered != NONE) {
            List<KeyMapping> hits = ToolbeltKeyMap.getKey(beltId, hovered)
                    .map(KeyMapScreen::conflicts)
                    .orElse(List.of());
            if (!hits.isEmpty()) {
                Component tip = Component.translatable("controls.keybinds.duplicateKeybinds", conflictNames(hits))
                        .withStyle(ChatFormatting.RED);
                g.renderTooltip(font, font.split(tip, 170), mouseX, mouseY); // 170 は Tooltip の既定幅
                return;
            }
        }

        if (hovered >= 0) {
            ItemStack stack = contents.getInitialFlat(hovered);
            if (!stack.isEmpty()) {
                g.renderTooltip(font, stack, mouseX, mouseY);
            }
        } else if (hovered == STOW_SLOT) {
            g.renderTooltip(font, Component.translatable(Const.LangKey.Screen.WHEEL_TOOLTIP_STOW), mouseX, mouseY);
        }
    }

    private void renderCell(GuiGraphics g, int x, int y, int slot, int hovered, UUID beltId) {
        int border = slot == listening ? 0xFFFFFF55 : slot == hovered ? 0xFFFFFFFF : 0xFF373737;
        g.fill(x, y, x + CELL_W, y + CELL_H, border);
        g.fill(x + 1, y + 1, x + CELL_W - 1, y + CELL_H - 1, 0xFF8B8B8B);

        Optional<InputConstants.Key> key = ToolbeltKeyMap.getKey(beltId, slot);

        Component bounded = key.map(InputConstants.Key::getDisplayName)
                .map(c -> c.copy().withStyle(ChatFormatting.WHITE))
                .orElse(Component.empty());
        Component label;
        label = slot == listening
                ? Component.literal("> ")
                        .append(bounded.copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                        .append(" <")
                        .withStyle(ChatFormatting.YELLOW)
                : key.filter(k -> !conflicts(k).isEmpty()).isPresent()
                        ? Component.literal("[ ")
                                .append(bounded.copy().withStyle(ChatFormatting.WHITE))
                                .append(" ]")
                                .withStyle(ChatFormatting.RED)
                        : bounded;
        drawFitted(g, label, x + CELL_W / 2f, y + 20);
    }

    /** 長いキー名はセル幅に収まるよう縮小して中央揃えで描画 */
    private void drawFitted(GuiGraphics g, Component text, float cx, float y) {
        int w = Math.max(1, font.width(text));
        float scale = Math.min(1f, (CELL_W - 2f) / w);
        g.pose().pushPose();
        g.pose().translate(cx, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, text, -w / 2, 0, 0xFFFFFF, true);
        g.pose().popPose();
    }

    /** 通常プレイ中（IN_GAME）で、このキーに割り当て済みの他の操作 */
    private static List<KeyMapping> conflicts(InputConstants.Key key) {
        List<KeyMapping> result = new java.util.ArrayList<>();
        for (KeyMapping km : Minecraft.getInstance().options.keyMappings) {
            if (!km.isUnbound()
                    && km.saveString().equals(key.getName())
                    && km.getKeyConflictContext().conflicts(KeyConflictContext.IN_GAME)) {
                result.add(km);
            }
        }
        return result;
    }

    private static Component conflictNames(List<KeyMapping> hits) {
        MutableComponent names = Component.empty();
        for (int i = 0; i < hits.size(); i++) {
            if (i > 0) names.append(", ");
            names.append(hits.get(i).getDisplayName());
        }
        return names;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (listening != NONE) {
            if (button >= GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                ToolbeltKeyMap.bind(InputConstants.Type.MOUSE.getOrCreate(button), menu.getBeltId(), listening);
            }
            listening = NONE;
            return true;
        }
        int slot = cellAt(mouseX, mouseY);
        if (slot != NONE) {
            if (button == 0) {
                listening = slot;
            } else if (button == 1) {
                ToolbeltKeyMap.unbind(menu.getBeltId(), slot);
            }
            Optional.ofNullable(minecraft).ifPresent(mc -> mc.getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1f)));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening != NONE) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                ToolbeltKeyMap.unbind(menu.getBeltId(), listening);
            } else {
                ToolbeltKeyMap.bind(InputConstants.getKey(keyCode, scanCode), menu.getBeltId(), listening);
            }
            listening = NONE;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollRow = Mth.clamp(scrollRow - (int) Math.signum(scrollY), 0, maxScroll());
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
