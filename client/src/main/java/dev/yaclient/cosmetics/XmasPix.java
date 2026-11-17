package dev.yaclient.cosmetics;

import java.util.LinkedHashMap;
import java.util.Map;

/** Christmas bundles, fully pixelated (voxel wings, pixel cape, blocky band and pet): Santa and Frost. */
final class XmasPix {
    private XmasPix() {}

    private static final double CELL = 0.035;               // size of one wing "pixel"

    private static float[] c(int hex) { return Vox.rgb(hex, 0.25f); }
    private static float[] g(int hex, float e) { return Vox.rgb(hex, e); }
    private static float noise(int a, int b) {
        long x = (a * 73856093L) ^ (b * 19349663L);
        x = (x ^ (x >>> 13)) * 1274126177L; x ^= x >>> 16;
        return (x & 0xFFFF) / 65535f;
    }

    // ------------------------------------------------------------------ wings: the angel-wing feathers rasterised onto a pixel grid
    private static float[] wingColor(boolean santa, int row, int idx, double s, int ix, int iy) {
        if (santa) {
            if (row == 2) return c(0xFFFFFF);
            boolean stripe = ((idx + (s > 0.5 ? 1 : 0)) % 2 == 0);
            if (s > 0.86) return g(0xFFE27A, 0.7f);                       // golden tips
            return stripe ? c(0xD62A2A) : c(0xFFF5F0);                   // candy-cane stripes
        }
        float[] base = Poly.mix(c(0x6FC3F7), c(0xFFFFFF), (float) Math.min(1.0, s * 0.9 + (row == 2 ? 0.3 : 0.0)));
        if (noise(ix, iy) > 0.93f) return g(0xFFFFFF, 0.9f);              // glints
        if (s > 0.88) return g(0xE6F6FF, 0.7f);
        return base;
    }

    static void wing(Anime.Th t, float age) {
        boolean santa = t.index == 4;
        Map<Long, float[]> cells = new LinkedHashMap<Long, float[]>();
        for (int row = 2; row >= 0; row--) {
            double[] r = CosmeticRender.ROWS[row];
            int n = (int) r[0];
            for (int i = 0; i < n; i++) {
                double f = n == 1 ? 0 : i / (double) (n - 1);
                double tt = r[1] + (r[2] - r[1]) * f;
                double ang = Math.toRadians(r[3] + (r[4] - r[3]) * f);
                double len = r[5] + r[6] * Math.sin(Math.PI * f);
                double[] b = CosmeticRender.arm(tt);
                double dx = Math.cos(ang), dy = Math.sin(ang), nx = -dy, ny = dx, w = r[7] * 0.85;
                for (double s = 0; s <= 1.0001; s += 0.02) {
                    double bend = 0.14 * len * s * s;
                    double cx = b[0] + dx * len * s + nx * bend, cy = b[1] + dy * len * s + ny * bend;
                    double wp = Math.max(CELL * 0.55, w * Math.sin(Math.PI * (0.12 + 0.88 * s)));
                    int x0 = (int) Math.floor((cx - wp) / CELL), x1 = (int) Math.floor((cx + wp) / CELL);
                    int y0 = (int) Math.floor((cy - wp) / CELL), y1 = (int) Math.floor((cy + wp) / CELL);
                    for (int ix = x0; ix <= x1; ix++) for (int iy = y0; iy <= y1; iy++) {
                        double px = (ix + 0.5) * CELL - cx, py = (iy + 0.5) * CELL - cy;
                        if (px * px + py * py > wp * wp) continue;
                        cells.put(((long) (ix + 1000) << 20) | (iy + 1000), wingColor(santa, row, i, s, ix, iy));
                    }
                }
            }
        }
        Vox.TEXEL = CELL; Vox.marker = null; Vox.yo = 0; Vox.reset();
        for (Map.Entry<Long, float[]> e : cells.entrySet()) {
            long k = e.getKey();
            int ix = (int) (k >> 20) - 1000, iy = (int) (k & 0xFFFFF) - 1000;
            Vox.box((ix + 0.5) * CELL, (iy + 0.5) * CELL, 0.0, CELL / 2, CELL / 2, 0.016, e.getValue(), e.getValue(), 0);
        }
        Vox.TEXEL = 0.0125;
    }

