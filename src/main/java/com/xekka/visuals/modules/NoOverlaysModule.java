package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;

public class NoOverlaysModule extends Module {
    public final Setting.BoolSetting noHurtCam = register(new Setting.BoolSetting("No Hurt Cam", true));
    public final Setting.BoolSetting noFireOverlay = register(new Setting.BoolSetting("No Fire Overlay", true));
    public final Setting.BoolSetting noRain = register(new Setting.BoolSetting("No Weather/Rain", true));
    public final Setting.BoolSetting noFog = register(new Setting.BoolSetting("No Fog", true));
    public final Setting.BoolSetting lowFire = register(new Setting.BoolSetting("Low Fire (3rd Person Only)", false));

    public NoOverlaysModule() { super("No Overlays", Category.VISUALS, false); }
}
