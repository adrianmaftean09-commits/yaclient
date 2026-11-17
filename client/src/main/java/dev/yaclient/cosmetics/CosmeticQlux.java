package dev.yaclient.cosmetics;

import net.minecraft.client.renderer.GlStateManager;

/**
 * Geometry of the "Qlux" partner bundle: white and pearl, ice-blue light. Smooth 3D shapes only (no pixel
 * textures): white feather wings (CosmeticRender with the qlux palette), a satin cape with an animated
 * wordmark, a white band and a floating light spirit.
 */
final class CosmeticQlux {
    private CosmeticQlux() {}

    private static float smooth(float t) { t = t < 0 ? 0 : (t > 1 ? 1 : t); return t * t * (3 - 2 * t); }

    // ------------------------------------------------------------------ cape
    private static float[] capeColor(double u, double v, float age) {
        float[] c = Poly.mix(Poly.SNOW, Poly.PEARL, (float) (v * 0.55));
        // soft vertical satin folds
        double fold = 0.5 + 0.5 * Math.sin(u * Math.PI * 6 + v * 1.5);
        c = Poly.mix(c, Poly.SNOW, (float) (fold * 0.25));
        // silver trim along the edge and an ice-blue glow running along the hem
        double edge = Math.min(Math.min(u, 1 - u) * CosmeticRender.CAPE_W / 0.028, (1 - v) / 0.034);
        float trim = smooth((float) (1.0 - edge));
        c = Poly.mix(c, Poly.SILVER, trim * 0.9f);
        double hem = Math.sin(u * 9.0 - age * 0.07) * 0.5 + 0.5;
        float glow = smooth((float) ((v - 0.90) / 0.10)) * (0.35f + 0.65f * (float) hem);
        c = Poly.mix(c, Poly.ICE, glow * 0.75f);
        c[3] = Math.max(c[3], 0.25f + glow * 0.6f);
        return c;
    }

