package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;

public class NametagsModule extends Module {
    public final Setting.BoolSetting showHealth = register(new Setting.BoolSetting("Health", true));
    public final Setting.BoolSetting showArmor = register(new Setting.BoolSetting("Armor", true));
    public final Setting.BoolSetting showDistance = register(new Setting.BoolSetting("Distance", true));
    public final Setting.BoolSetting showPing = register(new Setting.BoolSetting("Ping", false));
    public final Setting.NumberSetting scale = register(new Setting.NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    public final Setting.BoolSetting background = register(new Setting.BoolSetting("Background", true));
    public final Setting.BoolSetting selfTag = register(new Setting.BoolSetting("Show Own Tag", false));
    public final Setting.BoolSetting throughWalls = register(new Setting.BoolSetting("Through Walls", false));
    public final Setting.NumberSetting maxDistance = register(new Setting.NumberSetting("Max Distance", 50, 5, 200, 5));
    public final Setting.BoolSetting stackLines = register(new Setting.BoolSetting("Stack Lines", true));

    public NametagsModule() { super("Nametags", Category.PLAYERS, false); }

    public String buildTag(net.minecraft.entity.LivingEntity e, net.minecraft.client.MinecraftClient mc) {
        StringBuilder sb = new StringBuilder(e.getName().getString());
        if (showHealth.getValue()) sb.append(" §c").append(Math.round(e.getHealth())).append("HP");
        if (showDistance.getValue() && mc.player != null) {
            int dist = (int) mc.player.distanceTo(e);
            sb.append(" §7").append(dist).append("m");
        }
        return sb.toString();
    }
}
