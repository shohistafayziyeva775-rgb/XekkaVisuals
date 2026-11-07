package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/** Spawns an expanding sphere/ring at the point an entity was hit. */
public class HitBubbleModule extends Module {
    public final Setting.ModeSetting shape = register(new Setting.ModeSetting("Shape", "Ring", List.of("Ring", "Sphere", "Burst")));
    public final Setting.NumberSetting sizeSetting = register(new Setting.NumberSetting("Size", 0.6, 0.1, 2.0, 0.05));
    public final Setting.NumberSetting lifetimeTicks = register(new Setting.NumberSetting("Lifetime (ticks)", 10, 2, 40, 1));
    public final Setting.ColorSetting color = register(new Setting.ColorSetting("Color", 0xFFFF4D4D));
    public final Setting.BoolSetting rainbow = register(new Setting.BoolSetting("Rainbow", false));

    public static class Bubble {
        public final Vec3d pos; public final int color; public final String shape; public int age = 0; public final int lifetime;
        public Bubble(Vec3d pos, int color, String shape, int lifetime) { this.pos = pos; this.color = color; this.shape = shape; this.lifetime = lifetime; }
        public float progress() { return Math.min(1f, age / (float) lifetime); }
    }

    private final List<Bubble> active = new ArrayList<>();

    public HitBubbleModule() { super("Hit Bubble", Category.COMBAT_ESP, false); }

    public void spawn(Vec3d pos) {
        int c = rainbow.getValue() ? ColorUtils.rainbow(System.currentTimeMillis(), 1.3f, 0.9f, 1f) : color.getValue();
        active.add(new Bubble(pos, c, shape.getValue(), lifetimeTicks.getValue().intValue()));
    }

    @Override
    public void onTick() {
        active.removeIf(b -> b.age >= b.lifetime);
        for (Bubble b : active) b.age++;
    }

    public List<Bubble> getActive() { return active; }
    public double getBaseSize() { return sizeSetting.getValue(); }
}
