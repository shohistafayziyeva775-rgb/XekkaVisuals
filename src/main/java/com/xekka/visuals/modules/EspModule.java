package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class EspModule extends Module {
    public final Setting.ModeSetting mode = register(new Setting.ModeSetting(
            "Mode", "Box", List.of("Box", "Outline Box", "2D Box", "Corner Box")));
    public final Setting.BoolSetting players = register(new Setting.BoolSetting("Players", true));
    public final Setting.BoolSetting mobs = register(new Setting.BoolSetting("Mobs", false));
    public final Setting.BoolSetting items = register(new Setting.BoolSetting("Items", false));
    public final Setting.ModeSetting colorMode = register(new Setting.ModeSetting(
            "Color Mode", "Health", List.of("Static", "Rainbow", "Health", "Team")));
    public final Setting.NumberSetting lineWidth = register(new Setting.NumberSetting("Line Width", 1.5, 0.5, 4.0, 0.5));
    public final Setting.BoolSetting throughWalls = register(new Setting.BoolSetting("Through Walls", true));
    public final Setting.BoolSetting healthBar = register(new Setting.BoolSetting("Health Bar", true));
    public final Setting.BoolSetting nameTag = register(new Setting.BoolSetting("Name Tag", true));
    public final Setting.ColorSetting staticColor = register(new Setting.ColorSetting("Static Color", (int) ColorUtils.THEME_ACCENT));

    public EspModule() { super("ESP", Category.COMBAT_ESP, false); }

    public List<LivingEntity> getTargets() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return List.of();
        List<LivingEntity> out = new ArrayList<>();
        for (var entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (players.getValue() && entity instanceof PlayerEntity pe) out.add(pe);
            else if (mobs.getValue() && entity instanceof MobEntity me) out.add(me);
        }
        return out;
    }

    public int colorFor(LivingEntity e) {
        return switch (colorMode.getValue()) {
            case "Rainbow" -> ColorUtils.rainbow(System.currentTimeMillis(), 1f, 0.9f, 1f);
            case "Health" -> ColorUtils.healthColor(e.getHealth() / e.getMaxHealth());
            default -> staticColor.getValue();
        };
    }
}
