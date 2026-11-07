package com.xekka.visuals.gui;

import com.xekka.visuals.XekkaVisualsClient;
import com.xekka.visuals.modules.EmoteModule;
import com.xekka.visuals.util.ColorUtils;
import com.xekka.visuals.util.RenderUtils2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class EmoteWheelScreen extends Screen {
    private final EmoteModule.Emote[] emotes = EmoteModule.Emote.values();
    private int hovered = -1;

    public EmoteWheelScreen() { super(Text.literal("Emote Wheel")); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int cx = width / 2, cy = height / 2;
        double scale = XekkaVisualsClient.EMOTES.wheelScale.getValue();
        int outer = (int) (90 * scale), inner = (int) (34 * scale);

        double dx = mouseX - cx, dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        hovered = -1;
        if (dist > inner) {
            double angle = Math.atan2(dy, dx);
            if (angle < 0) angle += Math.PI * 2;
            int segment = (int) (angle / (Math.PI * 2 / emotes.length));
            hovered = Math.min(emotes.length - 1, segment);
        }

        RenderUtils2D.circle(ctx, cx, cy, outer, ColorUtils.withAlpha((int) ColorUtils.THEME_BG, 220));
        RenderUtils2D.circle(ctx, cx, cy, inner, ColorUtils.withAlpha((int) ColorUtils.THEME_BG_LIGHT, 255));

        for (int i = 0; i < emotes.length; i++) {
            double angle = (Math.PI * 2 * i) / emotes.length - Math.PI / 2;
            int labelX = (int) (cx + Math.cos(angle) * (outer + inner) / 2.0) - 16;
            int labelY = (int) (cy + Math.sin(angle) * (outer + inner) / 2.0) - 4;
            int color = i == hovered ? (int) ColorUtils.THEME_ON : (int) ColorUtils.THEME_TEXT;
            ctx.drawText(textRenderer, emotes[i].display, labelX, labelY, color, true);
        }

        if (hovered >= 0) {
            ctx.drawCenteredTextWithShadow(textRenderer, emotes[hovered].display, cx, cy - 4, (int) ColorUtils.THEME_ON);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered >= 0) {
            XekkaVisualsClient.EMOTES.play(emotes[hovered]);
            close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
