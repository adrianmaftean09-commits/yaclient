package dev.yaclient.fabric.cos;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.yaclient.fabric.Config;
import dev.yaclient.fabric.CosmeticsApi;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix3f;
import org.joml.Vector3f;

/** The YA logo (with the gold Plus badge) floating above players that use YA CLIENT. Always faces the camera. */
final class Nametag {
    private Nametag() {}

    private static Vector3f ex, ey, ez, origin;

    private static float[] pt(float x, float y, float k) {
        return new float[]{origin.x + ex.x * x + ey.x * y + ez.x * k, origin.y + ex.y * x + ey.y * y + ez.y * k, origin.z + ex.z * x + ey.z * y + ez.z * k};
    }

    private static void v(float[] p, float u, float w, float r, float g, float b, float a) { Poly.tex(p[0], p[1], p[2], u, w, r, g, b, a); }

    /** A quad facing the camera, written with both windings. */
    private static void quad(float x0, float y0, float x1, float y1, float k, float r, float g, float b, float a, boolean tex) {
        float[] p00 = pt(x0, y0, k), p10 = pt(x1, y0, k), p11 = pt(x1, y1, k), p01 = pt(x0, y1, k);
        // y grows upward: v = 1 at the bottom of the image
        v(p01, 0, 1, r, g, b, a); v(p11, 1, 1, r, g, b, a); v(p10, 1, 0, r, g, b, a); v(p00, 0, 0, r, g, b, a);
        v(p00, 0, 0, r, g, b, a); v(p10, 1, 0, r, g, b, a); v(p11, 1, 1, r, g, b, a); v(p01, 0, 1, r, g, b, a);
    }

    static void draw(PoseStack ps, SubmitNodeCollector c, AvatarRenderState s, Player p) {
        if (!Config.getBool("nametag.badge", true)) return;
        if (!CosmeticsApi.usesClient(p.getUUID())) return;
        Minecraft mc = Minecraft.getInstance();
        Camera cam = mc.getEntityRenderDispatcher().camera;
        if (cam == null) return;
        Matrix3f rot = new Matrix3f().set(ps.last().pose());
        Matrix3f inv = new Matrix3f(rot).invert();
        // the pose already includes the view rotation: the screen axes are simply x, y, z in view space
        ex = inv.transform(new Vector3f(1, 0, 0), new Vector3f());
        ey = inv.transform(new Vector3f(0, 1, 0), new Vector3f());
        ez = inv.transform(new Vector3f(0, 0, 1), new Vector3f());
        float h = s.boundingBoxHeight + (p == mc.player ? 0.55f : 0.95f);
        // the layer space is upside down and shifted: world up = 1.501 - y
        origin = inv.transform(new Vector3f(0, 0, 0), new Vector3f());
        origin.set(0f, 1.501f - h, 0f);
        float L = 0.32f;
        boolean plus = CosmeticsApi.isPlus(p.getUUID());
        Gl.begin(ps, c);
        try {
            LogoTexture.bind();
            Poly.beginTex();
            quad(-L / 2, -L / 2, L / 2, L / 2, 0f, 1f, 1f, 1f, 1f, true);
            Poly.end();
            if (plus) plus(L);
        } finally { Gl.end(); }
    }

    private static void plus(float L) {
        float t = (float) ((System.nanoTime() / 1000000L) % 100000L) / 1000f;
        float pulse = 0.72f + 0.28f * (float) Math.sin(t * 3.2f);
        float cx = L * 0.5f - 0.02f, cy = L * 0.5f - 0.02f, s = 0.16f;
        Poly.begin();
        // glow: soft disc, bright in the middle, transparent at the rim
        int n = 20;
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
            float[] c0 = pt(cx, cy, 0.004f);
            float[] p0 = pt(cx + (float) Math.cos(a0) * s * 0.9f, cy + (float) Math.sin(a0) * s * 0.9f, 0.004f);
            float[] p1 = pt(cx + (float) Math.cos(a1) * s * 0.9f, cy + (float) Math.sin(a1) * s * 0.9f, 0.004f);
            v(c0, 0, 0, 1f, 0.78f, 0.2f, 0.35f * pulse); v(p0, 0, 0, 1f, 0.62f, 0.05f, 0f); v(p1, 0, 0, 1f, 0.62f, 0.05f, 0f); v(p1, 0, 0, 1f, 0.62f, 0.05f, 0f);
            v(c0, 0, 0, 1f, 0.78f, 0.2f, 0.35f * pulse); v(p1, 0, 0, 1f, 0.62f, 0.05f, 0f); v(p0, 0, 0, 1f, 0.62f, 0.05f, 0f); v(p0, 0, 0, 1f, 0.62f, 0.05f, 0f);
        }
        float a = s * 0.46f, b = Math.max(0.012f, s * 0.11f);
        quad(cx - a - 0.004f, cy - b - 0.004f, cx + a + 0.004f, cy + b + 0.004f, 0.008f, 0.80f, 0.55f, 0.05f, 0.95f, false);
        quad(cx - b - 0.004f, cy - a - 0.004f, cx + b + 0.004f, cy + a + 0.004f, 0.008f, 0.80f, 0.55f, 0.05f, 0.95f, false);
        quad(cx - a, cy - b, cx + a, cy + b, 0.012f, 1f, 0.86f, 0.30f, 1f, false);
        quad(cx - b, cy - a, cx + b, cy + a, 0.012f, 1f, 0.86f, 0.30f, 1f, false);
        Poly.end();
    }
}
