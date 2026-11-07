package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;

import java.util.List;

/** Purely visual client-side override — does not change the real server time/weather, only how
 *  the local renderer interprets sky angle, so it never desyncs gameplay. */
public class TimeWeatherModule extends Module {
    public final Setting.ModeSetting timeMode = register(new Setting.ModeSetting(
            "Time", "Server", List.of("Server", "Day", "Noon", "Sunset", "Night", "Midnight")));
    public final Setting.ModeSetting weatherMode = register(new Setting.ModeSetting(
            "Weather", "Server", List.of("Server", "Clear", "Rain", "Thunder")));
    public final Setting.BoolSetting customSky = register(new Setting.BoolSetting("Custom Sky Color", false));
    public final Setting.ColorSetting skyColor = register(new Setting.ColorSetting("Sky Color", 0xFF87CEEB));
    public final Setting.BoolSetting customFog = register(new Setting.BoolSetting("Custom Fog Color", false));

    public TimeWeatherModule() { super("Time & Weather", Category.MISC, false); }

    /** Returns a 0..24000 tick-of-day value to render, or -1 to use the real server time. */
    public long resolveTimeOfDay(long serverTime) {
        return switch (timeMode.getValue()) {
            case "Day" -> 1000;
            case "Noon" -> 6000;
            case "Sunset" -> 12500;
            case "Night" -> 14000;
            case "Midnight" -> 18000;
            default -> serverTime;
        };
    }
}
