package com.xekka.visuals.mixin;

import com.xekka.visuals.XekkaVisualsClient;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Pushes the fog start/end distance far out when "No Fog" is on, rather than trying to cancel fog
 * entirely (full cancellation risks breaking chunk culling, which fog distance is tied to in some
 * MC versions). "applyFog" is the Yarn name for BackgroundRenderer's fog-distance method on the
 * 1.20–1.21 line; its FogData return/parameter shape has shifted slightly between minor versions,
 * so if this does not compile, open BackgroundRenderer with the pinned mappings and re-point the
 * @ModifyVariable at whichever local holds the computed far-fog distance.
 */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @ModifyVariable(method = "applyFog", at = @At("STORE"), ordinal = 1)
    private static float xekkavisuals$extendFogDistance(float farDistance) {
        var noOverlays = XekkaVisualsClient.NO_OVERLAYS;
        if (noOverlays != null && noOverlays.isEnabled() && noOverlays.noFog.getValue()) {
            return Math.max(farDistance, 512f);
        }
        return farDistance;
    }
}
