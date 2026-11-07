package com.xekka.visuals.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Projects a world-space point to 2D screen pixel coordinates using the camera's own position
 * and rotation, independent of the exact projection-matrix plumbing of any given MC version.
 * This manual perspective projection is intentionally self-contained (fov + aspect only) so it
 * keeps working even if internal renderer matrix classes shift between minor versions — the one
 * tradeoff is it ignores any custom Aspect Ratio/FOV-changer override unless you pass the same
 * fov/aspect those modules are using.
 */
public final class WorldToScreen {
    private WorldToScreen() {}

    public record Screen(double x, double y, double depth) {}

    public static Screen project(Vec3d worldPos, Camera camera, double fovDegrees, int screenW, int screenH) {
        Vec3d camPos = camera.getPos();
        Vector3f rel = new Vector3f(
                (float) (worldPos.x - camPos.x),
                (float) (worldPos.y - camPos.y),
                (float) (worldPos.z - camPos.z));

        // Rotate the relative vector into camera space using the inverse of the camera rotation.
        Quaternionf rot = new Quaternionf(camera.getRotation()).invert();
        rot.transform(rel);

        // Camera space in Minecraft looks down -Z; guard against points behind the camera.
        float camSpaceZ = -rel.z;
        if (camSpaceZ <= 0.05f) return null;

        double fovRad = Math.toRadians(fovDegrees);
        double aspect = (double) screenW / screenH;
        double tanHalfFov = Math.tan(fovRad / 2.0);

        double ndcX = rel.x / (camSpaceZ * tanHalfFov * aspect);
        double ndcY = rel.y / (camSpaceZ * tanHalfFov);

        double screenX = (ndcX * 0.5 + 0.5) * screenW;
        double screenY = (0.5 - ndcY * 0.5) * screenH;
        return new Screen(screenX, screenY, camSpaceZ);
    }
}
