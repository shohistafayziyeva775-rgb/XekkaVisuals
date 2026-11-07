package com.xekka.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.xekka.visuals.modules.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Draws all world-space visual effects (rings, bubbles, ESP) using the Tessellator directly.
 * NOTE: exact Tessellator/BufferBuilder/VertexFormat call signatures shift slightly between
 * Minecraft minor versions. The pattern below (begin → vertex → draw via BufferRenderer) is
 * correct for the 1.21.x line; re-check against the Yarn mappings bundled with the configured
 * minecraft_version before building if a method is reported missing.
 */
public final class WorldEffectsRenderer {
    private WorldEffectsRenderer() {}

    public static void renderJumpCircles(WorldRenderContext ctx, JumpCircleModule module) {
        if (module == null || module.getActiveRings().isEmpty()) return;
        Vec3d cam = ctx.camera().getPos();
        Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
        double baseRadius = module.getBaseRadius();

        for (JumpCircleModule.Ring ring : module.getActiveRings()) {
            float progress = ring.progress();
            double radius = baseRadius * (0.3 + progress * 1.2);
            float alpha = (1f - progress);
            Vec3d center = ring.pos.add(0, 0.05, 0);
            drawJumpShape(matrix, cam, center, radius, ring.color, alpha, ring.shape, ring.seed, progress, ring.age);
        }
    }

    /** Dispatches to the correct draw routine for a Jump Circle shape. All shapes are built from
     *  the same horizontal-plane line/triangle primitives so they stay cheap per-frame. */
    private static void drawJumpShape(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color,
                                       float alpha, String shape, long seed, float progress, int ageTicks) {
        double rotation = ageTicks * 0.12; // radians per tick, used by the rotating/orbiting shapes
        switch (shape) {
            case "Filled Circle" -> drawHorizontalRing(matrix, cam, center, radius, color, alpha, 40);
            case "Double Ring" -> {
                drawLineLoop(matrix, cam, center, radius, color, alpha, 40, 0);
                drawLineLoop(matrix, cam, center, radius * 0.6, color, alpha, 40, 0);
            }
            case "Star" -> drawPolygonLoop(matrix, cam, center, radius, color, alpha, 5, true, 0);
            case "Hexagon" -> drawPolygonLoop(matrix, cam, center, radius, color, alpha, 6, false, 0);
            case "Pentagram" -> drawPentagram(matrix, cam, center, radius, color, alpha);
            case "Chains" -> drawChains(matrix, cam, center, radius, color, alpha, 6);
            case "Sakura Petals" -> drawSakuraPetals(matrix, cam, center, radius, color, alpha, 6);
            case "Lightning Ring" -> drawLightningRing(matrix, cam, center, radius, color, alpha, seed, 28);
            case "Spiral" -> drawSpiral(matrix, cam, center, radius, color, alpha, progress);
            case "Dashed Ring" -> drawLineLoop(matrix, cam, center, radius, color, alpha, 40, 2);
            case "Square" -> drawPolygonLoop(matrix, cam, center, radius, color, alpha, 4, false, Math.PI / 4);
            case "Cross" -> drawCross(matrix, cam, center, radius, color, alpha);
            case "Diamond" -> drawPolygonLoop(matrix, cam, center, radius, color, alpha, 4, false, 0);
            case "Heart" -> drawHeart(matrix, cam, center, radius, color, alpha);
            case "Vortex" -> drawSpiral(matrix, cam, center, radius, color, alpha, progress, rotation);
            case "Rotating Ring" -> drawPolygonLoop(matrix, cam, center, radius, color, alpha, 8, false, rotation);
            case "Wave" -> drawWaveRing(matrix, cam, center, radius, color, alpha, rotation);
            case "Dots Orbit" -> drawDotsOrbit(matrix, cam, center, radius, color, alpha, rotation, 6);
            default -> drawLineLoop(matrix, cam, center, radius, color, alpha, 48, 0); // Flat Ring
        }
    }

    private static float[] rgbaOf(int color, float alpha) {
        return com.xekka.visuals.util.ColorUtils.toRGBAFloats((color & 0xFFFFFF) | ((int) (alpha * 255) << 24));
    }

