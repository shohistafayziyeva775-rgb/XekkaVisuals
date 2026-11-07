package com.xekka.visuals.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/** Rounded-rectangle / circle / glow helpers built on vanilla DrawContext fill calls. */
public final class RenderUtils2D {
    private RenderUtils2D() {}

    public static void roundedRect(DrawContext ctx, float x, float y, float w, float h, float radius, int color) {
        radius = Math.min(radius, Math.min(w, h) / 2f);
        int x0 = (int) x, y0 = (int) y, x1 = (int) (x + w), y1 = (int) (y + h);
        int r = (int) radius;

        ctx.fill(x0 + r, y0, x1 - r, y1, color);
        ctx.fill(x0, y0 + r, x1, y1 - r, color);

        for (int i = 0; i <= r; i++) {
            double dx = circleOffset(i, r);
            ctx.fill((int) (x0 + r - dx), y0 + r - i, (int) (x0 + r + 1), y0 + r - i + 1, color);
            ctx.fill((int) (x1 - r - 1), y0 + r - i, (int) (x1 - r + dx + 1), y0 + r - i + 1, color);
            ctx.fill((int) (x0 + r - dx), y1 - r + i - 1, (int) (x0 + r + 1), y1 - r + i, color);
            ctx.fill((int) (x1 - r - 1), y1 - r + i - 1, (int) (x1 - r + dx + 1), y1 - r + i, color);
        }
    }

    private static double circleOffset(int i, int r) {
        if (r <= 0) return 0;
        double t = (double) i / r;
        return r - r * Math.sqrt(Math.max(0, 1 - (1 - t) * (1 - t)));
    }

    public static void roundedRectBorder(DrawContext ctx, float x, float y, float w, float h, float radius, int thickness, int color) {
        roundedRect(ctx, x, y, w, thickness, radius, color);
        roundedRect(ctx, x, y + h - thickness, w, thickness, radius, color);
        roundedRect(ctx, x, y, thickness, h, radius, color);
        roundedRect(ctx, x + w - thickness, y, thickness, h, radius, color);
    }

    /** Soft outer glow approximated with several descending-alpha rounded rects. */
    public static void glow(DrawContext ctx, float x, float y, float w, float h, float radius, int color, int steps) {
        int baseAlpha = (color >>> 24) & 0xFF;
        for (int i = steps; i >= 1; i--) {
            float grow = i * 1.5f;
            int alpha = (int) (baseAlpha * (1f - (float) i / (steps + 1)) * 0.35f);
            int c = (alpha << 24) | (color & 0xFFFFFF);
            roundedRect(ctx, x - grow, y - grow, w + grow * 2, h + grow * 2, radius + grow, c);
        }
    }

    /** Filled circle outline used by toggle knobs / dock icons. */
    public static void circle(DrawContext ctx, float cx, float cy, float radius, int color) {
        int r = (int) radius;
        for (int yy = -r; yy <= r; yy++) {
            int dx = (int) Math.sqrt(Math.max(0, r * r - yy * yy));
            ctx.fill((int) (cx - dx), (int) (cy + yy), (int) (cx + dx + 1), (int) (cy + yy + 1), color);
        }
    }

    public static void horizontalGradient(DrawContext ctx, int x, int y, int w, int h, int colorLeft, int colorRight) {
        ctx.fillGradient(x, y, x + w, y + h, colorLeft, colorRight);
    }

    public static float ease(float t) {
        // ease-out-cubic
        t = MathHelper.clamp(t, 0f, 1f);
        float f = t - 1f;
        return f * f * f + 1f;
    }
}
