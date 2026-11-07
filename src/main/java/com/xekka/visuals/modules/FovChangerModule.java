package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;

public class FovChangerModule extends Module {
    public final Setting.NumberSetting fov = register(new Setting.NumberSetting("FOV", 90, 30, 110, 1));
    public final Setting.BoolSetting ignoreSprint = register(new Setting.BoolSetting("Ignore Sprint Bonus", true));
    public final Setting.BoolSetting ignoreSpeedPotion = register(new Setting.BoolSetting("Ignore Speed Potion Bonus", true));
    public final Setting.BoolSetting smoothTransition = register(new Setting.BoolSetting("Smooth Transition", true));
    public final Setting.NumberSetting transitionSpeed = register(new Setting.NumberSetting("Transition Speed", 0.2, 0.02, 1.0, 0.01));

    private float current;

    public FovChangerModule() { super("FOV Changer", Category.VISUALS, false); }

    public float getFixedFov(float vanillaFov) {
        if (!isEnabled()) return vanillaFov;
        float target = fov.getValue().floatValue();
        if (!smoothTransition.getValue()) return target;
        current += (target - current) * transitionSpeed.getValue().floatValue();
        return current;
    }
}