    /** Closed loop of line segments on the horizontal plane. skipEvery > 0 leaves a visual gap
     *  after every Nth segment to create a dashed effect. */
    private static void drawLineLoop(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color,
                                      float alpha, int segments, int skipEvery) {
        float[] rgba = rgbaOf(color, alpha);
        float cx = (float) (center.x - cam.x), cy = (float) (center.y - cam.y), cz = (float) (center.z - cam.z);
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(2f);
        for (int i = 0; i < segments; i++) {
            if (skipEvery > 0 && (i / skipEvery) % 2 == 0) continue;
            double a0 = (Math.PI * 2 * i) / segments;
            double a1 = (Math.PI * 2 * (i + 1)) / segments;
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            buf.vertex(matrix, (float) (cx + radius * Math.cos(a0)), cy, (float) (cz + radius * Math.sin(a0)))
                    .color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buf.vertex(matrix, (float) (cx + radius * Math.cos(a1)), cy, (float) (cz + radius * Math.sin(a1)))
                    .color(rgba[0], rgba[1], rgba[2], rgba[3]);
            BufferRenderer.drawWithGlobalProgram(buf.end());
        }
    }

    /** n-pointed star (star=true, alternating outer/inner radius) or a regular n-gon (star=false). */
    private static void drawPolygonLoop(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color,
                                         float alpha, int points, boolean star, double rotationOffset) {
        float[] rgba = rgbaOf(color, alpha);
        float cx = (float) (center.x - cam.x), cy = (float) (center.y - cam.y), cz = (float) (center.z - cam.z);
        int verts = star ? points * 2 : points;
        float[][] xz = new float[verts][2];
        for (int i = 0; i < verts; i++) {
            double a = rotationOffset + (Math.PI * 2 * i) / verts - Math.PI / 2;
            double r = star ? (i % 2 == 0 ? radius : radius * 0.45) : radius;
            xz[i][0] = (float) (cx + r * Math.cos(a));
            xz[i][1] = (float) (cz + r * Math.sin(a));
        }
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(2f);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= verts; i++) {
            float[] p = xz[i % verts];
            buf.vertex(matrix, p[0], cy, p[1]).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    /** True pentagram: 5 points connected in skip-one order (0-2-4-1-3-0), not the pentagon outline. */
    private static void drawPentagram(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color, float alpha) {
        float[] rgba = rgbaOf(color, alpha);
        float cx = (float) (center.x - cam.x), cy = (float) (center.y - cam.y), cz = (float) (center.z - cam.z);
        float[][] pts = new float[5][2];
        for (int i = 0; i < 5; i++) {
            double a = (Math.PI * 2 * i) / 5 - Math.PI / 2;
            pts[i][0] = (float) (cx + radius * Math.cos(a));
            pts[i][1] = (float) (cz + radius * Math.sin(a));
        }
        int[] order = {0, 2, 4, 1, 3, 0};
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(2f);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int idx : order) {
            buf.vertex(matrix, pts[idx][0], cy, pts[idx][1]).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    /** Several small ring "links" evenly spaced around the main radius, like a chain laid flat. */
    private static void drawChains(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color, float alpha, int linkCount) {
        double linkRadius = Math.max(0.08, radius * 0.18);
        for (int i = 0; i < linkCount; i++) {
            double a = (Math.PI * 2 * i) / linkCount;
            Vec3d linkCenter = center.add(radius * Math.cos(a), 0, radius * Math.sin(a));
            drawLineLoop(matrix, cam, linkCenter, linkRadius, color, alpha, 14, 0);
        }
    }

    /** Small flat "petal" triangles arranged radially to suggest falling sakura blossoms. */
    private static void drawSakuraPetals(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color, float alpha, int petalCount) {
        float[] rgba = rgbaOf(color, alpha);
        float cx = (float) (center.x - cam.x), cy = (float) (center.y - cam.y), cz = (float) (center.z - cam.z);
        Tessellator tess = Tessellator.getInstance();
        for (int i = 0; i < petalCount; i++) {
            double a = (Math.PI * 2 * i) / petalCount;
            double baseX = cx + radius * Math.cos(a), baseZ = cz + radius * Math.sin(a);
            double tipX = cx + (radius + 0.3) * Math.cos(a), tipZ = cz + (radius + 0.3) * Math.sin(a);
            double perpX = Math.cos(a + Math.PI / 2) * 0.12, perpZ = Math.sin(a + Math.PI / 2) * 0.12;
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            buf.vertex(matrix, (float) (baseX - perpX), cy, (float) (baseZ - perpZ)).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buf.vertex(matrix, (float) (baseX + perpX), cy, (float) (baseZ + perpZ)).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buf.vertex(matrix, (float) tipX, cy, (float) tipZ).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            BufferRenderer.drawWithGlobalProgram(buf.end());
        }
    }

    /** Jagged ring where each vertex's radius is perturbed with a seeded pseudo-random offset,
     *  giving a static "lightning" silhouette instead of a smooth circle. */
    private static void drawLightningRing(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color,
                                           float alpha, long seed, int segments) {
        float[] rgba = rgbaOf(color, alpha);
        float cx = (float) (center.x - cam.x), cy = (float) (center.y - cam.y), cz = (float) (center.z - cam.z);
        java.util.Random rng = new java.util.Random(seed);
        float[][] xz = new float[segments][2];
        for (int i = 0; i < segments; i++) {
            double a = (Math.PI * 2 * i) / segments;
            double jitter = 0.75 + rng.nextDouble() * 0.4; // 0.75..1.15x radius
            xz[i][0] = (float) (cx + radius * jitter * Math.cos(a));
            xz[i][1] = (float) (cz + radius * jitter * Math.sin(a));
        }
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(2f);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            float[] p = xz[i % segments];
            buf.vertex(matrix, p[0], cy, p[1]).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    /** Line strip spiraling outward from the center to the current radius, growing with progress. */
    private static void drawSpiral(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color, float alpha, float progress) {
        drawSpiral(matrix, cam, center, radius, color, alpha, progress, 0);
    }

    private static void drawSpiral(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color, float alpha, float progress, double rotation) {
        float[] rgba = rgbaOf(color, alpha);
        float cx = (float) (center.x - cam.x), cy = (float) (center.y - cam.y), cz = (float) (center.z - cam.z);
        int turns = 2;
        int segments = 48;
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(2f);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            double a = rotation + t * Math.PI * 2 * turns;
            double r = radius * t;
            buf.vertex(matrix, (float) (cx + r * Math.cos(a)), cy, (float) (cz + r * Math.sin(a)))
                    .color(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    /** Plus-shaped cross made of two perpendicular line segments through the center. */
    private static void drawCross(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color, float alpha) {
        float[] rgba = rgbaOf(color, alpha);
        float cx = (float) (center.x - cam.x), cy = (float) (center.y - cam.y), cz = (float) (center.z - cam.z);
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(2f);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        buf.vertex(matrix, (float) (cx - radius), cy, cz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        buf.vertex(matrix, (float) (cx + radius), cy, cz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        BufferBuilder buf2 = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        buf2.vertex(matrix, cx, cy, (float) (cz - radius)).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        buf2.vertex(matrix, cx, cy, (float) (cz + radius)).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        BufferRenderer.drawWithGlobalProgram(buf2.end());
    }

    /** Parametric heart curve flattened onto the horizontal plane. */
    private static void drawHeart(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color, float alpha) {
        float[] rgba = rgbaOf(color, alpha);
        float cx = (float) (center.x - cam.x), cy = (float) (center.y - cam.y), cz = (float) (center.z - cam.z);
        int segments = 40;
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(2f);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double t = (Math.PI * 2 * i) / segments;
            double hx = 16 * Math.pow(Math.sin(t), 3);
            double hz = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
            double scale = radius / 17.0;
            buf.vertex(matrix, (float) (cx + hx * scale), cy, (float) (cz - hz * scale))
                    .color(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    /** Ring whose radius oscillates sinusoidally per-segment, producing a scalloped wave outline. */
    private static void drawWaveRing(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color, float alpha, double rotation) {
        float[] rgba = rgbaOf(color, alpha);
        float cx = (float) (center.x - cam.x), cy = (float) (center.y - cam.y), cz = (float) (center.z - cam.z);
        int segments = 48, waves = 8;
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(2f);
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double a = rotation + (Math.PI * 2 * i) / segments;
            double r = radius * (1 + 0.15 * Math.sin(a * waves));
            buf.vertex(matrix, (float) (cx + r * Math.cos(a)), cy, (float) (cz + r * Math.sin(a)))
                    .color(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    /** Small dots orbiting the main radius, rotating over the ring's lifetime. */
    private static void drawDotsOrbit(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color, float alpha, double rotation, int dotCount) {
        for (int i = 0; i < dotCount; i++) {
            double a = rotation + (Math.PI * 2 * i) / dotCount;
            Vec3d dotCenter = center.add(radius * Math.cos(a), 0, radius * Math.sin(a));
            drawLineLoop(matrix, cam, dotCenter, Math.max(0.05, radius * 0.08), color, alpha, 10, 0);
        }
    }

    public static void renderHitBubbles(WorldRenderContext ctx, HitBubbleModule module) {
        if (module == null || module.getActive().isEmpty()) return;
        Vec3d cam = ctx.camera().getPos();
        Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
        double baseSize = module.getBaseSize();

        for (HitBubbleModule.Bubble b : module.getActive()) {
            float progress = b.progress();
            double radius = baseSize * (0.2 + progress * 1.0);
            float alpha = 1f - progress;
            drawBillboardRing(matrix, cam, b.pos, radius, b.color, alpha, 24);
        }
    }

    public static void renderTargetEsp(WorldRenderContext ctx, TargetEspModule module) {
        if (module == null) return;
        Entity target = module.getCurrentTarget();
        if (target == null) return;
        float alpha = module.alphaMultiplier();
        if (alpha <= 0f) return;

        Vec3d cam = ctx.camera().getPos();
        Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
        int color = module.colorFor(target);
        double size = module.size.getValue() * target.getWidth() * 1.3;
        Vec3d feet = target.getPos();
        Vec3d ringCenter = feet.add(0, 0.05, 0);

        switch (module.mode.getValue()) {
            case "Ring", "Pulse Sphere" -> drawHorizontalRing(matrix, cam, ringCenter, size, color, alpha, 32);
            case "Halo" -> drawBillboardRing(matrix, cam,
                    feet.add(0, target.getHeight() + 0.3, 0), size * 0.5, color, alpha, 24);
            case "Box" -> drawEntityBoxOutline(matrix, cam, target, color, alpha);
            case "Tracer" -> drawTracer(matrix, cam, ctx, target, color, alpha);
            case "Pentagram" -> drawPentagram(matrix, cam, ringCenter, size, color, alpha);
            case "Chains" -> drawChains(matrix, cam, ringCenter, size, color, alpha, 5);
            case "Crystals" -> drawOrbitingCrystals(matrix, cam, feet.add(0, target.getHeight() / 2.0, 0), size, color, alpha, System.currentTimeMillis());
            case "Orbiting Orbs" -> drawDotsOrbit(matrix, cam, feet.add(0, target.getHeight() / 2.0, 0), size, color, alpha, System.currentTimeMillis() * 0.003, 4);
            case "Arrow" -> drawArrowAbove(matrix, cam, feet.add(0, target.getHeight() + 0.5, 0), size, color, alpha, System.currentTimeMillis());
            default -> drawHorizontalRing(matrix, cam, ringCenter, size, color, alpha, 32);
        }
    }

    public static void renderTrails(WorldRenderContext ctx, TrailsModule module) {
        if (module == null || !module.isEnabled() || module.getPoints().isEmpty()) return;
        Vec3d cam = ctx.camera().getPos();
        Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(module.width.getValue().floatValue());
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        int total = module.getPoints().size(), i = 0;
        for (var pt : module.getPoints()) {
            int color = module.colorAt(i, total, pt);
            float alpha = module.fade.getValue() ? 1f - (i / (float) total) : 1f;
            float[] rgba = com.xekka.visuals.util.ColorUtils.toRGBAFloats((color & 0xFFFFFF) | ((int) (alpha * 255) << 24));
            buf.vertex(matrix, (float) (pt.pos.x - cam.x), (float) (pt.pos.y - cam.y), (float) (pt.pos.z - cam.z))
                    .color(rgba[0], rgba[1], rgba[2], rgba[3]);
            i++;
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    public static void renderEsp(WorldRenderContext ctx, EspModule module) {
        if (module == null || !module.isEnabled()) return;
        Vec3d cam = ctx.camera().getPos();
        Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
        for (var entity : module.getTargets()) {
            int color = module.colorFor(entity);
            float alpha = 0.9f;
            switch (module.mode.getValue()) {
                case "Corner Box" -> drawEntityCornerBox(matrix, cam, entity, color, alpha);
                case "2D Box" -> { /* drawn in screen space by HudRenderer.renderEsp2D instead */ }
                default -> drawEntityBoxOutline(matrix, cam, entity, color, alpha);
            }
        }
    }

    /** Only the short corner segments of the box (like a photo-camera focus bracket) instead of
     *  the full 12-edge wireframe. */
    private static void drawEntityCornerBox(Matrix4f matrix, Vec3d cam, Entity target, int color, float alpha) {
        double w = target.getWidth() / 2.0, h = target.getHeight();
        Vec3d pos = target.getPos();
        float x0 = (float) (pos.x - w - cam.x), y0 = (float) (pos.y - cam.y), z0 = (float) (pos.z - w - cam.z);
        float x1 = (float) (pos.x + w - cam.x), y1 = (float) (pos.y + h - cam.y), z1 = (float) (pos.z + w - cam.z);
        float lenX = (x1 - x0) * 0.25f, lenY = (y1 - y0) * 0.25f, lenZ = (z1 - z0) * 0.25f;

        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(2f);
        float[] rgba = com.xekka.visuals.util.ColorUtils.toRGBAFloats((color & 0xFFFFFF) | ((int) (alpha * 255) << 24));
        float[][] corners = {{x0,y0,z0},{x1,y0,z0},{x1,y0,z1},{x0,y0,z1},{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1}};
        for (float[] c : corners) {
            float sx = c[0] < (x0 + x1) / 2f ? lenX : -lenX;
            float sy = c[1] < (y0 + y1) / 2f ? lenY : -lenY;
            float sz = c[2] < (z0 + z1) / 2f ? lenZ : -lenZ;
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            buf.vertex(matrix, c[0] + sx, c[1], c[2]).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buf.vertex(matrix, c[0], c[1], c[2]).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buf.vertex(matrix, c[0], c[1] + sy, c[2]).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            BufferRenderer.drawWithGlobalProgram(buf.end());
            BufferBuilder buf2 = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            buf2.vertex(matrix, c[0], c[1], c[2]).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buf2.vertex(matrix, c[0], c[1], c[2] + sz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            BufferRenderer.drawWithGlobalProgram(buf2.end());
        }
    }

    public static void renderDamageIndicators(WorldRenderContext ctx, DamageIndicatorModule module) {
        if (module == null || module.getActive().isEmpty()) return;
        // Rendered as small billboard-less rings at the damage position rising upward, since full
        // screen-space number projection needs the GameRenderer's camera matrices wired in from the
        // HUD layer; this world-space stand-in still gives clear positional feedback on every hit.
        Vec3d cam = ctx.camera().getPos();
        Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
        for (var ind : module.getActive()) {
            float t = ind.age / (float) ind.lifetime;
            Vec3d pos = ind.pos.add(0, ind.age * module.getRiseSpeed(), 0);
            float alpha = 1f - t;
            double size = (ind.crit ? 0.18 : 0.1) * module.scale.getValue();
            drawHorizontalRing(matrix, cam, pos, size, module.color.getValue(), alpha, 10);
        }
    }

    public static void renderCosmeticTrail(WorldRenderContext ctx, CosmeticsModule module) {
        if (module == null || !module.isEnabled() || module.getTrailPoints().isEmpty()) return;
        Vec3d cam = ctx.camera().getPos();
        Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        int i = 0;
        for (Vec3d p : module.getTrailPoints()) {
            int color = module.resolveTrailColor(i);
            float alpha = 1f - (i / (float) module.getTrailPoints().size());
            float[] rgba = com.xekka.visuals.util.ColorUtils.toRGBAFloats((color & 0xFFFFFF) | ((int) (alpha * 255) << 24));
            buf.vertex(matrix, (float) (p.x - cam.x), (float) (p.y - cam.y + 0.1), (float) (p.z - cam.z))
                    .color(rgba[0], rgba[1], rgba[2], rgba[3]);
            i++;
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    private static void drawEntityBoxOutline(Matrix4f matrix, Vec3d cam, Entity target, int color, float alpha) {
        double w = target.getWidth() / 2.0, h = target.getHeight();
        Vec3d pos = target.getPos();
        float x0 = (float) (pos.x - w - cam.x), y0 = (float) (pos.y - cam.y), z0 = (float) (pos.z - w - cam.z);
        float x1 = (float) (pos.x + w - cam.x), y1 = (float) (pos.y + h - cam.y), z1 = (float) (pos.z + w - cam.z);
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(2f);
        BufferBuilder line = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        float[] rgba = com.xekka.visuals.util.ColorUtils.toRGBAFloats((color & 0xFFFFFF) | ((int) (alpha * 255) << 24));
        float[][] corners = {{x0,y0,z0},{x1,y0,z0},{x1,y0,z1},{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1},{x0,y1,z0}};
        for (float[] c : corners) line.vertex(matrix, c[0], c[1], c[2]).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        BufferRenderer.drawWithGlobalProgram(line.end());
    }

    /** Small vertical diamond "crystal" shapes orbiting around the target at body height. */
    private static void drawOrbitingCrystals(Matrix4f matrix, Vec3d cam, Vec3d center, double radius, int color, float alpha, long timeMs) {
        int count = 3;
        double rotation = timeMs * 0.0025;
        float[] rgba = rgbaOf(color, alpha);
        Tessellator tess = Tessellator.getInstance();
        for (int i = 0; i < count; i++) {
            double a = rotation + (Math.PI * 2 * i) / count;
            float cx = (float) (center.x - cam.x + radius * Math.cos(a));
            float cy = (float) (center.y - cam.y);
            float cz = (float) (center.z - cam.z + radius * Math.sin(a));
            float s = 0.12f;
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            buf.vertex(matrix, cx, cy + s, cz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buf.vertex(matrix, cx + s, cy, cz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buf.vertex(matrix, cx, cy - s, cz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buf.vertex(matrix, cx - s, cy, cz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buf.vertex(matrix, cx, cy + s, cz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
            BufferRenderer.drawWithGlobalProgram(buf.end());
        }
    }

    /** A small downward-pointing arrow that bobs up and down above the target's head. */
    private static void drawArrowAbove(Matrix4f matrix, Vec3d cam, Vec3d center, double size, int color, float alpha, long timeMs) {
        float bob = (float) (Math.sin(timeMs * 0.004) * 0.1);
        float cx = (float) (center.x - cam.x), cy = (float) (center.y - cam.y + bob), cz = (float) (center.z - cam.z);
        float s = (float) size * 0.3f;
        float[] rgba = rgbaOf(color, alpha);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        buf.vertex(matrix, cx - s, cy + s, cz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        buf.vertex(matrix, cx + s, cy + s, cz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        buf.vertex(matrix, cx, cy, cz).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    private static void drawTracer(Matrix4f matrix, Vec3d cam, WorldRenderContext ctx, Entity target, int color, float alpha) {
        // Line starts at the camera itself (0,0,0 in camera-relative space) and ends at the target.
        Vec3d to = target.getPos().add(0, target.getHeight() / 2.0, 0);
        Tessellator tess = Tessellator.getInstance();
        RenderSystem.lineWidth(1.5f);
        BufferBuilder line = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        float[] rgba = com.xekka.visuals.util.ColorUtils.toRGBAFloats((color & 0xFFFFFF) | ((int) (alpha * 255) << 24));
        line.vertex(matrix, 0f, 0f, 0f).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        line.vertex(matrix, (float) (to.x - cam.x), (float) (to.y - cam.y), (float) (to.z - cam.z)).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        BufferRenderer.drawWithGlobalProgram(line.end());
    }

    public static void renderBlockOverlay(WorldRenderContext ctx, BlockOverlayModule module) {
        if (module == null) return;
        BlockPos pos = module.getLookedAtBlock();
        if (pos == null) return;
        Vec3d cam = ctx.camera().getPos();
        Matrix4f matrix = ctx.matrixStack().peek().getPositionMatrix();
        int color = module.resolveColor();
        int fillA = module.fillAlpha.getValue().intValue();
        drawBoxOutline(matrix, cam, pos, color, fillA, module.outlineWidth.getValue().floatValue());
    }

    // ---- low level draws ----

    private static void drawHorizontalRing(Matrix4f matrix, Vec3d cam, Vec3d centerWorld, double radius, int color, float alpha, int segments) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        float[] rgba = com.xekka.visuals.util.ColorUtils.toRGBAFloats((color & 0xFFFFFF) | ((int) (alpha * 255) << 24));
        float cx = (float) (centerWorld.x - cam.x), cy = (float) (centerWorld.y - cam.y), cz = (float) (centerWorld.z - cam.z);
        buf.vertex(matrix, cx, cy, cz).color(rgba[0], rgba[1], rgba[2], 0f);
        for (int i = 0; i <= segments; i++) {
            double a = (Math.PI * 2 * i) / segments;
            float x = (float) (cx + radius * Math.cos(a));
            float z = (float) (cz + radius * Math.sin(a));
            buf.vertex(matrix, x, cy, z).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    private static void drawBillboardRing(Matrix4f matrix, Vec3d cam, Vec3d centerWorld, double radius, int color, float alpha, int segments) {
        // Simplified: draws the ring on the horizontal plane; a true camera-facing billboard
        // would rotate basis vectors by the camera's yaw/pitch, omitted here for brevity.
        drawHorizontalRing(matrix, cam, centerWorld, radius, color, alpha, segments);
    }

    private static void drawBoxOutline(Matrix4f matrix, Vec3d cam, BlockPos pos, int color, int fillAlpha, float lineWidth) {
        Tessellator tess = Tessellator.getInstance();
        float x0 = (float) (pos.getX() - cam.x), y0 = (float) (pos.getY() - cam.y), z0 = (float) (pos.getZ() - cam.z);
        float x1 = x0 + 1, y1 = y0 + 1, z1 = z0 + 1;
        float[] rgbaFill = com.xekka.visuals.util.ColorUtils.toRGBAFloats((color & 0xFFFFFF) | (fillAlpha << 24));

        BufferBuilder fill = tess.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        fill.vertex(matrix, x0, y0, z0).color(rgbaFill[0], rgbaFill[1], rgbaFill[2], rgbaFill[3]);
        fill.vertex(matrix, x1, y0, z0).color(rgbaFill[0], rgbaFill[1], rgbaFill[2], rgbaFill[3]);
        fill.vertex(matrix, x0, y1, z0).color(rgbaFill[0], rgbaFill[1], rgbaFill[2], rgbaFill[3]);
        fill.vertex(matrix, x1, y1, z0).color(rgbaFill[0], rgbaFill[1], rgbaFill[2], rgbaFill[3]);
        BufferRenderer.drawWithGlobalProgram(fill.end());

        RenderSystem.lineWidth(lineWidth);
        BufferBuilder line = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        float[] rgbaLine = com.xekka.visuals.util.ColorUtils.toRGBAFloats(color | 0xFF000000);
        float[][] corners = {{x0,y0,z0},{x1,y0,z0},{x1,y0,z1},{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1},{x0,y1,z0}};
        for (float[] c : corners) line.vertex(matrix, c[0], c[1], c[2]).color(rgbaLine[0], rgbaLine[1], rgbaLine[2], rgbaLine[3]);
        BufferRenderer.drawWithGlobalProgram(line.end());
    }
}
