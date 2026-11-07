package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;

public class HitMarkerModule extends Module {
    public final Setting.NumberSetting size = register(new Setting.NumberSetting("Size", 6, 2, 16, 1));
    public final Setting.NumberSetting durationTicks = register(new Setting.NumberSetting("Duration (ticks)", 6, 2, 20, 1));
    public final Setting.ColorSetting color = register(new Setting.ColorSetting("Color", 0xFFFFFFFF));
    public final Setting.BoolSetting sound = register(new Setting.BoolSetting("Play Sound", true));
    public final Setting.BoolSetting critColor = register(new Setting.BoolSetting("Different Color On Crit", true));

    private int ticksLeft = 0;
    private boolean lastWasCrit = false;

    public HitMarkerModule() { super("Hit Marker", Category.COMBAT_ESP, false); }

    public void trigger(boolean crit) {
        ticksLeft = durationTicks.getValue().intValue();
        lastWasCrit = crit;
        if (sound.getValue()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4f, crit ? 1.4f : 1.0f);
            }
        }
    }

    @Override
    public void onTick() { if (ticksLeft > 0) ticksLeft--; }

    public void render(DrawContext ctx) {
        if (!isEnabled() || ticksLeft <= 0) return;
        int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2;
        int s = size.getValue().intValue();
        int c = (critColor.getValue() && lastWasCrit) ? 0xFFFFD24D : color.getValue();
        ctx.fill(cx - s, cy - 1, cx + s, cy + 1, c);
        ctx.fill(cx - 1, cy - s, cx + 1, cy + s, c);
    }
}
