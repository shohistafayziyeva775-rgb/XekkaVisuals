package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ParticlesModule extends Module {
    public final Setting.NumberSetting hitMultiplier = register(new Setting.NumberSetting("Hit Multiplier", 2.0, 1.0, 10.0, 0.5));
    public final Setting.NumberSetting critMultiplier = register(new Setting.NumberSetting("Crit Multiplier", 3.0, 1.0, 10.0, 0.5));
    public final Setting.BoolSetting customColor = register(new Setting.BoolSetting("Custom Color", false));
    public final Setting.ColorSetting hitColor = register(new Setting.ColorSetting("Hit Color", 0xFFFF3B3B));
    public final Setting.BoolSetting rainbow = register(new Setting.BoolSetting("Rainbow Particles", false));
    public final Setting.ModeSetting shape = register(new Setting.ModeSetting(
            "Particle Shape", "Crit", List.of("Crit", "Heart", "Smoke", "Flame", "Note")));
    public final Setting.NumberSetting spread = register(new Setting.NumberSetting("Spread", 0.3, 0.0, 1.0, 0.05));
    public final Setting.BoolSetting onHit = register(new Setting.BoolSetting("On Hit", true));
    public final Setting.BoolSetting onKill = register(new Setting.BoolSetting("On Kill", true));
    public final Setting.BoolSetting upward = register(new Setting.BoolSetting("Upward Force", true));

    public ParticlesModule() { super("Particles", Category.VISUALS, false); }

    /** Called from AttackEntityCallback — spawns extra particles at the hit position. */
    public void spawnHitParticles(Vec3d hitPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || !onHit.getValue()) return;
        int count = hitMultiplier.getValue().intValue();
        var particleType = switch (shape.getValue()) {
            case "Heart" -> ParticleTypes.HEART;
            case "Smoke" -> ParticleTypes.SMOKE;
            case "Flame" -> ParticleTypes.FLAME;
            case "Note" -> ParticleTypes.NOTE;
            default -> ParticleTypes.CRIT;
        };
        double sp = spread.getValue();
        for (int i = 0; i < count; i++) {
            double vx = (Math.random() - 0.5) * sp;
            double vy = upward.getValue() ? Math.random() * 0.3 : (Math.random() - 0.5) * sp;
            double vz = (Math.random() - 0.5) * sp;
            mc.world.addParticle(particleType, hitPos.x, hitPos.y, hitPos.z, vx, vy, vz);
        }
    }
}
