package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** Purely client-side, cosmetic-only visuals: a generated cape glow and a movement trail. */
public class CosmeticsModule extends Module {
    public final Setting.BoolSetting capeEnabled = register(new Setting.BoolSetting("Cape Glow", false));
    public final Setting.ColorSetting capeColor = register(new Setting.ColorSetting("Cape Color", (int) ColorUtils.THEME_ACCENT));
    public final Setting.ModeSetting trailStyle = register(new Setting.ModeSetting(
            "Trail Style", "Line", List.of("Line", "Dots", "Sparks")));
    public final Setting.BoolSetting trailRainbow = register(new Setting.BoolSetting("Trail Rainbow", true));
    public final Setting.NumberSetting trailLength = register(new Setting.NumberSetting("Trail Length (ticks)", 20, 4, 80, 1));

    private final Deque<Vec3d> trailPoints = new ArrayDeque<>();

    public CosmeticsModule() { super("Cosmetics", Category.COSMETICS, false); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        trailPoints.addFirst(mc.player.getPos());
        int max = trailLength.getValue().intValue();
        while (trailPoints.size() > max) trailPoints.removeLast();
    }

    @Override
    protected void onDisable() { trailPoints.clear(); }

    public Deque<Vec3d> getTrailPoints() { return trailPoints; }

    public int resolveTrailColor(int indexFromHead) {
        if (trailRainbow.getValue()) {
            return ColorUtils.rainbow(System.currentTimeMillis() - indexFromHead * 20L, 1f, 0.85f, 1f);
        }
        return capeColor.getValue();
    }
}
