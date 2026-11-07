package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.List;

/** Highlights whatever entity the crosshair currently targets. */
public class TargetEspModule extends Module {
    public final Setting.ModeSetting mode = register(new Setting.ModeSetting(
            "Mode", "Ring", List.of("Ring", "Box", "Halo", "Tracer", "Pentagram", "Chains",
                    "Crystals", "Orbiting Orbs", "Arrow")));
    public final Setting.ModeSetting colorMode = register(new Setting.ModeSetting(
            "Color Mode", "Theme", List.of("Static", "Rainbow", "Health-Based", "Theme")));
    public final Setting.NumberSetting size = register(new Setting.NumberSetting("Size", 1.0, 0.3, 3.0, 0.1));
    public final Setting.BoolSetting playersOnly = register(new Setting.BoolSetting("Players Only", false));
    public final Setting.NumberSetting fadeOutTicks = register(new Setting.NumberSetting("Fade Out (ticks)", 10, 0, 40, 1));

    private Entity currentTarget;
    private int ticksSinceLost = 0;

    public TargetEspModule() { super("Target ESP", Category.COMBAT_ESP, false); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Entity target = resolveTarget(mc);
        if (target != null) {
            currentTarget = target;
            ticksSinceLost = 0;
        } else if (currentTarget != null) {
            ticksSinceLost++;
            if (ticksSinceLost > fadeOutTicks.getValue().intValue()) currentTarget = null;
        }
    }

    private Entity resolveTarget(MinecraftClient mc) {
        if (mc.crosshairTarget instanceof EntityHitResult eh && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity e = eh.getEntity();
            if (playersOnly.getValue() && !(e instanceof PlayerEntity)) return null;
            return e;
        }
        return null;
    }

    public Entity getCurrentTarget() { return currentTarget; }

    public int colorFor(Entity target) {
        return switch (colorMode.getValue()) {
            case "Rainbow" -> ColorUtils.rainbow(System.currentTimeMillis(), 1.2f, 0.9f, 1f);
            case "Health-Based" -> {
                if (target instanceof net.minecraft.entity.LivingEntity le) {
                    yield ColorUtils.healthColor(le.getHealth() / le.getMaxHealth());
                }
                yield (int) ColorUtils.THEME_ACCENT;
            }
            case "Static" -> (int) ColorUtils.THEME_ACCENT;
            default -> 0xFFFF4D4D; // Theme
        };
    }

    public float alphaMultiplier() {
        if (currentTarget != null) return 1f;
        int fade = fadeOutTicks.getValue().intValue();
        if (fade <= 0) return 0f;
        return Math.max(0f, 1f - (ticksSinceLost / (float) fade));
    }
}
