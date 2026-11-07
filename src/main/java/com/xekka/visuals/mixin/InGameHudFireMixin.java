package com.xekka.visuals.mixin;

import com.xekka.visuals.XekkaVisualsClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels the full-screen fire overlay quad when "No Fire Overlay" is on. "renderFire" is the
 * Yarn name for InGameHud's fire-overlay method on the 1.20–1.21 line; some minor versions fold
 * this into a generic "renderOverlay(DrawContext, Identifier, float)" helper instead — if so,
 * redirect this @Inject to that helper and check the Identifier argument equals the fire overlay
 * texture before cancelling, so you don't also suppress the pumpkin-head/underwater overlays.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudFireMixin {

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private void xekkavisuals$cancelFire(CallbackInfo ci) {
        var noOverlays = XekkaVisualsClient.NO_OVERLAYS;
        if (noOverlays != null && noOverlays.isEnabled() && noOverlays.noFireOverlay.getValue()) {
            ci.cancel();
        }
    }
}
