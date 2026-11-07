package com.xekka.visuals.mixin;

import com.xekka.visuals.XekkaVisualsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /** Overrides the computed FOV before the projection matrix is built, applying Zoom (held)
     *  then the standing FOV Changer override. Method name/signature for "the FOV getter" has
     *  varied in wording across 1.2x minor versions (getFov vs getFovMultiplier variants) — if
     *  the build reports this target missing, open GameRenderer in your IDE with the configured
     *  Yarn mappings and point @At("RETURN") at whichever method returns the final double FOV. */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void xekkavisuals$overrideFov(Camera camera, float tickDelta, boolean changingFov,
                                           CallbackInfoReturnable<Double> cir) {
        double fov = cir.getReturnValue();
        var zoom = XekkaVisualsClient.ZOOM;
        var fovChanger = XekkaVisualsClient.FOV_CHANGER;
        if (fovChanger != null && fovChanger.isEnabled()) {
            fov = fovChanger.getFixedFov((float) fov);
        }
        if (zoom != null && zoom.isEnabled() && zoom.isHeld()) {
            fov = zoom.getAppliedFov((float) fov);
        }
        cir.setReturnValue(fov);
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true)
    private void xekkavisuals$aspectRatio(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        var module = XekkaVisualsClient.ASPECT_RATIO;
        if (module == null || !module.isEnabled()) return;
        if (module.lockInMenus.getValue() && MinecraftClient.getInstance().currentScreen != null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        double realRatio = (double) mc.getWindow().getFramebufferWidth() / mc.getWindow().getFramebufferHeight();
        double targetRatio = module.resolveRatio();
        float xScale = (float) (realRatio / targetRatio);

        Matrix4f matrix = new Matrix4f(cir.getReturnValue());
        matrix.scale(1f / xScale, 1f, 1f);
        cir.setReturnValue(matrix);
    }
}
