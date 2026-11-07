package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;
import com.xekka.visuals.util.ColorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class BlockOverlayModule extends Module {
    public final Setting.NumberSetting fillAlpha = register(new Setting.NumberSetting("Fill Alpha", 60, 0, 255, 1));
    public final Setting.NumberSetting outlineWidth = register(new Setting.NumberSetting("Outline Width", 2, 1, 6, 1));
    public final Setting.ColorSetting color = register(new Setting.ColorSetting("Color", (int) ColorUtils.THEME_ACCENT));
    public final Setting.BoolSetting rainbow = register(new Setting.BoolSetting("Rainbow", false));
    public final Setting.BoolSetting throughWalls = register(new Setting.BoolSetting("Through Walls", false));

    public BlockOverlayModule() { super("Block Overlay", Category.VISUALS, false); }

    public BlockPos getLookedAtBlock() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            return ((BlockHitResult) mc.crosshairTarget).getBlockPos();
        }
        return null;
    }

    public int resolveColor() {
        if (rainbow.getValue()) return ColorUtils.rainbow(System.currentTimeMillis(), 1f, 0.85f, 1f);
        return color.getValue();
    }
}
