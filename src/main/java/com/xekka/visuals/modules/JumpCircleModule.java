package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/** Spawns an expanding ring/shape at the player's feet whenever they leave the ground going up. */
public class JumpCircleModule extends Module {
    public final Setting.ModeSetting shape = register(new Setting.ModeSetting(
            "Shape", "Flat Ring",
            List.of("Flat Ring", "Filled Circle", "Double Ring", "Star", "Hexagon",
                    "Pentagram", "Chains", "Sakura Petals", "Lightning Ring", "Spiral", "Dashed Ring",
                    "Square", "Cross", "Diamond", "Heart", "Vortex", "Rotating Ring", "Wave", "Dots Orbit")));
    public final Setting.ModeSetting colorMode = register(new Setting.ModeSetting(
            "Color Mode", "Red-Green Theme",
            List.of("Rainbow", "Static", "Red-Green Theme", "Health-Based", "Random Per Jump")));
    public final Setting.NumberSetting radius = register(new Setting.NumberSetting("Radius", 1.2, 0.3, 4.0, 0.1));
    public final Setting.NumberSetting lifetimeTicks = register(new Setting.NumberSetting("Lifetime (ticks)", 14, 4, 60, 1));
    public final Setting.ColorSetting staticColor = register(new Setting.ColorSetting("Static Color", (int) ColorUtils.THEME_ACCENT));

    public static class Ring {
        public final Vec3d pos;
        public final int color;
        public final String shape;
        public int age = 0;
        public final int lifetime;
        public final long seed;
        public Ring(Vec3d pos, int color, String shape, int lifetime, long seed) {
            this.pos = pos; this.color = color; this.shape = shape; this.lifetime = lifetime; this.seed = seed;
        }
        public float progress() { return Math.min(1f, age / (float) lifetime); }
    }

    private final List<Ring> active = new ArrayList<>();
    private boolean wasOnGround = true;
    private int randomSeed = 0;

    public JumpCircleModule() { super("Jump Circle", Category.VISUALS, false); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        boolean onGround = mc.player.isOnGround();
        boolean movingUp = mc.player.getVelocity().y > 0.1;
        if (wasOnGround && !onGround && movingUp) {
            spawn(mc);
        }
        wasOnGround = onGround;

        active.removeIf(r -> r.age >= r.lifetime);
        for (Ring r : active) r.age++;
    }

    private void spawn(MinecraftClient mc) {
        randomSeed++;
        int color = switch (colorMode.getValue()) {
            case "Rainbow" -> ColorUtils.rainbow(System.currentTimeMillis(), 1f, 0.9f, 1f);
            case "Health-Based" -> ColorUtils.healthColor(mc.player.getHealth() / mc.player.getMaxHealth());
            case "Random Per Jump" -> ColorUtils.rainbow(randomSeed * 777L, 1f, 0.9f, 1f);
            case "Static" -> staticColor.getValue();
            default -> randomSeed % 2 == 0 ? (int) ColorUtils.THEME_ON : 0xFFFF4D4D; // Red-Green Theme
        };
        active.add(new Ring(mc.player.getPos(), color, shape.getValue(), lifetimeTicks.getValue().intValue(),
                System.nanoTime()));
    }

    public List<Ring> getActiveRings() { return active; }
    public double getBaseRadius() { return radius.getValue(); }
}
