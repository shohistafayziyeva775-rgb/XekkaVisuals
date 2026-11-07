package com.xekka.visuals.gui;

import com.xekka.visuals.XekkaVisualsClient;
import com.xekka.visuals.config.ConfigManager;
import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.ModuleManager;
import com.xekka.visuals.util.ColorUtils;
import com.xekka.visuals.util.RenderUtils2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Dark "premium ink" panel with a top tab bar + search, two-column module grid with toggle
 *  switches, and a bottom floating dock to switch between categories — matching the reference. */
public class XekkaClickGui extends Screen {
    private static final int PANEL_W = 420;
    private static final int PANEL_H = 300;
    private static final int ROW_H = 26;
    private static final int DOCK_ICONS = 5;

    private Module.Category currentCategory = Module.Category.VISUALS;
    private String search = "";
    private float openAnim = 0f;
    private int dragOffsetX, dragOffsetY;
    private boolean dragging = false;
    private int panelX, panelY;
    private Module expandedModule = null;

    public XekkaClickGui() { super(Text.literal("XekkaVisuals")); }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        openAnim = Math.min(1f, openAnim + delta * 0.12f);
        float eased = RenderUtils2D.ease(openAnim);
        float scale = 0.9f + 0.1f * eased;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(width / 2f, height / 2f, 0);
        ctx.getMatrices().scale(scale, scale, 1f);
        ctx.getMatrices().translate(-width / 2f, -height / 2f, 0);

        renderPanel(ctx, mouseX, mouseY, delta);

        ctx.getMatrices().pop();
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPanel(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long t = System.currentTimeMillis();
        var theme = XekkaVisualsClient.THEME_EDITOR;
        int borderGlow = theme.borderColor(t);
        int accent = theme.accent();
        int panelBg = theme.panelBg();

        RenderUtils2D.glow(ctx, panelX, panelY, PANEL_W, PANEL_H, 14, borderGlow, 4);
        RenderUtils2D.roundedRect(ctx, panelX, panelY, PANEL_W, PANEL_H, 14, panelBg);
        RenderUtils2D.roundedRectBorder(ctx, panelX, panelY, PANEL_W, PANEL_H, 14, 1, ColorUtils.withAlpha(accent, 170));

        // Header: logo + title
        int logoCx = panelX + 20, logoCy = panelY + 20;
        RenderUtils2D.circle(ctx, logoCx, logoCy, 7, accent);
        ctx.drawText(textRenderer, "XekkaVisuals", panelX + 36, panelY + 14, (int) ColorUtils.THEME_TEXT, false);

        // Tab row: category name top-left, search top-right
        int tabY = panelY + 34;
        ctx.drawText(textRenderer, currentCategory.display, panelX + 16, tabY + 6, (int) ColorUtils.THEME_TEXT_DIM, false);
        int searchX = panelX + PANEL_W - 140;
        RenderUtils2D.roundedRect(ctx, searchX, tabY, 124, 16, 6, (int) ColorUtils.THEME_BG_LIGHT);
        String searchLabel = search.isEmpty() ? "Search..." : search;
        int searchColor = search.isEmpty() ? (int) ColorUtils.THEME_TEXT_DIM : (int) ColorUtils.THEME_TEXT;
        ctx.drawText(textRenderer, searchLabel, searchX + 6, tabY + 4, searchColor, false);

        // Module grid (two columns)
        List<Module> modules = filteredModules();
        int gridTop = tabY + 26;
        if (currentCategory == Module.Category.MISC) {
            gridTop = renderPresetBar(ctx, tabY + 24, mouseX, mouseY) + 6;
        }
        int colW = (PANEL_W - 32 - 8) / 2;
        for (int i = 0; i < modules.size(); i++) {
            int col = i % 2, row = i / 2;
            int rx = panelX + 16 + col * (colW + 8);
            int ry = gridTop + row * (ROW_H + 6);
            if (ry + ROW_H > panelY + PANEL_H - 46) break; // reserve space for dock
            renderModuleRow(ctx, modules.get(i), rx, ry, colW, mouseX, mouseY);
        }

        renderDock(ctx, mouseX, mouseY);
    }

    // ---- Config / presets bar (shown on the Misc page) ----
    private String presetNameBuffer = "default";
    private int presetBarY;

    private int renderPresetBar(DrawContext ctx, int y, int mouseX, int mouseY) {
        presetBarY = y;
        int barH = 18;
        int fieldW = PANEL_W - 32 - 3 * 56 - 12;
        int x = panelX + 16;
        RenderUtils2D.roundedRect(ctx, x, y, fieldW, barH, 5, (int) ColorUtils.THEME_BG_LIGHT);
        ctx.drawText(textRenderer, presetNameBuffer, x + 6, y + 5, (int) ColorUtils.THEME_TEXT, false);

        String[] labels = {"Save", "Load", "Del"};
        for (int i = 0; i < 3; i++) {
            int bx = x + fieldW + 6 + i * 58;
            boolean hov = mouseX >= bx && mouseX <= bx + 52 && mouseY >= y && mouseY <= y + barH;
            RenderUtils2D.roundedRect(ctx, bx, y, 52, barH, 5,
                    hov ? XekkaVisualsClient.THEME_EDITOR.accent() : (int) ColorUtils.THEME_BG_LIGHT);
            ctx.drawCenteredTextWithShadow(textRenderer, labels[i], bx + 26, y + 5,
                    hov ? 0xFF000000 : (int) ColorUtils.THEME_TEXT);
        }
        return y + barH;
    }

