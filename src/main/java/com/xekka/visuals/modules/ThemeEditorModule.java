package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;

/** Live-editable GUI theme. The ClickGUI reads these getters every frame instead of the static
 *  ColorUtils constants, so changes apply immediately with no restart. */
public class ThemeEditorModule extends Module {
    public final Setting.ColorSetting accentColor = register(new Setting.ColorSetting("Accent Color", (int) ColorUtils.THEME_ACCENT));
    public final Setting.ColorSetting toggleOnColor = register(new Setting.ColorSetting("Toggle ON Color", (int) ColorUtils.THEME_ON));
    public final Setting.NumberSetting panelOpacity = register(new Setting.NumberSetting("Panel Opacity", 90, 40, 100, 1));
    public final Setting.NumberSetting rainbowBorderSpeed = register(new Setting.NumberSetting("Rainbow Border Speed", 0.25, 0.0, 2.0, 0.05));
    public final Setting.BoolSetting rainbowBorder = register(new Setting.BoolSetting("Animated Rainbow Border", true));

    public ThemeEditorModule() { super("Theme Editor", Category.MISC, true); }

    public int accent() { return accentColor.getValue(); }
    public int toggleOn() { return toggleOnColor.getValue(); }

    public int panelBg() {
        int alpha = (int) (panelOpacity.getValue() / 100.0 * 255);
        return (alpha << 24) | (ColorUtils.THEME_BG & 0xFFFFFF);
    }

    public int borderColor(long timeMs) {
        if (!rainbowBorder.getValue()) return ColorUtils.withAlpha(accent(), 140);
        return ColorUtils.withAlpha(ColorUtils.rainbow(timeMs, rainbowBorderSpeed.getValue().floatValue(), 0.6f, 1f), 90);
    }
}
