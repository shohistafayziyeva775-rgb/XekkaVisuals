package com.xekka.visuals.mixin;

import com.xekka.visuals.XekkaVisualsClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides the sky angle (0..1, used by WorldRenderer for sun/moon position and sky color) based
 * on the Time & Weather module's chosen time-of-day preset. "getSkyAngle" is World's Yarn name
 * for this on the 1.20–1.21 line and ClientWorld inherits it; the conversion from a 0..24000 tick
 * value to the 0..1 angle mirrors vanilla's own getSkyAngle body (tick/24000, minus a quarter-turn
 * offset so noon lines up with the sun overhead) — re-check that formula against the decompiled
 * vanilla method if the sun position looks off after building.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldSkyMixin {

    @Inject(method = "getSkyAngle", at = @At("RETURN"), cancellable = true)
    private void xekkavisuals$overrideSkyAngle(float tickDelta, CallbackInfoReturnable<Float> cir) {
        var module = XekkaVisualsClient.TIME_WEATHER;
        if (module == null || !module.isEnabled() || "Server".equals(module.timeMode.getValue())) return;

        long tickOfDay = module.resolveTimeOfDay(0L); // 0L is unused for every non-"Server" preset
        double d = (tickOfDay / 24000.0) - 0.25;
        if (d < 0) d += 1.0;
        cir.setReturnValue((float) d);
    }
}
