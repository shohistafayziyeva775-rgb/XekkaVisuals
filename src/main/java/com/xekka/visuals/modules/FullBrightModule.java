package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;

import java.util.List;

/** Actual lightmap override happens in LightmapTextureManagerMixin, which reads this module's
 *  state directly (XekkaVisualsClient.FULL_BRIGHT). Kept as a plain data holder + toggle here. */
public class FullBrightModule extends Module {
    public final Setting.ModeSetting mode = register(new Setting.ModeSetting(
            "Mode", "Full", List.of("Full", "Smooth", "Night Vision Style")));
    public final Setting.NumberSetting strength = register(new Setting.NumberSetting("Strength", 1.0, 0.0, 1.0, 0.05));
    public final Setting.BoolSetting affectNether = register(new Setting.BoolSetting("Affect Nether/End", true));
    public final Setting.BoolSetting smoothTransition = register(new Setting.BoolSetting("Smooth Transition", true));
    public final Setting.NumberSetting transitionSpeed = register(new Setting.NumberSetting("Transition Speed", 0.15, 0.02, 1.0, 0.01));

    private float currentFactor = 0f;

    public FullBrightModule() { super("Full Bright", Category.VISUALS, false); }

    /** Called every frame from the mixin to compute the current brightness blend factor (0..1). */
    public float getFactor() {
        float target = isEnabled() ? strength.getValue().floatValue() : 0f;
        if (!smoothTransition.getValue()) return target;
        currentFactor += (target - currentFactor) * transitionSpeed.getValue().floatValue();
        if (Math.abs(currentFactor - target) < 0.001f) currentFactor = target;
        return currentFactor;
    }
}
