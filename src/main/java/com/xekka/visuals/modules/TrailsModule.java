package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class TrailsModule extends Module {
    public final Setting.ModeSetting style = register(new Setting.ModeSetting(
            "Style", "Line", List.of("Line", "Dots", "Particles", "Ribbon")));
    public final Setting.ModeSetting colorMode = register(new Setting.ModeSetting(
            "Color Mode", "Rainbow", List.of("Rainbow", "Static", "Gradient", "Health")));
    public final Setting.NumberSetting length = register(new Setting.NumberSetting("Length", 24, 4, 100, 2));
    public final Setting.NumberSetting width = register(new Setting.NumberSetting("Width", 2.0, 0.5, 6.0, 0.5));
    public final Setting.ColorSetting color1 = register(new Setting.ColorSetting("Color 1", (int) ColorUtils.THEME_ACCENT));
    public final Setting.ColorSetting color2 = register(new Setting.ColorSetting("Color 2", (int) ColorUtils.THEME_ON));
    public final Setting.BoolSetting groundOnly = register(new Setting.BoolSetting("Ground Only", false));
    public final Setting.NumberSetting heightOffset = register(new Setting.NumberSetting("Height Offset", 0.1, 0.0, 1.0, 0.05));
    public final Setting.BoolSetting fade = register(new Setting.BoolSetting("Fade Out", true));
    public final Setting.BoolSetting bobbingOffset = register(new Setting.BoolSetting("Smooth Bobbing", false));

    public static class TrailPoint {
        public final Vec3d pos;
        public final long timeMs;
        public TrailPoint(Vec3d pos) { this.pos = pos; this.timeMs = System.currentTimeMillis(); }
    }

    private final Deque<TrailPoint> points = new ArrayDeque<>();

    public TrailsModule() { super("Trails", Category.PLAYERS, false); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (groundOnly.getValue() && !mc.player.isOnGround()) return;
        double off = heightOffset.getValue();
        points.addFirst(new TrailPoint(mc.player.getPos().add(0, off, 0)));
        while (points.size() > length.getValue().intValue()) points.removeLast();
    }

    @Override
    protected void onDisable() { points.clear(); }

    public Deque<TrailPoint> getPoints() { return points; }

    public int colorAt(int idx, int total, TrailPoint pt) {
        float ratio = total > 1 ? (float) idx / (total - 1) : 0f;
        return switch (colorMode.getValue()) {
            case "Static" -> color1.getValue();
            case "Gradient" -> ColorUtils.lerp(color1.getValue(), color2.getValue(), ratio);
            case "Health" -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null) yield color1.getValue();
                yield ColorUtils.healthColor(mc.player.getHealth() / mc.player.getMaxHealth());
            }
            default -> ColorUtils.rainbow(pt.timeMs, 0.8f, 0.9f, 1f);
        };
    }
}
