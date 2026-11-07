package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;

import java.util.HashMap;
import java.util.Map;

/** Tracks a short-lived color flash per entity id when the player deals damage to it. */
public class HitColorModule extends Module {
    public final Setting.ColorSetting color = register(new Setting.ColorSetting("Color", 0xAAFF3B3B));
    public final Setting.NumberSetting durationTicks = register(new Setting.NumberSetting("Duration (ticks)", 5, 1, 20, 1));
    public final Setting.BoolSetting fade = register(new Setting.BoolSetting("Fade Out", true));
    public final Setting.NumberSetting intensity = register(new Setting.NumberSetting("Intensity", 1.0, 0.1, 1.0, 0.05));
    public final Setting.BoolSetting affectMobsOnly = register(new Setting.BoolSetting("Mobs Only (not players)", false));

    private final Map<Integer, Integer> flashTicks = new HashMap<>();

    public HitColorModule() { super("Hit Color", Category.COMBAT_ESP, false); }

    public void onHit(int entityId) {
        flashTicks.put(entityId, durationTicks.getValue().intValue());
    }

    @Override
    public void onTick() {
        flashTicks.replaceAll((id, t) -> t - 1);
        flashTicks.values().removeIf(t -> t <= 0);
    }

    /** Returns the ARGB overlay color to render for this entity this frame, or 0 if none. */
    public int colorFor(int entityId) {
        Integer t = flashTicks.get(entityId);
        if (t == null || t <= 0) return 0;
        int base = color.getValue();
        if (!fade.getValue()) return base;
        float ratio = t / (float) durationTicks.getValue().intValue();
        int alpha = (int) (((base >>> 24) & 0xFF) * ratio * intensity.getValue());
        return (alpha << 24) | (base & 0xFFFFFF);
    }
}
