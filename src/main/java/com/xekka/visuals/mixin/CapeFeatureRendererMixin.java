package com.xekka.visuals.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xekka.visuals.XekkaVisualsClient;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tints the cape by setting the shader color before CapeFeatureRenderer draws its quad, then
 * resetting it afterward. This relies on the cape render path still multiplying vertex color by
 * RenderSystem's shader color uniform, which held for the 1.20–1.21 line; if the cape stops
 * tinting after an update, the render method likely switched to baking color into the mesh and
 * this inject point needs to move to wherever that happens instead.
 */
@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void xekkavisuals$capeColorPre(CallbackInfo ci) {
        var cosmetics = XekkaVisualsClient.COSMETICS;
        if (cosmetics == null || !cosmetics.isEnabled() || !cosmetics.capeEnabled.getValue()) return;
        float[] rgba = com.xekka.visuals.util.ColorUtils.toRGBAFloats(cosmetics.capeColor.getValue());
        RenderSystem.setShaderColor(rgba[0], rgba[1], rgba[2], 1f);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void xekkavisuals$capeColorPost(CallbackInfo ci) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
