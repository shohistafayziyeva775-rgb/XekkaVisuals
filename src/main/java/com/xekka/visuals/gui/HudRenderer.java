package com.xekka.visuals.gui;

import com.xekka.visuals.modules.*;
import com.xekka.visuals.util.ColorUtils;
import com.xekka.visuals.util.RenderUtils2D;
import com.xekka.visuals.util.WorldToScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.math.Vec3d;

public final class HudRenderer {
    private HudRenderer() {}

    public static void render(DrawContext ctx, MinecraftClient mc, float tickDelta,
                               HudElementsModule hud, NametagsModule nametags,
                               HitMarkerModule hitMarker, DamageIndicatorModule dmg) {
        if (mc.player == null) return;
        hitMarker.render(ctx);
        renderArmor(ctx, mc, hud.get("armor"));
        renderPotions(ctx, mc, hud.get("potions"));
        renderCoords(ctx, mc, hud.get("coords"));
        renderPerf(ctx, mc, hud.get("perf"));
        renderKeystrokes(ctx, mc, hud.get("keystrokes"));
        renderNametags(ctx, mc, nametags);
        renderDamageNumbers(ctx, mc, dmg);
    }

    /** Draws the "2D Box" ESP mode: projects each entity's 8 bounding-box corners to screen space
     *  and fills a flat screen-aligned rectangle around their min/max — a true 2D box, not a
     *  reprojected 3D wireframe. Call this from the HUD layer alongside render(...) above. */
    public static void renderEsp2D(DrawContext ctx, MinecraftClient mc, EspModule esp) {
        if (esp == null || !esp.isEnabled() || !"2D Box".equals(esp.mode.getValue())) return;
        var camera = mc.gameRenderer.getCamera();
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        double fov = currentFov(mc);

        for (var entity : esp.getTargets()) {
            double w = entity.getWidth() / 2.0, h = entity.getHeight();
            Vec3d base = entity.getPos();
            Vec3d[] corners = {
                    base.add(-w, 0, -w), base.add(w, 0, -w), base.add(-w, 0, w), base.add(w, 0, w),
                    base.add(-w, h, -w), base.add(w, h, -w), base.add(-w, h, w), base.add(w, h, w)
            };
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            boolean any = false;
            for (Vec3d c : corners) {
                var proj = WorldToScreen.project(c, camera, fov, sw, sh);
                if (proj == null) continue;
                any = true;
                minX = Math.min(minX, proj.x()); maxX = Math.max(maxX, proj.x());
                minY = Math.min(minY, proj.y()); maxY = Math.max(maxY, proj.y());
            }
            if (!any) continue;
            int color = esp.colorFor(entity);
            ctx.drawBorder((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY),
                    ColorUtils.withAlpha(color, 220));
        }
    }

    private static double currentFov(MinecraftClient mc) {
        return mc.options.getFov().getValue();
    }

