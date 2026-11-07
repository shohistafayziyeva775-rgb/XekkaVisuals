package com.xekka.visuals.mixin;

import com.xekka.visuals.XekkaVisualsClient;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels the vanilla weather (rain/snow) particle-and-overlay pass outright when "No Weather" is
 * on. "renderWeather" is the Yarn name for this method across the 1.20–1.21 line; if the build
 * reports it missing, search WorldRenderer for whichever method iterates rain/snow columns and
 * point this @Inject at its HEAD instead — the cancellation logic below does not need to change.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererOverlaysMixin {

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void xekkavisuals$cancelWeather(CallbackInfo ci) {
        var noOverlays = XekkaVisualsClient.NO_OVERLAYS;
        if (noOverlays != null && noOverlays.isEnabled() && noOverlays.noRain.getValue()) {
            ci.cancel();
        }
    }
}
