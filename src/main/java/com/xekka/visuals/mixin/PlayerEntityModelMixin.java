package com.xekka.visuals.mixin;

import com.xekka.visuals.XekkaVisualsClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bends the local player's right arm while an emote is playing. PlayerEntityModel exposes its
 * body parts as public ModelPart fields named rightArm/leftArm in Yarn for 1.21.x — if the exact
 * field name differs after re-checking mappings, update the @Shadow target below; the emote
 * timing/progress logic in EmoteModule itself needs no changes either way.
 */
@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin {

    @Shadow public ModelPart rightArm;

    @Inject(method = "setAngles", at = @At("TAIL"))
    private void xekkavisuals$emotePose(LivingEntity entity, float limbAngle, float limbDistance,
                                         float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || entity != mc.player) return;
        var emotes = XekkaVisualsClient.EMOTES;
        if (emotes == null || !emotes.isPlaying()) return;

        float offsetDegrees = emotes.armPitchOffset();
        if (offsetDegrees == 0f) return;
        rightArm.pitch -= Math.toRadians(offsetDegrees);
    }
}
