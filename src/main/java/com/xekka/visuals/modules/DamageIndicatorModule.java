package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class DamageIndicatorModule extends Module {
    public final Setting.NumberSetting lifetimeTicks = register(new Setting.NumberSetting("Lifetime (ticks)", 20, 5, 60, 1));
    public final Setting.NumberSetting riseSpeed = register(new Setting.NumberSetting("Rise Speed", 0.03, 0.0, 0.2, 0.01));
    public final Setting.ColorSetting color = register(new Setting.ColorSetting("Color", 0xFFFF4D4D));
    public final Setting.BoolSetting critBigger = register(new Setting.BoolSetting("Bigger On Crit", true));
    public final Setting.NumberSetting scale = register(new Setting.NumberSetting("Scale", 1.0, 0.5, 3.0, 0.1));

    public static class Indicator {
        public final Vec3d pos; public final float amount; public final boolean crit;
        public int age = 0; public final int lifetime;
        public Indicator(Vec3d pos, float amount, boolean crit, int lifetime) {
            this.pos = pos; this.amount = amount; this.crit = crit; this.lifetime = lifetime;
        }
    }

    private final List<Indicator> active = new ArrayList<>();

    public DamageIndicatorModule() { super("Damage Indicator", Category.COMBAT_ESP, false); }

    public void spawn(Vec3d pos, float amount, boolean crit) {
        active.add(new Indicator(pos, amount, crit, lifetimeTicks.getValue().intValue()));
    }

    @Override
    public void onTick() {
        active.removeIf(i -> i.age >= i.lifetime);
        for (Indicator i : active) i.age++;
    }

    @Override
    protected void onDisable() { active.clear(); }

    public List<Indicator> getActive() { return active; }
    public double getRiseSpeed() { return riseSpeed.getValue(); }
}
