package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;

public class ZoomModule extends Module {
    public final Setting.NumberSetting zoomFov = register(new Setting.NumberSetting("Zoom FOV", 30, 5, 90, 1));
    public final Setting.NumberSetting smoothing = register(new Setting.NumberSetting("Smoothing", 0.25, 0.02, 1.0, 0.01));
    public final Setting.BoolSetting scrollAdjust = register(new Setting.BoolSetting("Scroll To Adjust", true));
    public final Setting.BoolSetting slowSensitivity = register(new Setting.BoolSetting("Lower Sensitivity While Zoomed", true));
    public final Setting.NumberSetting minZoomFov = register(new Setting.NumberSetting("Min Zoom FOV", 5, 1, 30, 1));

    private boolean held = false;
    private float currentFov;

    public ZoomModule() { super("Zoom", Category.VISUALS, false); }

    public void setHeld(boolean held) { this.held = held; }
    public boolean isHeld() { return held; }

    public void adjustScroll(double delta) {
        if (!held || !scrollAdjust.getValue()) return;
        double v = zoomFov.getValue() - delta * 2;
        zoomFov.setValue(Math.max(minZoomFov.getValue(), Math.min(90, v)));
    }

    /** Called every frame to get the FOV to apply (blends toward target FOV smoothly). */
    public float getAppliedFov(float normalFov) {
        float target = (isEnabled() && held) ? zoomFov.getValue().floatValue() : normalFov;
        currentFov += (target - currentFov) * smoothing.getValue().floatValue();
        if (Math.abs(currentFov - target) < 0.05f) currentFov = target;
        if (currentFov == 0f) currentFov = normalFov;
        return currentFov;
    }
}
