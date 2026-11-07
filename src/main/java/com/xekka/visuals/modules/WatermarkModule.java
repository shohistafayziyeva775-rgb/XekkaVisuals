package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.client.gui.DrawContext;

public class WatermarkModule extends Module {
    public final Setting.BoolSetting animated = register(new Setting.BoolSetting("Animated Gradient", true));
    public final Setting.NumberSetting x = register(new Setting.NumberSetting("X", 4, 0, 4000, 1));
    public final Setting.NumberSetting y = register(new Setting.NumberSetting("Y", 4, 0, 4000, 1));
    public final Setting.NumberSetting scale = register(new Setting.NumberSetting("Scale", 1.0, 0.5, 3.0, 0.1));
    public final Setting.BoolSetting showVersion = register(new Setting.BoolSetting("Show Version", true));

    public WatermarkModule() { super("Watermark", Category.MISC, true); }

    public void render(DrawContext ctx, net.minecraft.client.font.TextRenderer tr) {
        if (!isEnabled()) return;
        String text = "XekkaVisuals" + (showVersion.getValue() ? " v0.1.0" : "");
        int colorA = animated.getValue()
                ? ColorUtils.rainbow(System.currentTimeMillis(), 0.6f, 0.2f, 1f)
                : (int) ColorUtils.THEME_ON;
        int colorB = animated.getValue() ? 0xFFFF4D4D : (int) ColorUtils.THEME_ACCENT;
        int px = x.getValue().intValue();
        int py = y.getValue().intValue();
        // simple left-to-right two-color gradient across the text by splitting into halves
        String left = text.substring(0, text.length() / 2);
        String right = text.substring(text.length() / 2);
        ctx.drawText(tr, left, px, py, colorA, true);
        ctx.drawText(tr, right, px + tr.getWidth(left), py, colorB, true);
    }
}
