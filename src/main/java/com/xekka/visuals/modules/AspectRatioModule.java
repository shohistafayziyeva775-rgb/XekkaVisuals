package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;

import java.util.List;

/** Real aspect override is applied in GameRendererMixin by scaling the projection matrix X axis.
 *  This class only stores the target ratio that the mixin reads. */
public class AspectRatioModule extends Module {
    public final Setting.NumberSetting ratio = register(new Setting.NumberSetting("Ratio", 1.777, 0.5, 3.0, 0.01));
    public final Setting.ModeSetting preset = register(new Setting.ModeSetting(
            "Preset", "16:9", List.of("4:3", "16:9", "21:9", "5:4", "Stretched", "Custom")));
    public final Setting.BoolSetting applyToGui = register(new Setting.BoolSetting("Apply To GUI", false));
    public final Setting.NumberSetting smoothing = register(new Setting.NumberSetting("Smoothing", 0.2, 0.0, 1.0, 0.05));
    public final Setting.BoolSetting lockInMenus = register(new Setting.BoolSetting("Lock In Menus (disable)", true));

    public AspectRatioModule() { super("Aspect Ratio", Category.VISUALS, false); }

    public double resolveRatio() {
        return switch (preset.getValue()) {
            case "4:3" -> 4.0 / 3.0;
            case "16:9" -> 16.0 / 9.0;
            case "21:9" -> 21.0 / 9.0;
            case "5:4" -> 5.0 / 4.0;
            case "Stretched" -> 3.0;
            default -> ratio.getValue();
        };
    }
}