    /** Projects each tracked entity's head position to screen space and draws the built tag text. */
    private static void renderNametags(DrawContext ctx, MinecraftClient mc, NametagsModule nametags) {
        if (nametags == null || !nametags.isEnabled() || mc.world == null) return;
        var camera = mc.gameRenderer.getCamera();
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        double fov = currentFov(mc);
        double maxDist = nametags.maxDistance.getValue();

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof net.minecraft.entity.LivingEntity living)) continue;
            if (!nametags.selfTag.getValue() && entity == mc.player) continue;
            if (mc.player != null && mc.player.distanceTo(entity) > maxDist) continue;

            Vec3d headPos = entity.getPos().add(0, entity.getHeight() + 0.25, 0);
            var proj = WorldToScreen.project(headPos, camera, fov, sw, sh);
            if (proj == null) continue;
            if (proj.x() < 0 || proj.x() > sw || proj.y() < 0 || proj.y() > sh) continue;

            String tag = nametags.buildTag(living, mc);
            int w = mc.textRenderer.getWidth(tag);
            int x = (int) proj.x() - w / 2, y = (int) proj.y();
            if (nametags.background.getValue()) {
                ctx.fill(x - 2, y - 1, x + w + 2, y + 9, ColorUtils.withAlpha((int) ColorUtils.THEME_BG, 150));
            }
            ctx.drawTextWithShadow(mc.textRenderer, tag, x, y, (int) ColorUtils.THEME_TEXT);
        }
    }

    /** Projects each active damage indicator to screen space and draws the floating number. */
    private static void renderDamageNumbers(DrawContext ctx, MinecraftClient mc, DamageIndicatorModule dmg) {
        if (dmg == null || !dmg.isEnabled() || dmg.getActive().isEmpty()) return;
        var camera = mc.gameRenderer.getCamera();
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        double fov = currentFov(mc);

        for (var ind : dmg.getActive()) {
            Vec3d pos = ind.pos.add(0, ind.age * dmg.getRiseSpeed(), 0);
            var proj = WorldToScreen.project(pos, camera, fov, sw, sh);
            if (proj == null) continue;
            float lifeRatio = ind.age / (float) ind.lifetime;
            float scale = (ind.crit && dmg.critBigger.getValue() ? 1.5f : 1f) * dmg.scale.getValue().floatValue();
            int alpha = (int) (255 * (1f - lifeRatio));
            String text = (ind.crit ? "§6" : "") + String.format("%.1f", ind.amount);
            ctx.getMatrices().push();
            ctx.getMatrices().translate(proj.x(), proj.y(), 0);
            ctx.getMatrices().scale(scale, scale, 1f);
            int w = mc.textRenderer.getWidth(text);
            ctx.drawTextWithShadow(mc.textRenderer, text, -w / 2, 0,
                    ColorUtils.withAlpha(dmg.color.getValue(), alpha));
            ctx.getMatrices().pop();
        }
    }

    private static void panelBg(DrawContext ctx, int x, int y, int w, int h) {
        RenderUtils2D.roundedRect(ctx, x - 4, y - 3, w + 8, h + 6, 4, ColorUtils.withAlpha((int) ColorUtils.THEME_BG, 170));
    }

    private static void renderArmor(DrawContext ctx, MinecraftClient mc, HudElementsModule.Element el) {
        if (!el.enabled.getValue()) return;
        int x = el.x.getValue().intValue(), y = el.y.getValue().intValue();
        double armor = mc.player.getAttributeValue(EntityAttributes.ARMOR);
        String text = "Armor: " + (int) armor;
        panelBg(ctx, x, y, mc.textRenderer.getWidth(text), 9);
        ctx.drawTextWithShadow(mc.textRenderer, text, x, y, (int) ColorUtils.THEME_TEXT);
    }

    private static void renderPotions(DrawContext ctx, MinecraftClient mc, HudElementsModule.Element el) {
        if (!el.enabled.getValue()) return;
        int x = el.x.getValue().intValue(), y = el.y.getValue().intValue();
        var effects = mc.player.getStatusEffects();
        if (effects.isEmpty()) return;
        int line = 0;
        for (var effect : effects) {
            String text = effect.getEffectType().value().getName().getString() + " " + (effect.getAmplifier() + 1);
            panelBg(ctx, x, y + line * 10, mc.textRenderer.getWidth(text), 9);
            ctx.drawTextWithShadow(mc.textRenderer, text, x, y + line * 10, (int) ColorUtils.THEME_ON);
            line++;
            if (line > 6) break;
        }
    }

    private static void renderCoords(DrawContext ctx, MinecraftClient mc, HudElementsModule.Element el) {
        if (!el.enabled.getValue()) return;
        int x = el.x.getValue().intValue(), y = el.y.getValue().intValue();
        var p = mc.player.getPos();
        String text = String.format("X: %.1f  Y: %.1f  Z: %.1f", p.x, p.y, p.z);
        panelBg(ctx, x, y, mc.textRenderer.getWidth(text), 9);
        ctx.drawTextWithShadow(mc.textRenderer, text, x, y, (int) ColorUtils.THEME_TEXT);
    }

    private static void renderPerf(DrawContext ctx, MinecraftClient mc, HudElementsModule.Element el) {
        if (!el.enabled.getValue()) return;
        int x = el.x.getValue().intValue(), y = el.y.getValue().intValue();
        int fps = mc.getCurrentFps();
        String text = "FPS: " + fps;
        panelBg(ctx, x, y, mc.textRenderer.getWidth(text), 9);
        ctx.drawTextWithShadow(mc.textRenderer, text, x, y, (int) ColorUtils.THEME_TEXT);
    }

    private static int[] keyHoldTicks = new int[4];

    private static void renderKeystrokes(DrawContext ctx, MinecraftClient mc, HudElementsModule.Element el) {
        if (!el.enabled.getValue()) return;
        int x = el.x.getValue().intValue(), y = el.y.getValue().intValue();
        int size = 18, gap = 2;
        boolean[] pressed = {
                mc.options.forwardKey.isPressed(),
                mc.options.leftKey.isPressed(),
                mc.options.backKey.isPressed(),
                mc.options.rightKey.isPressed()
        };
        String[] labels = {"W", "A", "S", "D"};
        int[] px = {x + size + gap, x, x + size + gap, x + 2 * (size + gap)};
        int[] py = {y, y + size + gap, y + size + gap, y + size + gap};
        for (int i = 0; i < 4; i++) {
            int bg = pressed[i] ? (int) ColorUtils.THEME_ON : (int) ColorUtils.THEME_BG_LIGHT;
            RenderUtils2D.roundedRect(ctx, px[i], py[i], size, size, 3, bg);
            ctx.drawCenteredTextWithShadow(mc.textRenderer, labels[i], px[i] + size / 2, py[i] + size / 2 - 4,
                    pressed[i] ? 0xFF000000 : (int) ColorUtils.THEME_TEXT);
        }
    }
}
