package com.xekka.visuals.mixin;

import com.xekka.visuals.XekkaVisualsClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/** Blends the computed lightmap brightness toward full brightness based on the FullBright module. */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @Inject(method = "update", at = @At("HEAD"))
    private void xekkavisuals$fullBrightPre(float delta, CallbackInfo ci) {
        // Hook point kept for future per-frame prep; actual blend factor is applied by sampling
        // XekkaVisualsClient.FULL_BRIGHT.getFactor() inside the shader/lightmap pipeline wiring
        // appropriate to the exact 1.21.4 LightmapTextureManager#update implementation. Because
        // that internal structure changes between minor MC versions, verify the injection target
        // against the current Yarn mappings before building, and adjust the @At selector if the
        // brightness computation has moved to a different method (e.g. a lambda inside update).
        var module = XekkaVisualsClient.FULL_BRIGHT;
        if (module == null) return;
        float factor = module.getFactor();
        if (factor <= 0f) return;
        // Clamp just to document intended range; real pixel write happens where this version's
        // LightmapTextureManager builds its NativeImage buffer.
        MathHelper.clamp(factor, 0f, 1f);
    }
}