    private boolean presetBarClicked(double mouseX, double mouseY) {
        if (currentCategory != Module.Category.MISC) return false;
        int barH = 18;
        int fieldW = PANEL_W - 32 - 3 * 56 - 12;
        int x = panelX + 16;
        int y = presetBarY;
        String[] names = {"save", "load", "del"};
        for (int i = 0; i < 3; i++) {
            int bx = x + fieldW + 6 + i * 58;
            if (mouseX >= bx && mouseX <= bx + 52 && mouseY >= y && mouseY <= y + barH) {
                switch (names[i]) {
                    case "save" -> ConfigManager.savePreset(presetNameBuffer);
                    case "load" -> ConfigManager.loadPreset(presetNameBuffer);
                    case "del" -> ConfigManager.deletePreset(presetNameBuffer);
                }
                return true;
            }
        }
        return false;
    }

    private void renderModuleRow(DrawContext ctx, Module m, int x, int y, int w, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + ROW_H;
        int bg = hovered ? (int) ColorUtils.THEME_BG_LIGHT : ColorUtils.withAlpha((int) ColorUtils.THEME_BG_LIGHT, 140);
        RenderUtils2D.roundedRect(ctx, x, y, w, ROW_H, 8, bg);
        ctx.drawText(textRenderer, m.getName(), x + 10, y + 9, (int) ColorUtils.THEME_TEXT, false);

        // toggle switch on the right
        int swW = 28, swH = 14;
        int swX = x + w - swW - 8, swY = y + (ROW_H - swH) / 2;
        int trackColor = m.isEnabled() ? XekkaVisualsClient.THEME_EDITOR.toggleOn() : (int) ColorUtils.THEME_OFF;
        RenderUtils2D.roundedRect(ctx, swX, swY, swW, swH, swH / 2f, trackColor);
        int knobX = m.isEnabled() ? swX + swW - swH : swX;
        RenderUtils2D.circle(ctx, knobX + swH / 2f, swY + swH / 2f, swH / 2f - 2, 0xFFFFFFFF);
    }

    private void renderDock(DrawContext ctx, int mouseX, int mouseY) {
        int dockW = 44 * DOCK_ICONS + 8 * (DOCK_ICONS - 1) + 16;
        int dockH = 40;
        int dx = panelX + (PANEL_W - dockW) / 2;
        int dy = panelY + PANEL_H - dockH - 10;
        RenderUtils2D.roundedRect(ctx, dx, dy, dockW, dockH, 18, (int) ColorUtils.THEME_BG_LIGHT);

        Module.Category[] cats = Module.Category.values();
        for (int i = 0; i < DOCK_ICONS && i < cats.length; i++) {
            int ix = dx + 8 + i * (44 + 8);
            int iy = dy + 4;
            boolean selected = cats[i] == currentCategory;
            int bg = selected ? (int) ColorUtils.THEME_ACCENT : 0x00000000;
            RenderUtils2D.roundedRect(ctx, ix, iy, 44, 32, 10, bg);
            String label = cats[i].display.length() > 6 ? cats[i].display.substring(0, 6) : cats[i].display;
            ctx.drawText(textRenderer, label, ix + 4, iy + 11,
                    selected ? 0xFF000000 : (int) ColorUtils.THEME_TEXT_DIM, false);
        }
    }

    private List<Module> filteredModules() {
        List<Module> out = new ArrayList<>();
        for (Module m : ModuleManager.getByCategory(currentCategory)) {
            if (search.isEmpty() || m.getName().toLowerCase().contains(search.toLowerCase())) out.add(m);
        }
        return out;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (presetBarClicked(mouseX, mouseY)) return true;

        // Dock click → switch category
        int dockW = 44 * DOCK_ICONS + 8 * (DOCK_ICONS - 1) + 16;
        int dockH = 40;
        int dx = panelX + (PANEL_W - dockW) / 2;
        int dy = panelY + PANEL_H - dockH - 10;
        Module.Category[] cats = Module.Category.values();
        for (int i = 0; i < DOCK_ICONS && i < cats.length; i++) {
            int ix = dx + 8 + i * (44 + 8);
            if (mouseX >= ix && mouseX <= ix + 44 && mouseY >= dy + 4 && mouseY <= dy + 36) {
                currentCategory = cats[i];
                return true;
            }
        }

        // Module row click → toggle (left click) handled via grid hit-test
        List<Module> modules = filteredModules();
        int tabY = panelY + 34;
        int gridTop = tabY + 26;
        int colW = (PANEL_W - 32 - 8) / 2;
        for (int i = 0; i < modules.size(); i++) {
            int col = i % 2, row = i / 2;
            int rx = panelX + 16 + col * (colW + 8);
            int ry = gridTop + row * (ROW_H + 6);
            if (mouseX >= rx && mouseX <= rx + colW && mouseY >= ry && mouseY <= ry + ROW_H) {
                modules.get(i).toggle();
                ConfigManager.save();
                return true;
            }
        }

        // Header drag start
        if (mouseY >= panelY && mouseY <= panelY + 32) {
            dragging = true;
            dragOffsetX = (int) (mouseX - panelX);
            dragOffsetY = (int) (mouseY - panelY);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            panelX = (int) (mouseX - dragOffsetX);
            panelY = (int) (mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (currentCategory == Module.Category.MISC) presetNameBuffer += chr;
        else search += chr;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 259) { // backspace
            if (currentCategory == Module.Category.MISC && !presetNameBuffer.isEmpty()) {
                presetNameBuffer = presetNameBuffer.substring(0, presetNameBuffer.length() - 1);
                return true;
            } else if (!search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
