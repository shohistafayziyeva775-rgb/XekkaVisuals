package com.xekka.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xekka.visuals.config.ConfigManager;
import com.xekka.visuals.core.ModuleManager;
import com.xekka.visuals.gui.XekkaClickGui;
import com.xekka.visuals.modules.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class XekkaVisualsClient implements ClientModInitializer {
    public static KeyBinding OPEN_GUI_KEY;

    // Convenience static references the mixins and renderers read (populated in onInitializeClient).
    public static CrosshairModule CROSSHAIR;
    public static FullBrightModule FULL_BRIGHT;
    public static JumpCircleModule JUMP_CIRCLE;
    public static TargetEspModule TARGET_ESP;
    public static BlockOverlayModule BLOCK_OVERLAY;
    public static HitColorModule HIT_COLOR;
    public static AspectRatioModule ASPECT_RATIO;
    public static HitBubbleModule HIT_BUBBLE;
    public static WatermarkModule WATERMARK;
    public static CosmeticsModule COSMETICS;
    public static EmoteModule EMOTES;
    public static ThemeEditorModule THEME_EDITOR;
    public static KeyBinding EMOTE_WHEEL_KEY;
    public static HudElementsModule HUD_ELEMENTS;
    public static EspModule ESP;
    public static NametagsModule NAMETAGS;
    public static TrailsModule TRAILS;
    public static ParticlesModule PARTICLES;
    public static HitMarkerModule HIT_MARKER;
    public static DamageIndicatorModule DAMAGE_INDICATOR;
    public static ZoomModule ZOOM;
    public static FovChangerModule FOV_CHANGER;
    public static NoOverlaysModule NO_OVERLAYS;
    public static TimeWeatherModule TIME_WEATHER;
    public static KeyBinding HUD_EDIT_KEY;
    public static KeyBinding ZOOM_KEY;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        ConfigManager.init();

        CROSSHAIR = (CrosshairModule) ModuleManager.getByName("Crosshair");
        FULL_BRIGHT = (FullBrightModule) ModuleManager.getByName("Full Bright");
        JUMP_CIRCLE = (JumpCircleModule) ModuleManager.getByName("Jump Circle");
        TARGET_ESP = (TargetEspModule) ModuleManager.getByName("Target ESP");
        BLOCK_OVERLAY = (BlockOverlayModule) ModuleManager.getByName("Block Overlay");
        HIT_COLOR = (HitColorModule) ModuleManager.getByName("Hit Color");
        ASPECT_RATIO = (AspectRatioModule) ModuleManager.getByName("Aspect Ratio");
        HIT_BUBBLE = (HitBubbleModule) ModuleManager.getByName("Hit Bubble");
        WATERMARK = (WatermarkModule) ModuleManager.getByName("Watermark");
        COSMETICS = (CosmeticsModule) ModuleManager.getByName("Cosmetics");
        EMOTES = (EmoteModule) ModuleManager.getByName("Emotes");
        THEME_EDITOR = (ThemeEditorModule) ModuleManager.getByName("Theme Editor");
        HUD_ELEMENTS = (HudElementsModule) ModuleManager.getByName("HUD Editor");
        ESP = (EspModule) ModuleManager.getByName("ESP");
        NAMETAGS = (NametagsModule) ModuleManager.getByName("Nametags");
        TRAILS = (TrailsModule) ModuleManager.getByName("Trails");
        PARTICLES = (ParticlesModule) ModuleManager.getByName("Particles");
        HIT_MARKER = (HitMarkerModule) ModuleManager.getByName("Hit Marker");
        DAMAGE_INDICATOR = (DamageIndicatorModule) ModuleManager.getByName("Damage Indicator");
        ZOOM = (ZoomModule) ModuleManager.getByName("Zoom");
        FOV_CHANGER = (FovChangerModule) ModuleManager.getByName("FOV Changer");
        NO_OVERLAYS = (NoOverlaysModule) ModuleManager.getByName("No Overlays");
        TIME_WEATHER = (TimeWeatherModule) ModuleManager.getByName("Time & Weather");

        OPEN_GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xekkavisuals.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.xekkavisuals.main"));

        HUD_EDIT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xekkavisuals.hud_edit",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.xekkavisuals.main"));

        ZOOM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xekkavisuals.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "category.xekkavisuals.main"));

        EMOTE_WHEEL_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.xekkavisuals.emote_wheel",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.xekkavisuals.main"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (OPEN_GUI_KEY.wasPressed() && client.currentScreen == null) {
                client.setScreen(new XekkaClickGui());
            }
            if (HUD_EDIT_KEY.wasPressed() && client.currentScreen == null) {
                client.setScreen(new com.xekka.visuals.gui.HudEditorScreen());
            }
            if (EMOTE_WHEEL_KEY.wasPressed() && client.currentScreen == null) {
                client.setScreen(new com.xekka.visuals.gui.EmoteWheelScreen());
            }
            if (ZOOM != null && ZOOM.isEnabled()) {
                boolean held = InputUtil.isKeyPressed(client.getWindow().getHandle(),
                        GLFW.GLFW_KEY_Z);
                ZOOM.setHeld(held);
            }
            if (EMOTES.cancelOnMove.getValue() && EMOTES.isPlaying() && client.player != null
                    && client.player.input.getMovementInput().length() > 0.01f) {
                EMOTES.cancel();
            }
            // No Hurt Cam: genuinely works with no mixin needed — the vanilla hurt-cam tilt in
            // GameRenderer reads straight off LivingEntity#hurtTime, so zeroing it client-side
            // removes the tilt/shake with no visible side effect (health/damage are unaffected,
            // only the camera-shake timer is reset every tick).
            if (NO_OVERLAYS != null && NO_OVERLAYS.isEnabled() && NO_OVERLAYS.noHurtCam.getValue()
                    && client.player != null) {
                client.player.hurtTime = 0;
                client.player.lastDamageTaken = 0;
            }
            ModuleManager.tickAll();
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden) return;
            CROSSHAIR.render(drawContext);
            WATERMARK.render(drawContext, mc.textRenderer);
            com.xekka.visuals.gui.HudRenderer.render(drawContext, mc, tickDelta,
                    HUD_ELEMENTS, NAMETAGS, HIT_MARKER, DAMAGE_INDICATOR);
            com.xekka.visuals.gui.HudRenderer.renderEsp2D(drawContext, mc, ESP);
            if (EMOTES.showNameplate.getValue() && EMOTES.isPlaying()) {
                String label = EMOTES.getCurrent().display;
                int w = mc.textRenderer.getWidth(label);
                drawContext.drawTextWithShadow(mc.textRenderer, label,
                        drawContext.getScaledWindowWidth() / 2 - w / 2,
                        drawContext.getScaledWindowHeight() - 60,
                        (int) com.xekka.visuals.util.ColorUtils.THEME_ON);
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            WorldEffectsRenderer.renderJumpCircles(context, JUMP_CIRCLE);
            WorldEffectsRenderer.renderHitBubbles(context, HIT_BUBBLE);
            WorldEffectsRenderer.renderTargetEsp(context, TARGET_ESP);
            WorldEffectsRenderer.renderBlockOverlay(context, BLOCK_OVERLAY);
            WorldEffectsRenderer.renderCosmeticTrail(context, COSMETICS);
            WorldEffectsRenderer.renderTrails(context, TRAILS);
            WorldEffectsRenderer.renderEsp(context, ESP);
            WorldEffectsRenderer.renderDamageIndicators(context, DAMAGE_INDICATOR);
            RenderSystem.disableBlend();
        });

        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            HIT_COLOR.onHit(entity.getId());
            var hitPos = entity.getPos().add(0, entity.getHeight() / 2.0, 0);
            HIT_BUBBLE.spawn(hitPos);
            boolean crit = player.fallDistance > 0.0f && !player.isOnGround() && !player.isClimbing();
            if (HIT_MARKER.isEnabled()) HIT_MARKER.trigger(crit);
            if (PARTICLES.isEnabled()) PARTICLES.spawnHitParticles(hitPos);
            if (DAMAGE_INDICATOR.isEnabled()) {
                float approxDamage = (float) player.getAttributeValue(
                        net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE);
                DAMAGE_INDICATOR.spawn(hitPos, approxDamage, crit);
            }
            return net.minecraft.util.ActionResult.PASS;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::save));
    }
}