    // ------------------------------------------------------------------ cape: pixel grid with a fur border
    static void capeBase(Anime.Th t, float age, boolean plus) {
        final int NU = 11, NV = 16;
        boolean santa = t.index == 4;
        for (int i = 0; i < NU; i++) for (int j = 0; j < NV; j++) {
            float[] col;
            boolean border = i == 0 || i == NU - 1 || j == 0 || j == NV - 1;
            float n = noise(i, j);
            if (santa) {
                if (border) col = c(0xFFFFFF);                                            // white fur outline
                else if (j >= NV - 3) col = ((i + j) % 2 == 0) ? c(0xFFFFFF) : c(0xF0E8E8);   // fluffy hem
                else if (i == 1 || i == NU - 2) col = c(0xB81E1E);
                else col = n > 0.85f ? c(0xE04040) : c(0xD62A2A);
            } else {
                if (border) col = c(0xFFFFFF);
                else if (j >= NV - 3) col = ((i + j) % 2 == 0) ? c(0xA8DCFA) : c(0xE6F6FF);   // icicle hem
                else if (i == 1 || i == NU - 2) col = c(0x7CCBF5);
                else col = n > 0.88f ? c(0xFFFFFF) : (n < 0.12f ? c(0xA8DCFA) : c(0xBFE6FF));
            }
            float k = 0.96f + 0.04f * noise(j, i);
            float[] cc = {Math.min(1f, col[0] * k), Math.min(1f, col[1] * k), Math.min(1f, col[2] * k), col[3]};
            double u0 = i / (double) NU, u1 = (i + 1) / (double) NU, v0 = j / (double) NV, v1 = (j + 1) / (double) NV;
            Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                    CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus), cc, cc, cc, cc, 1f);
        }
    }

    // ------------------------------------------------------------------ band (head space: y down, front -z)
    static void band(Anime.Th t, float age, float limbSwing) {
        final boolean santa = t.index == 4;
        Vox.TEXEL = 0.025; Vox.yo = 0; Vox.reset();
        Vox.marker = new Vox.Marker() {
            public float[] mark(int part, int a, int sa, double rx, double ry, double rz) {
                if (santa) {
                    if (ry < -0.45) return c(0xFFFFFF);                                  // fur trim along the top
                    return null;
                }
                if (part == 0 && a == 2 && sa < 0 && Math.abs(rx) < 0.22 && ry > -0.5 && ry < 0.7) {   // snowflake on the front
                    int ix = (int) Math.round(rx * 8), iy = (int) Math.round(ry * 3);
                    if (ix == 0 || iy == 0 || Math.abs(ix) == Math.abs(iy)) return c(0xFFFFFF);
                }
                return null;
            }
        };
        float[] top = santa ? c(0xD62A2A) : c(0x7CCBF5), bot = santa ? c(0xB81E1E) : c(0x5DB4EC);
        double y = -0.37, hy = 0.06;
        Vox.box(0, y, -0.27, 0.2825, hy, 0.0125, top, bot, 0);
        Vox.box(0, y, 0.27, 0.2825, hy, 0.0125, top, bot, 0);
        Vox.box(-0.27, y, 0, 0.0125, hy, 0.2575, top, bot, 0);
        Vox.box(0.27, y, 0, 0.0125, hy, 0.2575, top, bot, 0);
        Vox.marker = null;
        // pompom / tails at the back
        double sway = Math.sin(age * 0.12) * 0.02;
        if (santa) {
            for (int i = 0; i < 4; i++) Vox.box(sway * i * 0.4, y + 0.03 + i * 0.05, 0.30 + i * 0.012, 0.03, 0.026, 0.02, c(0xD62A2A), c(0xB81E1E), 1);
            Vox.box(sway * 1.6, y + 0.25, 0.35, 0.045, 0.045, 0.045, c(0xFFFFFF), c(0xF0E8E8), 1);
        } else {
            for (int s = -1; s <= 1; s += 2) for (int i = 0; i < 5; i++)
                Vox.box(s * 0.05 + Math.sin(age * 0.14 + i * 0.5 + s) * 0.02 * i * 0.3, y + 0.03 + i * 0.05, 0.30 + i * 0.015, 0.025, 0.026, 0.018, i % 2 == 0 ? c(0xFFFFFF) : c(0x7CCBF5), c(0x5DB4EC), 1);
        }
        Vox.TEXEL = 0.0125;
    }

    // ------------------------------------------------------------------ pets
    static void pet(Anime.Th t, float age) {
        Vox.TEXEL = 0.0185; Vox.reset();
        Vox.marker = null;
        if (t.index == 4) reindeer(age); else snowman(age);
        Vox.TEXEL = 0.0125;
    }

    private static void box(double cx, double cy, double cz, double hx, double hy, double hz, float[] top, float[] bot) {
        Vox.box(cx, cy, cz, hx, hy, hz, top, bot, 0);
    }

    private static void reindeer(float age) {
        float[] brown = c(0x9A6535), dbrown = c(0x6E4423), cream = c(0xFFF1DC);
        double ph = age * 0.32;
        double hop = Math.abs(Math.sin(ph)) * 0.016 + Math.sin(age * 0.07) * 0.006;
        Vox.yo = -0.045 - hop;
        double bob = Math.sin(ph * 2) * 0.004;
        Vox.marker = new Vox.Marker() {
            public float[] mark(int part, int a, int sa, double rx, double ry, double rz) {
                if (part == 1 && a == 0 && ry > 0.3) return c(0xFFF1DC);                  // cream belly
                if (part == 2 && a == 2 && sa < 0 && ry > 0.1) return c(0xFFF1DC);        // muzzle
                if (part == 3 && ry > 0.35) return c(0x2A1A14);                           // hooves
                return null;
            }
        };
        Vox.box(0, 0.02, 0.045, 0.068, 0.062, 0.115, brown, dbrown, 1);
        for (int s = -1; s <= 1; s += 2) for (int f = 0; f < 2; f++) {
            double lp = ph + (f + (s > 0 ? 1 : 0)) * Math.PI;
            double z = f == 0 ? -0.045 : 0.135, step = Math.sin(lp) * 0.026, lift = Math.max(0, Math.cos(lp)) * 0.016;
            Vox.box(s * 0.046, 0.108 - lift, z + step, 0.024, 0.036, 0.024, brown, dbrown, 3);
        }
        double hz = -0.115, hy = -0.072 + bob;
        Vox.box(0, hy, hz, 0.072, 0.064, 0.064, brown, brown, 0);
        Vox.box(0, hy + 0.030, hz - 0.074, 0.036, 0.026, 0.026, cream, cream, 2);
        Vox.box(0, hy + 0.012, hz - 0.101, 0.020, 0.018, 0.010, g(0xFF2A2A, 0.95f), g(0xFF6A5A, 0.95f), 0);   // glowing red nose
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.040, hy - 0.006, hz - 0.066, 0.014, 0.014, 0.004, c(0x1A0E0A), c(0x1A0E0A), 0);
            Vox.box(s * 0.045, hy - 0.010, hz - 0.069, 0.005, 0.005, 0.002, c(0xFFFFFF), c(0xFFFFFF), 0);
            Vox.box(s * 0.092, hy - 0.020, hz + 0.01, 0.026, 0.012, 0.012, brown, dbrown, 0);                 // ears
            // antlers: a trunk with two branches
            Vox.box(s * 0.040, hy - 0.100, hz + 0.02, 0.008, 0.040, 0.008, c(0xD8B56A), c(0xB89348), 0);
            Vox.box(s * 0.060, hy - 0.120, hz + 0.02, 0.020, 0.008, 0.008, c(0xD8B56A), c(0xB89348), 0);
            Vox.box(s * 0.052, hy - 0.092, hz + 0.02, 0.020, 0.007, 0.007, c(0xD8B56A), c(0xB89348), 0);
        }
        // Santa hat on top: red cap, white fur band, pompom
        Vox.box(0, hy - 0.072, hz + 0.01, 0.060, 0.012, 0.060, c(0xFFFFFF), c(0xF0E8E8), 0);
        Vox.box(0, hy - 0.100, hz + 0.02, 0.046, 0.018, 0.046, c(0xD62A2A), c(0xB81E1E), 0);
        Vox.box(0.012, hy - 0.128, hz + 0.03, 0.030, 0.012, 0.030, c(0xD62A2A), c(0xB81E1E), 0);
        Vox.box(0.032, hy - 0.140, hz + 0.034, 0.014, 0.014, 0.014, c(0xFFFFFF), c(0xF0E8E8), 0);
        // collar with a golden bell
        Vox.box(0, -0.012, -0.060, 0.070, 0.012, 0.018, g(0xE02B1A, 0.4f), g(0xB01E10, 0.3f), 0);
        Vox.box(0, 0.016, -0.082, 0.014, 0.014, 0.012, g(0xFFC21A, 0.9f), g(0xFFE27A, 0.9f), 0);
        // little white tail
        Vox.box(0, 0.0, 0.17, 0.026, 0.026, 0.026, c(0xFFFFFF), c(0xF0E8E8), 0);
        Vox.marker = null;
    }

    private static void snowman(float age) {
        double ph = age * 0.22;
        double hop = Math.abs(Math.sin(ph)) * 0.030 + Math.sin(age * 0.07) * 0.006;
        double sq = 1.0 - 0.05 * Math.max(0, -Math.sin(ph + 0.3));                // squash on landing
        Vox.yo = -0.02 - hop;
        float[] snow = c(0xFFFFFF), shade = c(0xDDEBF7);
        Vox.box(0, 0.075, 0.0, 0.080, 0.070 * sq, 0.080, snow, shade, 0);                // body ball (cube)
        Vox.box(0, -0.045, 0.0, 0.064, 0.056 * sq, 0.064, snow, shade, 0);
        double hy = -0.140;
        Vox.box(0, hy, 0.0, 0.054, 0.052, 0.054, snow, shade, 0);                        // head
        // face
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.022, hy - 0.012, -0.056, 0.008, 0.008, 0.003, c(0x1A1A22), c(0x1A1A22), 0);
            Vox.box(s * 0.030 + s * 0.012, hy + 0.020, -0.056, 0.005, 0.005, 0.003, c(0x1A1A22), c(0x1A1A22), 0);   // smile ends
        }
        Vox.box(0, hy + 0.026, -0.056, 0.020, 0.005, 0.003, c(0x1A1A22), c(0x1A1A22), 0);
        Vox.box(0, hy + 0.004, -0.078, 0.010, 0.010, 0.028, c(0xFF8A1E), c(0xE2560F), 0);   // carrot nose
        // buttons
        for (int i = 0; i < 3; i++) Vox.box(0, -0.075 + i * 0.045, -0.066 + (i == 2 ? -0.014 : 0), 0.008, 0.008, 0.003, c(0x1A1A22), c(0x1A1A22), 0);
        // top hat
        Vox.box(0, hy - 0.058, 0, 0.072, 0.006, 0.072, c(0x23232B), c(0x15151B), 0);
        Vox.box(0, hy - 0.100, 0, 0.044, 0.038, 0.044, c(0x2A2A33), c(0x15151B), 0);
        Vox.box(0, hy - 0.072, 0, 0.046, 0.010, 0.046, c(0xD62A2A), c(0xB81E1E), 0);
        // scarf with a tail that swings
        double sway = Math.sin(age * 0.18) * 0.02;
        Vox.box(0, hy + 0.062, 0, 0.062, 0.012, 0.062, c(0x2E9E4B), c(0x1F7A38), 0);
        Vox.box(0.044, hy + 0.098, -0.060 + sway, 0.016, 0.034, 0.008, c(0x2E9E4B), c(0xD62A2A), 0);
        // stick arms
        for (int s = -1; s <= 1; s += 2) {
            double lift = Math.sin(age * 0.18 + s) * 0.012;
            Vox.box(s * 0.114, -0.060 - lift, 0.0, 0.034, 0.006, 0.006, c(0x7A4A22), c(0x5A3416), 0);
            Vox.box(s * 0.150, -0.080 - lift, 0.0, 0.006, 0.020, 0.006, c(0x7A4A22), c(0x5A3416), 0);
        }
        // snow sparkle floating next to it
        Poly.ellipsoid(0.12, -0.20 + Vox.yo, 0.04, 0.016, 0.016, 0.016, g(0xFFFFFF, 1f), g(0xBFE6FF, 1f), 1f);
    }
}
