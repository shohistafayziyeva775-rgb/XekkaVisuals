package com.xekka.visuals.gui;

import com.xekka.visuals.XekkaVisualsClient;
import com.xekka.visuals.config.ConfigManager;
import com.xekka.visuals.modules.HudElementsModule;
import com.xekka.visuals.util.ColorUtils;
import com.xekka.visuals.util.RenderUtils2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Lets the player drag each enabled HUD panel to a new screen position; press ESC to save+close. */
public class HudEditorScreen extends Screen {
    private static final int BOX_W = 100, BOX_H = 16;
    private HudElementsModule.Element dragging = null;
    private int dragOffX, dragOffY;

    public HudEditorScreen() { super(Text.literal("HUD Editor")); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "HUD Editor — drag panels, ESC to save", width / 2, 10,
                (int) ColorUtils.THEME_TEXT);

        for (var el : XekkaVisualsClient.HUD_ELEMENTS.elements.values()) {
            if (!el.enabled.getValue()) continue;
            int x = el.x.getValue().intValue(), y = el.y.getValue().intValue();
            boolean hovered = mouseX >= x && mouseX <= x + BOX_W && mouseY >= y && mouseY <= y + BOX_H;
            int bg = hovered || el == dragging
                    ? ColorUtils.withAlpha((int) ColorUtils.THEME_ACCENT, 160)
                    : ColorUtils.withAlpha((int) ColorUtils.THEME_BG_LIGHT, 200);
            RenderUtils2D.roundedRect(ctx, x, y, BOX_W, BOX_H, 4, bg);
            ctx.drawCenteredTextWithShadow(textRenderer, el.display, x + BOX_W / 2, y + 4, (int) ColorUtils.THEME_TEXT);
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (var el : XekkaVisualsClient.HUD_ELEMENTS.elements.values()) {
            if (!el.enabled.getValue()) continue;
            int x = el.x.getValue().intValue(), y = el.y.getValue().intValue();
            if (mouseX >= x && mouseX <= x + BOX_W && mouseY >= y && mouseY <= y + BOX_H) {
                dragging = el;
                dragOffX = (int) (mouseX - x);
                dragOffY = (int) (mouseY - y);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null) {
            dragging.x.setValue((double) ((int) (mouseX - dragOffX)));
            dragging.y.setValue((double) ((int) (mouseY - dragOffY)));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        ConfigManager.save();
        super.close();
    }

    @Override
    public boolean shouldPause() { return false; }
}
