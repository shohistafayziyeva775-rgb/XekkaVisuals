package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import com.xekka.visuals.util.RenderUtils2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class CrosshairModule extends Module {
    public final Setting.ModeSetting style = register(new Setting.ModeSetting(
            "Style", "Cross", List.of("Cross", "Dot", "Circle", "T-Shape")));
    public final Setting.NumberSetting size = register(new Setting.NumberSetting("Size", 6, 2, 20, 1));
    public final Setting.NumberSetting thickness = register(new Setting.NumberSetting("Thickness", 2, 1, 6, 1));
    public final Setting.BoolSetting dynamicGap = register(new Setting.BoolSetting("Dynamic Gap", true));
    public final Setting.ColorSetting color = register(new Setting.ColorSetting("Color", (int) ColorUtils.THEME_ON));

    public CrosshairModule() { super("Crosshair", Category.VISUALS, false); }

    public void render(DrawContext ctx) {
        if (!isEnabled()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;
        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;

        float gap = 2;
        if (dynamicGap.getValue() && mc.player != null) {
            double speed = mc.player.getVelocity().horizontalLength();
            gap += (float) Math.min(6, speed * 40);
        }
        int s = size.getValue().intValue();
        int t = thickness.getValue().intValue();
        int argb = color.getValue();

        switch (style.getValue()) {
            case "Dot" -> RenderUtils2D.circle(ctx, cx, cy, Math.max(1, t), argb);
            case "Circle" -> drawRingOutline(ctx, cx, cy, s, t, argb);
            case "T-Shape" -> {
                ctx.fill((int) (cx - s), cy, (int) (cx + s), cy + t, argb);
                ctx.fill(cx, cy, cx + t, (int) (cy + s), argb);
            }
            default -> { // Cross
                ctx.fill((int) (cx - gap - s), (int) (cy - t / 2f), (int) (cx - gap), (int) (cy + t / 2f + 1), argb);
                ctx.fill((int) (cx + gap), (int) (cy - t / 2f), (int) (cx + gap + s), (int) (cy + t / 2f + 1), argb);
                ctx.fill((int) (cx - t / 2f), (int) (cy - gap - s), (int) (cx + t / 2f + 1), (int) (cy - gap), argb);
                ctx.fill((int) (cx - t / 2f), (int) (cy + gap), (int) (cx + t / 2f + 1), (int) (cy + gap + s), argb);
            }
        }
    }

    /** Draws a hollow ring by filling a disc then re-filling a smaller disc with the background
     *  cleared via alpha-blended "erase" color is unreliable on GUI layer, so instead we draw
     *  a ring using many thin concentric arcs limited to [radius-thickness, radius]. */
    private void drawRingOutline(DrawContext ctx, int cx, int cy, int radius, int thickness, int color) {
        int inner = Math.max(0, radius - thickness);
        for (int rr = inner; rr <= radius; rr++) {
            int r = rr;
            int steps = Math.max(8, r * 2);
            for (int i = 0; i < steps; i++) {
                double angle = (Math.PI * 2 * i) / steps;
                int px = (int) (cx + r * Math.cos(angle));
                int py = (int) (cy + r * Math.sin(angle));
                ctx.fill(px, py, px + 1, py + 1, color);
            }
        }
    }
}
