package com.xekka.visuals.util;

import java.awt.Color;

public final class ColorUtils {
    private ColorUtils() {}

    /** Rainbow color cycling over time (ms). speed = cycles per 10 seconds roughly. */
    public static int rainbow(long timeMs, float speed, float saturation, float brightness) {
        float hue = ((timeMs * speed) % 10000L) / 10000f;
        return Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF | 0xFF000000;
    }

    public static int withAlpha(int rgb, int alpha) {
        return (alpha & 0xFF) << 24 | (rgb & 0xFFFFFF);
    }

    public static int lerp(int colorA, int colorB, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (colorA >> 24) & 0xFF, ar = (colorA >> 16) & 0xFF, ag = (colorA >> 8) & 0xFF, ab = colorA & 0xFF;
        int ba = (colorB >> 24) & 0xFF, br = (colorB >> 16) & 0xFF, bg = (colorB >> 8) & 0xFF, bb = colorB & 0xFF;
        int a = (int) (aa + (ba - aa) * t);
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int b = (int) (ab + (bb - ab) * t);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    /** Green (full health) to red (empty) based on ratio 0..1. */
    public static int healthColor(float ratio) {
        ratio = Math.max(0f, Math.min(1f, ratio));
        int green = 0xFF3DDC84;
        int red = 0xFFFF3B3B;
        return lerp(red, green, ratio);
    }

    public static float[] toRGBAFloats(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        return new float[]{r, g, b, a};
    }

    /** Signature "ink" premium theme accent: deep charcoal -> near-black with a faint violet-ink sheen. */
    public static final int THEME_BG = 0xE60B0B0E;        // panel background, ~90% opaque near-black
    public static final int THEME_BG_LIGHT = 0xCC17171C;  // row background
    public static final int THEME_ACCENT = 0xFF8A7CFF;    // ink-violet accent (borders/selected)
    public static final int THEME_ACCENT_DIM = 0x558A7CFF;
    public static final int THEME_ON = 0xFF3DDC84;         // toggle ON green
    public static final int THEME_OFF = 0xFF3A3A42;        // toggle OFF gray
    public static final int THEME_TEXT = 0xFFEDEDF2;
    public static final int THEME_TEXT_DIM = 0xFF8C8C96;
}