    static void capeBase(float age, boolean plus) {
        for (int i = 0; i < CosmeticRender.CAPE_C; i++) {
            for (int j = 0; j < CosmeticRender.CAPE_R; j++) {
                double u0 = i / (double) CosmeticRender.CAPE_C, u1 = (i + 1) / (double) CosmeticRender.CAPE_C;
                double v0 = j / (double) CosmeticRender.CAPE_R, v1 = (j + 1) / (double) CosmeticRender.CAPE_R;
                Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                        CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus),
                        capeColor(u0, v0, age), capeColor(u1, v0, age), capeColor(u1, v1, age), capeColor(u0, v1, age), 1f);
            }
        }
    }

    // wordmark placement on the cape
    private static final double TU0 = 0.10, TU1 = 0.90, TV0 = 0.10;
    private static final double TV1 = TV0 + (TU1 - TU0) * CosmeticRender.CAPE_W / CosmeticRender.CAPE_H * QluxText.H / (double) QluxText.W;

    /** The wordmark: dark graphite letters that gently wave, with a bright ice-blue light sweeping through them. */
    static void text(float age, boolean plus) {
        final int NX = 28, NY = 10;
        double sweep = ((age * 0.018) % 1.6) - 0.3;                 // light position in u-space, wraps around
        for (int i = 0; i < NX; i++) {
            for (int j = 0; j < NY; j++) {
                double a0 = i / (double) NX, a1 = (i + 1) / (double) NX, b0 = j / (double) NY, b1 = (j + 1) / (double) NY;
                tv(a0, b0, age, plus, sweep);
                tv(a1, b0, age, plus, sweep);
                tv(a1, b1, age, plus, sweep);
                tv(a0, b1, age, plus, sweep);
            }
        }
    }

    private static void tv(double a, double b, float age, boolean plus, double sweep) {
        double wave = Math.sin(age * 0.09 + a * 7.0) * 0.006 * (1.0 - b * 0.4);   // letters bob like a ribbon
        double u = TU0 + (TU1 - TU0) * a, v = TV0 + (TV1 - TV0) * b + wave;
        double[] p = CosmeticRender.capePt(Math.max(0, Math.min(1, u)), Math.max(0, Math.min(1, v)), age, plus);
        float d = (float) Math.abs(a - sweep);
        float lit = (float) Math.exp(-d * d * 60.0);                 // soft light band
        float pulse = 0.5f + 0.5f * (float) Math.sin(age * 0.05);
        float r = 0.07f + (0.45f - 0.07f) * lit + 0.04f * pulse;
        float g = 0.09f + (0.78f - 0.09f) * lit + 0.04f * pulse;
        float bl = 0.14f + (1.00f - 0.14f) * lit + 0.05f * pulse;
        Poly.tex(p[0], p[1], p[2] + 0.005, (float) a, (float) b, r, g, bl, 1f);
    }

    /** Whole cape: satin base plus the animated wordmark (own textured pass). */
    static void drawCape(float age, boolean plus) {
        Poly.begin();
        capeBase(age, plus);
        Poly.end();
        GlStateManager.enableTexture2D();
        QluxText.bind();
        Poly.beginTex();
        text(age, plus);
        Poly.end();
        GlStateManager.disableTexture2D();
    }

    // ------------------------------------------------------------------ pet: "Lumi", a little light spirit
    static void pet(float age) {
        float pulse = 0.85f + 0.15f * (float) Math.sin(age * 0.2);
        // body and head (one soft blob pair)
        Poly.ellipsoid(0, 0.01, 0.02, 0.095, 0.085, 0.10, Poly.SNOW, Poly.PEARL, 0.25f);
        Poly.ellipsoid(0, -0.075, -0.045, 0.082, 0.074, 0.076, Poly.SNOW, Poly.SNOW, 0.3f);
        // ears
        for (int s = -1; s <= 1; s += 2) {
            Poly.cone(s * 0.045, -0.125, -0.045, s * 0.07, -0.215, -0.055, 0.030, Poly.SNOW, Poly.ICE);
            // eyes (glowing ice blue) and cheeks
            Poly.ellipsoid(s * 0.034, -0.082, -0.112, 0.014, 0.017, 0.010, Poly.ICE, Poly.ICE, 1f);
            Poly.ellipsoid(s * 0.058, -0.052, -0.100, 0.012, 0.008, 0.008, Poly.PEARL, Poly.PEARL, 0f);
        }
        Poly.ellipsoid(0, -0.052, -0.120, 0.009, 0.006, 0.006, Poly.SILVER, Poly.SILVER, 0f);
        // halo ring above the head
        final int n = 28;
        double hy = -0.205 + Math.sin(age * 0.12) * 0.006;
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
            double r0 = 0.072, r1 = 0.088;
            double[] p0 = Poly.p(Math.cos(a0) * r0, hy, -0.045 + Math.sin(a0) * r0), p1 = Poly.p(Math.cos(a1) * r0, hy, -0.045 + Math.sin(a1) * r0);
            double[] q1 = Poly.p(Math.cos(a1) * r1, hy, -0.045 + Math.sin(a1) * r1), q0 = Poly.p(Math.cos(a0) * r1, hy, -0.045 + Math.sin(a0) * r1);
            float[] hc = Poly.mix(Poly.ICE, Poly.SNOW, (float) (0.5 + 0.5 * Math.sin(a0 * 2 - age * 0.25)));
            Poly.quad(p0, p1, q1, q0, hc, hc, hc, hc, 1f);
        }
        // tail: shrinking spheres ending in a sparkle
        final int T = 5;
        for (int i = 0; i < T; i++) {
            double f = i / (double) (T - 1);
            double x = Math.sin(age * 0.14 - i * 0.7) * 0.04 * f * 2.0;
            double y = 0.03 - 0.10 * f;
            double z = 0.10 + 0.20 * f;
            double r = 0.052 * (1 - f * 0.7);
            if (i < T - 1) Poly.ellipsoid(x, y, z, r, r, r, Poly.SNOW, Poly.PEARL, 0.2f);
            else Poly.ellipsoid(x, y - 0.01, z, r * 1.8 * pulse, r * 1.8 * pulse, r * 1.8 * pulse, Poly.ICE, Poly.SNOW, 1f);
        }
        // tiny feather wings
        double flap = Math.sin(age * 0.40) * 0.10;
        for (int s = -1; s <= 1; s += 2) {
            double[] root = Poly.p(s * 0.07, -0.01, 0.03);
            double[] t1 = Poly.p(s * 0.30, -0.15 - flap, 0.05);
            double[] t2 = Poly.p(s * 0.27, 0.01 - flap * 0.6, 0.12);
            double[] t3 = Poly.p(s * 0.14, 0.06 - flap * 0.2, 0.12);
            Poly.quad(root, t1, t2, root, Poly.SNOW, Poly.ICE, Poly.SNOW, Poly.SNOW, 1f);
            Poly.quad(root, t2, t3, root, Poly.SNOW, Poly.SNOW, Poly.PEARL, Poly.SNOW, 1f);
        }
    }
}
