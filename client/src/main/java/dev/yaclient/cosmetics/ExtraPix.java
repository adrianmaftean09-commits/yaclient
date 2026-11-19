package dev.yaclient.cosmetics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single cosmetics (sold one by one, not in bundles): five capes, five wings, five bands and five pets.
 * Capes, wings and bands are pixel art; pets mix pixel (voxel) and smooth models.
 */
final class ExtraPix {
    private ExtraPix() {}

    interface Fn { float[] at(double x, double y); }

    private static float[] c(int hex) { return Vox.rgb(hex, 0.25f); }
    private static float[] g(int hex, float e) { return Vox.rgb(hex, e); }
    private static float[] k(int hex, float e, float m) { float[] a = Vox.rgb(hex, e); return new float[]{Math.min(1f, a[0] * m), Math.min(1f, a[1] * m), Math.min(1f, a[2] * m), a[3]}; }
    private static float noise(int a, int b) {
        long x = (a * 73856093L) ^ (b * 19349663L);
        x = (x ^ (x >>> 13)) * 1274126177L; x ^= x >>> 16;
        return (x & 0xFFFF) / 65535f;
    }
    private static double sm(double t) { t = t < 0 ? 0 : (t > 1 ? 1 : t); return t * t * (3 - 2 * t); }

    // ================================================================== CAPES (pixel grid 22 x 32)
    static boolean has(String slot, String style) {
        String[] a = slot.equals("cape") ? CAPES : slot.equals("wings") ? WINGS : slot.equals("bandana") ? BANDS : slot.equals("pet") ? PETS : null;
        if (a == null || style == null) return false;
        for (String x : a) if (x.equals(style)) return true;
        return false;
    }

    static final String[] CAPES = {"pirate", "love", "camo", "synth", "storm"};

    static void cape(String id, float age, boolean plus) {
        final int NU = 22, NV = 32;
        for (int i = 0; i < NU; i++) for (int j = 0; j < NV; j++) {
            float[] col;
            if (id.equals("pirate")) col = pirate(i, j, NU, NV, age);
            else if (id.equals("love")) col = love(i, j, NU, NV, age);
            else if (id.equals("camo")) col = camo(i, j, NU, NV, age);
            else if (id.equals("synth")) col = synth(i, j, NU, NV, age);
            else col = storm(i, j, NU, NV, age);
            double u0 = i / (double) NU, u1 = (i + 1) / (double) NU, v0 = j / (double) NV, v1 = (j + 1) / (double) NV;
            Poly.quad(CosmeticRender.capePt(u0, v0, age, plus), CosmeticRender.capePt(u1, v0, age, plus),
                    CosmeticRender.capePt(u1, v1, age, plus), CosmeticRender.capePt(u0, v1, age, plus), col, col, col, col, 1f);
        }
    }

    /** Pixel sprite: '#' = lit cell. Drawn centred at (cx, cy). */
    private static boolean spr(String[] rows, int i, int j, int cx, int cy) {
        int w = rows[0].length(), h = rows.length, x = i - (cx - w / 2), y = j - (cy - h / 2);
        return x >= 0 && y >= 0 && x < w && y < h && rows[y].charAt(x) == '#';
    }

    private static final String[] SKULL = {
            "..#######..", ".#########.", "###########", "##..###..##", "##..###..##", "###########", ".####.####.", "..#######..", "..#.#.#.#..", "..#.#.#.#.."};
    private static final String[] HEART = {
            "..##...##..", ".####.####.", "###########", "###########", "###########", ".#########.", "..#######..", "...#####...", "....###....", ".....#....."};

    private static float[] pirate(int i, int j, int NU, int NV, float age) {
        float[] base = Poly.mix(c(0x15151C), c(0x24242E), (float) (j / (double) NV));
        if (noise(i, j) > 0.93) base = c(0x2E2E3A);
        // crossbones behind the skull
        for (int t = 0; t < 12; t++) {
            int x1 = 5 + t, y1 = 6 + t, y2 = 17 - t;
            if ((i == x1 && j == y1) || (i == x1 + 1 && j == y1) || (i == x1 && j == y2) || (i == x1 + 1 && j == y2)) base = c(0xD8D0B8);
        }
        for (int[] kn : new int[][]{{4, 5}, {5, 6}, {17, 5}, {16, 6}, {4, 18}, {5, 17}, {17, 18}, {16, 17}}) if (i == kn[0] && j == kn[1]) base = c(0xD8D0B8);
        if (spr(SKULL, i, j, 11, 12)) {
            base = c(0xF2EEE0);
            if (SKULL[j - 7].charAt(i - 6) == '#' && (j - 7 == 3 || j - 7 == 4) && ((i - 6) == 2 || (i - 6) == 3 || (i - 6) == 7 || (i - 6) == 8)) base = c(0x15151C);
        }
        if (j >= 10 && j <= 11 && ((i == 8 || i == 9 || i == 13 || i == 14))) base = g(0xFF2A2A, 0.6f + 0.4f * (float) Math.sin(age * 0.2));   // glowing eyes
        if (i == 0 || i == NU - 1 || j == NV - 1 || j == 0) base = c(0xC21A22);
        else if (i == 1 || i == NU - 2 || j == NV - 2) base = Poly.mix(base, c(0xC21A22), 0.5f);
        return base;
    }

    private static float[] love(int i, int j, int NU, int NV, float age) {
        float[] base = Poly.mix(c(0xFF9EC4), c(0xFF5A94), (float) (j / (double) NV));
        // small hearts drifting up
        for (int h = 0; h < 5; h++) {
            int hx = 2 + (h * 5) % 18, hy = (int) ((((h * 11) - age * (0.10 + h * 0.015)) % 32 + 32) % 32);
            if ((i == hx && j == hy) || (i == hx + 2 && j == hy) || (i == hx + 1 && j == hy + 1)) base = c(0xFFE0EE);
            if ((i == hx + 1 && j == hy) ) base = c(0xFFE0EE);
        }
        boolean big = ((int) (age * 0.1) & 1) == 0;
        String[] h = HEART;
        if (spr(h, i, j, 11, 14)) {
            base = c(0xE0143C);
            if (i == 7 && j == 11 || i == 8 && j == 11 || i == 7 && j == 12) base = c(0xFFC0D0);                                       // shine
            if (big && ((i - 6) + (j - 9) < 1)) base = c(0xFFFFFF);
        } else {                                                                                                                       // outline around the heart
            if (spr(h, i - 1, j, 11, 14) || spr(h, i + 1, j, 11, 14) || spr(h, i, j - 1, 11, 14) || spr(h, i, j + 1, 11, 14)) base = c(0xFFFFFF);
        }
        if (i == 0 || i == NU - 1 || j == NV - 1) base = c(0xFFFFFF);
        else if (i == 1 || i == NU - 2 || j == NV - 2) base = Poly.mix(base, c(0xFFFFFF), 0.5f);
        return base;
    }

    private static float[] camo(int i, int j, int NU, int NV, float age) {
        // blocky blobs from two octaves of low-frequency noise
        double n = noise(i / 3 + 7, j / 3 + 2) * 0.6 + noise(i / 2 + 31, j / 2 + 13) * 0.4;
        float[] base = n < 0.30 ? c(0x2F3B22) : n < 0.52 ? c(0x5C6B35) : n < 0.74 ? c(0x4A3A22) : c(0xA89A62);
        if (noise(i, j) > 0.94) base = Poly.mix(base, c(0x1E2616), 0.5f);
        // dog-tag patch
        if (i >= 8 && i <= 13 && j >= 12 && j <= 17) { base = c(0xC9CED8); if (i == 8 || i == 13 || j == 12 || j == 17) base = c(0x8A90A0); if (j == 14 && i >= 9 && i <= 12) base = c(0x2A2D38); if (j == 15 && i >= 9 && i <= 11) base = c(0x2A2D38); }
        if (i == 0 || i == NU - 1 || j == NV - 1 || j == 0) base = c(0x181E10);
        else if ((i == 1 || i == NU - 2 || j == NV - 2) && ((i + j) & 1) == 0) base = c(0xD8C060);   // stitched edge
        return base;
    }

    private static float[] synth(int i, int j, int NU, int NV, float age) {
        double v = j / (double) NV;
        float[] base;
        int horizon = 19;
        if (j < horizon) {
            base = Poly.mix(c(0x2A0B52), c(0xFF4FA3), (float) Math.pow(j / (double) horizon, 1.4));
            // sun with horizontal cuts
            double dx = i - 10.5, dy = j - 12.5, d = Math.sqrt(dx * dx + dy * dy);
            if (d < 7.2) {
                boolean cut = j > 13 && ((j - 13) % 3 == 2);
                if (!cut) base = Poly.mix(c(0xFFE259), c(0xFF4F8B), (float) ((j - 5) / 14.0));
                base = g(((int) (base[0] * 255) << 16) | ((int) (base[1] * 255) << 8) | (int) (base[2] * 255), 0.8f);
            }
            if (noise(i, j) > 0.97 && j < 8) base = c(0xFFFFFF);                                    // stars
        } else {
            base = c(0x14062E);
            double depth = (j - horizon) / (double) (NV - horizon);
            // horizontal grid lines moving toward the viewer
            double ph = ((j - horizon) + (age * 0.08)) % 4.0;
            if (ph < 1.0 && j > horizon + 1) base = g(0x39F3FF, 0.8f);
            // converging vertical lines
            double xr = (i - 10.5) / (1 + depth * 2.6);
            if (Math.abs(xr - Math.round(xr / 2.2) * 2.2) < 0.35 / (1 + depth) + 0.02) base = g(0xFF4FD8, 0.8f);
            if (j == horizon) base = g(0xFFFFFF, 0.9f);
        }
        if (i == 0 || i == NU - 1 || j == NV - 1) base = c(0x0A0420);
        return base;
    }

    private static float[] storm(int i, int j, int NU, int NV, float age) {
        double flash = Math.max(0, Math.sin(age * 0.07)); flash = Math.pow(flash, 24);          // brief lightning flashes
        float[] base = Poly.mix(c(0x2B3A55), c(0x0F1626), (float) (j / (double) NV));
        base = Poly.mix(base, g(0xCFE3FF, 0.8f), (float) flash * 0.45f);
        // rain
        for (int r = 0; r < 12; r++) {
            int rx = (r * 7 + 3) % NU, ry = (int) (((r * 13 - age * 0.9) % 34 + 34) % 34);
            if ((i == rx && j == ry) || (i == rx - 1 && j == ry + 1)) base = c(0x6EA8E8);
        }
        // cloud
        int[][] cloud = {{4, 4, 18}, {3, 5, 19}, {2, 6, 20}, {2, 7, 20}, {3, 8, 19}};
        for (int[] row : cloud) if (j == row[0] + 0 && false) base = base;
        double cd = 0;
        double[][] puffs = {{6, 6, 3.4}, {10, 4.5, 4.2}, {15, 5.5, 3.8}, {18, 7, 3.0}, {4, 8, 2.6}, {12, 8, 3.6}};
        boolean in = false;
        for (double[] p : puffs) { double dx = i - p[0], dy = j - p[1]; if (dx * dx + dy * dy < p[2] * p[2]) in = true; }
        if (in) base = j > 7 ? c(0x6E7A94) : c(0xA9B6CE);
        // lightning bolt
        int[][] bolt = {{11, 9}, {10, 10}, {10, 11}, {9, 12}, {10, 13}, {9, 14}, {9, 15}, {8, 16}, {9, 17}, {8, 18}, {7, 19}, {8, 20}};
        for (int[] b : bolt) if (i == b[0] || i == b[0] + 1) { if (j == b[1]) base = g(flash > 0.2 ? 0xFFFFFF : 0xFFE24A, 1f); }
        if (flash > 0.2) for (int[] b : bolt) if ((i == b[0] - 1 || i == b[0] + 2) && j == b[1]) base = Poly.mix(base, g(0xFFF7B0, 0.9f), 0.6f);
        if (i == 0 || i == NU - 1 || j == NV - 1) base = c(0x0A0F1C);
        return base;
    }

    // ================================================================== WINGS (pixel cells in the wing plane)
    static final String[] WINGS = {"dragon", "butterfly", "crystal", "mecha", "dragonfly"};

    private static final double CELL = 0.020;
    private static final Map<Long, float[]> W = new LinkedHashMap<Long, float[]>();
    private static long key(int ix, int iy) { return ((long) (ix + 1000) << 20) | (iy + 1000); }

    private static void fill(double x0, double y0, double x1, double y1, Fn f) {
        for (int ix = (int) Math.floor(x0 / CELL); ix <= (int) Math.ceil(x1 / CELL); ix++)
            for (int iy = (int) Math.floor(y0 / CELL); iy <= (int) Math.ceil(y1 / CELL); iy++) {
                float[] col = f.at((ix + 0.5) * CELL, (iy + 0.5) * CELL);
                if (col != null) W.put(key(ix, iy), col);
            }
    }

    private static void seg(double x0, double y0, double x1, double y1, double w, float[] col) {
        double len = Math.hypot(x1 - x0, y1 - y0);
        int steps = Math.max(2, (int) (len / (CELL * 0.4)));
        for (int s = 0; s <= steps; s++) {
            double t = s / (double) steps, x = x0 + (x1 - x0) * t, y = y0 + (y1 - y0) * t;
            for (int ix = (int) Math.floor((x - w) / CELL); ix <= (int) Math.floor((x + w) / CELL); ix++)
                for (int iy = (int) Math.floor((y - w) / CELL); iy <= (int) Math.floor((y + w) / CELL); iy++) {
                    double px = (ix + 0.5) * CELL - x, py = (iy + 0.5) * CELL - y;
                    if (px * px + py * py <= w * w) W.put(key(ix, iy), col);
                }
        }
    }

    private static boolean inPoly(double x, double y, double[][] p) {
        boolean in = false;
        for (int i = 0, j = p.length - 1; i < p.length; j = i++)
            if ((p[i][1] > y) != (p[j][1] > y) && x < (p[j][0] - p[i][0]) * (y - p[i][1]) / (p[j][1] - p[i][1]) + p[i][0]) in = !in;
        return in;
    }

    /** A tapered feather/tongue: from (bx,by) at angle ang (deg), length len, half width w, curl bends it along its length. */
    private static void blade(double bx, double by, double deg, double len, double w, double curl, Fn colAtS) {
        double ang = Math.toRadians(deg);
        int steps = Math.max(6, (int) (len / (CELL * 0.35)));
        for (int s = 0; s <= steps; s++) {
            double t = s / (double) steps, a = ang + curl * t * t;
            double x = bx, y = by;
            // integrate the curved centre line
            double xx = bx, yy = by;
            for (int q = 1; q <= s; q++) { double tq = q / (double) steps, aq = ang + curl * tq * tq; xx += Math.cos(aq) * len / steps; yy += Math.sin(aq) * len / steps; }
            double ww = w * Math.pow(Math.sin(Math.PI * Math.min(1.0, 0.18 + 0.82 * t)), 0.55);
            float[] col = colAtS.at(t, 0);
            double nx = -Math.sin(a), ny = Math.cos(a);
            for (double o = -ww; o <= ww; o += CELL * 0.45) {
                int ix = (int) Math.floor((xx + nx * o) / CELL), iy = (int) Math.floor((yy + ny * o) / CELL);
                float[] cc = col;
                if (colAtS != null) { float[] e = colAtS.at(t, o / Math.max(1e-6, ww)); if (e != null) cc = e; }
                W.put(key(ix, iy), cc);
            }
        }
    }

    private static float WA = 1f;
    private static boolean outline = true;

    static void wing(String id, float age) {
        W.clear(); WA = 1f; outline = true;
        if (id.equals("dragon")) dragonWing(age);
        else if (id.equals("butterfly")) butterflyWing(age);
        else if (id.equals("crystal")) crystalWing(age);
        else if (id.equals("mecha")) mechaWing(age);
        else dragonflyWing(age);
        emitWing();
    }

    private static void emitWing() {
        final double hz = 0.011;
        for (Map.Entry<Long, float[]> e : W.entrySet()) {
            long kk = e.getKey();
            int ix = (int) (kk >> 20) - 1000, iy = (int) (kk & 0xFFFFF) - 1000;
            float[] col = e.getValue();
            boolean edge = !W.containsKey(key(ix - 1, iy)) || !W.containsKey(key(ix + 1, iy)) || !W.containsKey(key(ix, iy - 1)) || !W.containsKey(key(ix, iy + 1));
            float m = 0.97f + 0.05f * noise(iy, ix);
            if (outline && edge) m *= 0.62f;
            float[] cc = {Math.min(1f, col[0] * m), Math.min(1f, col[1] * m), Math.min(1f, col[2] * m), edge && outline ? col[3] * 0.4f : col[3]};
            double x0 = ix * CELL, x1 = x0 + CELL, y0 = iy * CELL, y1 = y0 + CELL;
            double[] a = Poly.p(x0, y0, -hz), b = Poly.p(x1, y0, -hz), cq = Poly.p(x1, y1, -hz), d = Poly.p(x0, y1, -hz);
            double[] a2 = Poly.p(x0, y0, hz), b2 = Poly.p(x1, y0, hz), c2 = Poly.p(x1, y1, hz), d2 = Poly.p(x0, y1, hz);
            Poly.quad(a, b, cq, d, cc, cc, cc, cc, WA);
            Poly.quad(a2, d2, c2, b2, cc, cc, cc, cc, WA);
            if (!W.containsKey(key(ix - 1, iy))) Poly.quad(a, d, d2, a2, cc, cc, cc, cc, WA);
            if (!W.containsKey(key(ix + 1, iy))) Poly.quad(b, b2, c2, cq, cc, cc, cc, cc, WA);
            if (!W.containsKey(key(ix, iy - 1))) Poly.quad(a, a2, b2, b, cc, cc, cc, cc, WA);
            if (!W.containsKey(key(ix, iy + 1))) Poly.quad(d, cq, c2, d2, cc, cc, cc, cc, WA);
        }
    }

    // --- dragon: leathery bat wing with finger bones and a scalloped edge
    private static void dragonWing(float age) {
        final double fl = Math.sin(age * 0.2) * 0.012;
        final double[][] mem = {{0.02, 0.00}, {0.12, 0.30}, {0.36, 0.62}, {0.74, 0.54}, {0.92, 0.10 + fl}, {0.80, 0.17}, {0.73, -0.14 + fl}, {0.57, 0.05},
                {0.45, -0.20 + fl}, {0.31, 0.00}, {0.17, -0.14}};
        fill(0, -0.3, 1.0, 0.7, new Fn() { public float[] at(double x, double y) {
            if (!inPoly(x, y, mem)) return null;
            float t = (float) sm((0.62 - y) / 0.8);
            float[] col = Poly.mix(c(0xC2253A), c(0x6E0F1E), t);
            if (noise((int) (x / CELL), (int) (y / CELL)) > 0.9) col = Poly.mix(col, c(0xE0485A), 0.5f);
            return col; } });
        float[] bone = c(0x3A0A12), horn = c(0xF2D9A0);
        double[][] tips = {{0.92, 0.10 + fl}, {0.73, -0.14 + fl}, {0.45, -0.20 + fl}, {0.17, -0.14}};
        for (double[] t : tips) seg(0.74, 0.54, t[0], t[1], 0.011, bone);
        seg(0.02, 0.00, 0.12, 0.30, 0.020, bone); seg(0.12, 0.30, 0.36, 0.62, 0.018, bone); seg(0.36, 0.62, 0.74, 0.54, 0.016, bone);
        seg(0.74, 0.54, 0.80, 0.68, 0.012, horn); seg(0.36, 0.62, 0.34, 0.72, 0.010, horn);      // claws
    }

    // --- butterfly: two rounded lobes with a dark border, white dots and blue eyespots
    private static void butterflyWing(float age) {
        final double fl = Math.sin(age * 0.16) * 0.02;
        fill(0, -0.3, 0.9, 0.8, new Fn() { public float[] at(double x, double y) {
            double ca = Math.cos(0.45), sa = Math.sin(0.45);
            double ux = ((x - 0.40) * ca + (y - 0.38 - fl) * sa) / 0.40, uy = (-(x - 0.40) * sa + (y - 0.38 - fl) * ca) / 0.29;
            double d = Math.sqrt(ux * ux + uy * uy);
            if (d <= 1) return butter(d, Math.atan2(uy, ux), x, y);
            double cb = Math.cos(-0.35), sb = Math.sin(-0.35);
            double vx = ((x - 0.27) * cb + (y + 0.10) * sb) / 0.27, vy = (-(x - 0.27) * sb + (y + 0.10) * cb) / 0.20;
            double e = Math.sqrt(vx * vx + vy * vy);
            if (e <= 1) return butter(e, Math.atan2(vy, vx) + 2, x, y);
            return null; } });
        seg(0.02, 0.02, 0.60, 0.50, 0.007, c(0x3A1A06)); seg(0.02, 0.02, 0.45, 0.05, 0.007, c(0x3A1A06)); seg(0.02, 0.02, 0.34, 0.30, 0.007, c(0x3A1A06));
    }
    private static float[] butter(double d, double ang, double x, double y) {
        if (d > 0.88) {
            int dots = (int) Math.floor(ang * 5 / Math.PI);
            return (d > 0.90 && d < 0.97 && dots % 2 == 0) ? c(0xFFFFFF) : c(0x2A1608);
        }
        if (d > 0.74) return c(0xFFD21F);
        if (d < 0.30) return c(0x2F6FE8);
        if (d < 0.42) return c(0x8CC0FF);
        return Poly.mix(c(0xFF8A1E), c(0xE8501A), (float) sm((d - 0.4) / 0.4));
    }

    // --- crystal: angular translucent shards, faceted light/dark halves
    private static void shard(final double bx, final double by, double deg, final double len, final double w) {
        final double ang = Math.toRadians(deg), ca = Math.cos(ang), sa = Math.sin(ang);
        final double[][] kite = {{0, 0}, {0.30 * len, w}, {len, 0}, {0.30 * len, -w}};
        fill(Math.min(bx, bx + ca * len) - w - 0.02, Math.min(by, by + sa * len) - w - 0.02, Math.max(bx, bx + ca * len) + w + 0.02, Math.max(by, by + sa * len) + w + 0.02, new Fn() {
            public float[] at(double x, double y) {
                double dx = x - bx, dy = y - by, along = dx * ca + dy * sa, across = -dx * sa + dy * ca;
                if (!inPoly(along, across, kite)) return null;
                float t = (float) Math.min(1, along / len);
                float[] col = Poly.mix(c(0x7DF3FF), c(0x9A6BFF), t);
                if (across > 0) col = new float[]{col[0] * 0.72f, col[1] * 0.72f, col[2] * 0.85f, col[3]};      // shaded facet
                if (Math.abs(across) < w * 0.07) col = g(0xFFFFFF, 0.9f);                                       // bright ridge
                col[3] = 0.55f + 0.3f * t;
                return col;
            }
        });
    }

    private static void crystalWing(float age) {
        WA = 0.82f; outline = true;
        double[] angs = {86, 70, 54, 38, 22, 8, -6};
        double[] lens = {0.38, 0.56, 0.74, 0.88, 0.82, 0.64, 0.42};
        for (int i = 0; i < angs.length; i++) shard(0.03 + i * 0.01, 0.02 + (6 - i) * 0.01, angs[i] + Math.sin(age * 0.15 + i) * 1.5, lens[i], 0.075);
        double[] a2 = {78, 46, 14};
        for (int i = 0; i < a2.length; i++) shard(0.05, 0.05, a2[i], 0.40 + 0.04 * i, 0.05);
    }

    // --- mecha: armoured plates with glowing stripes, a spinning gear and thruster flames
    private static void plate(final double bx, final double by, double deg, final double len, final double w, final int idx) {
        final double ang = Math.toRadians(deg), ca = Math.cos(ang), sa = Math.sin(ang);
        fill(Math.min(bx, bx + ca * len) - w - 0.02, Math.min(by, by + sa * len) - w - 0.02, Math.max(bx, bx + ca * len) + w + 0.02, Math.max(by, by + sa * len) + w + 0.02, new Fn() {
            public float[] at(double x, double y) {
                double dx = x - bx, dy = y - by, along = dx * ca + dy * sa, across = -dx * sa + dy * ca;
                if (along < 0 || along > len || Math.abs(across) > w * (1 - 0.35 * along / len)) return null;
                float[] col = Poly.mix(c(0xB4BAC8), c(0x6C7286), (float) (along / len));
                if (Math.abs(across) < w * 0.16) col = g(0x39F3FF, 0.9f);                                      // glowing stripe
                else if (((int) Math.floor(along / 0.07)) % 3 == 0 && along > 0.02) col = Poly.mix(col, c(0x3A3F50), 0.55f);   // panel gaps
                if (along > len * 0.9) col = c(0x2A2D38);
                return col;
            }
        });
    }

    private static void mechaWing(float age) {
        outline = true;
        double[] angs = {74, 50, 26, 2};
        double[] lens = {0.50, 0.70, 0.80, 0.62};
        for (int i = 0; i < angs.length; i++) plate(0.10, 0.06, angs[i], lens[i], 0.062, i);
        // thruster flames at the plate tips
        for (int i = 0; i < angs.length; i++) {
            double a = Math.toRadians(angs[i]), fl = 0.05 + 0.03 * Math.sin(age * 0.5 + i * 1.7);
            double tx = 0.10 + Math.cos(a) * lens[i], ty = 0.06 + Math.sin(a) * lens[i];
            seg(tx, ty, tx + Math.cos(a) * fl * 1.6, ty + Math.sin(a) * fl * 1.6, 0.020, g(0xFFE060, 0.95f));
            seg(tx, ty, tx + Math.cos(a) * fl, ty + Math.sin(a) * fl, 0.012, g(0xFFFFFF, 1f));
        }
        // spinning gear at the joint
        final double rot = age * 0.08;
        fill(-0.05, -0.08, 0.28, 0.25, new Fn() { public float[] at(double x, double y) {
            double dx = x - 0.14, dy = y - 0.12, d = Math.sqrt(dx * dx + dy * dy), a = Math.atan2(dy, dx) - rot;
            double tooth = 0.10 + 0.022 * (Math.sin(a * 8) > 0 ? 1 : 0);
            if (d > tooth) return null;
            if (d < 0.030) return c(0x2A2D38);
            if (d < 0.055) return g(0x39F3FF, 0.8f);
            return c(0xE0B040); } });
    }

    // --- dragonfly: slim translucent wings with veins and a dark tip mark
    private static void dragonflyWing(float age) {
        WA = 0.72f; outline = false;
        final double fl = Math.sin(age * 0.3) * 0.02;
        double[][] ws = {{0.40, 0.30 + fl, 0.85, 0.44, 0.095}, {0.38, 0.12 + fl * 0.6, 0.55, 0.40, 0.085}, {0.34, -0.03, 0.15, 0.36, 0.08}};
        for (final double[] w : ws) {
            fill(0, -0.4, 0.95, 0.8, new Fn() { public float[] at(double x, double y) {
                double ca = Math.cos(w[2]), sa = Math.sin(w[2]);
                double ux = ((x - w[0]) * ca + (y - w[1]) * sa) / w[3], uy = (-(x - w[0]) * sa + (y - w[1]) * ca) / w[4];
                double d = ux * ux + uy * uy;
                if (d > 1) return null;
                if (d > 0.78) return g(0x3FB7C9, 0.5f);                                                  // edge vein
                if (Math.abs(uy) < 0.12) return g(0x3FB7C9, 0.5f);                                       // main vein
                if (ux > 0.80 && Math.abs(uy) < 0.5) return g(0x1E4A5A, 0.4f);                           // dark tip mark
                if (((int) Math.floor((ux + 1) * 5.5)) % 2 == 0 && Math.abs(uy) < 0.85 && ((int) Math.floor((uy + 1) * 3)) % 3 == 1) return g(0x7ED8E8, 0.5f);
                return g(0xCFF6FF, 0.6f); } });
        }
    }

    // ================================================================== BANDS (voxel rings around the head)
    static final String[] BANDS = {"ninja", "crown", "halo", "cat", "rainbow"};
    private static final double BY = -0.37;

    private static void ring(double y, double hy, float[] top, float[] bot) {
        Vox.box(0, y, -0.27, 0.2825, hy, 0.0125, top, bot, 0);
        Vox.box(0, y, 0.27, 0.2825, hy, 0.0125, top, bot, 0);
        Vox.box(-0.27, y, 0, 0.0125, hy, 0.2575, top, bot, 0);
        Vox.box(0.27, y, 0, 0.0125, hy, 0.2575, top, bot, 0);
    }

    static void band(String id, float age, float limb) {
        Vox.TEXEL = 0.025; Vox.yo = 0; Vox.reset(); Vox.marker = null;
        if (id.equals("ninja")) {
            ring(BY, 0.05, c(0x1C1C26), c(0x0E0E14));
            Vox.box(0, BY, -0.285, 0.10, 0.045, 0.012, c(0xC9CEDB), c(0x8A90A4), 1);                 // steel plate
            Vox.box(0, BY, -0.298, 0.045, 0.020, 0.004, c(0x1C1C26), c(0x1C1C26), 1);               // emblem
            Vox.box(0, BY, -0.299, 0.012, 0.045, 0.004, c(0x1C1C26), c(0x1C1C26), 1);
            for (int t = 0; t < 2; t++) for (int s = 0; s < 6; s++) {
                double sw = Math.sin(age * 0.14 + s * 0.6 + t * 1.5) * 0.02 * s * 0.3;
                Vox.box(-0.04 + t * 0.08 + sw, BY + 0.03 + s * 0.055, 0.30 + s * 0.02, 0.026, 0.030, 0.012, c(0x1C1C26), c(0x2E2E3C), 1);
            }
        } else if (id.equals("crown")) {
            ring(BY - 0.01, 0.045, c(0xFFC21F), c(0xC98A00));
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4, x = Math.sin(a) * 0.268, z = -Math.cos(a) * 0.268;
                double h = (i % 2 == 0) ? 0.130 : 0.075;
                Vox.box(x, BY - 0.055 - h, z, 0.030, h, 0.030, c(0xFFD84A), c(0xD9A200), 1);
                Vox.box(x, BY - 0.055 - 2 * h - 0.020, z, 0.024, 0.024, 0.024, (i % 2 == 0) ? g(0xFF3B5C, 0.7f) : g(0x4FC3F7, 0.7f), g(0xFFFFFF, 0.5f), 1);
            }
            Vox.box(0, BY, -0.282, 0.032, 0.028, 0.010, g(0xFF3B5C, 0.7f), g(0xFF9AAE, 0.7f), 1);   // front ruby
        } else if (id.equals("halo")) {
            ring(BY, 0.04, c(0xFFFFFF), c(0xE4E8F4));
            ring(BY - 0.034, 0.008, c(0xFFD84A), c(0xE0B000));
            double bob = Math.sin(age * 0.12) * 0.012;
            for (int i = 0; i < 20; i++) {                                                 // floating golden halo
                double a = i * Math.PI / 10, x = Math.sin(a) * 0.17, z = Math.cos(a) * 0.17;
                Vox.box(x, BY - 0.34 + bob, z, 0.030, 0.012, 0.030, g(0xFFE680, 0.95f), g(0xFFC21F, 0.9f), 1);
            }
            for (int s2 = -1; s2 <= 1; s2 += 2) {                                          // tiny feather wings on the band
                Vox.box(s2 * 0.30, BY - 0.02, 0.0, 0.020, 0.030, 0.06, c(0xFFFFFF), c(0xDDE4FA), 1);
                Vox.box(s2 * 0.34, BY - 0.05 + Math.sin(age * 0.3) * 0.008, 0.0, 0.016, 0.022, 0.045, c(0xFFFFFF), c(0xDDE4FA), 1);
            }
        } else if (id.equals("cat")) {
            ring(BY, 0.04, c(0xFF8FB8), c(0xE0608F));
            for (int s = -1; s <= 1; s += 2) {
                double x = s * 0.17;
                Vox.box(x, BY - 0.110, -0.10, 0.085, 0.065, 0.040, c(0xFF8FB8), c(0xE0608F), 1);
                Vox.box(x + s * 0.014, BY - 0.205, -0.10, 0.060, 0.034, 0.040, c(0xFF8FB8), c(0xE0608F), 1);
                Vox.box(x + s * 0.026, BY - 0.270, -0.10, 0.034, 0.034, 0.040, c(0xFF8FB8), c(0xE0608F), 1);
                Vox.box(x, BY - 0.110, -0.145, 0.052, 0.046, 0.006, c(0xFFD3E4), c(0xFFB0CC), 1);
                Vox.box(x + s * 0.010, BY - 0.195, -0.145, 0.032, 0.028, 0.006, c(0xFFD3E4), c(0xFFB0CC), 1);
            }
            Vox.box(0, BY + 0.02, -0.285, 0.020, 0.020, 0.012, g(0xFFD21F, 0.6f), g(0xC98A00, 0.4f), 1);   // bell
            Vox.box(0, BY + 0.005, -0.290, 0.030, 0.006, 0.006, c(0xE0608F), c(0xE0608F), 1);
        } else {
            int[] rb = {0xE8262B, 0xFF8A1F, 0xFFD21F, 0x2FB34A, 0x2F6FE8, 0x8A3FD8};
            for (int i = 0; i < 6; i++) { double y = BY - 0.055 + i * 0.022; ring(y, 0.011, c(rb[i]), c(rb[i])); }
            for (int i = 0; i < 6; i++) for (int s = 0; s < 5; s++) {
                double sw = Math.sin(age * 0.13 + s * 0.5 + i) * 0.018 * s * 0.3;
                Vox.box((i - 2.5) * 0.034 + sw, BY + 0.05 + s * 0.05, 0.30 + s * 0.012, 0.016, 0.026, 0.010, c(rb[i]), c(rb[i]), 1);
            }
        }
        Vox.TEXEL = 0.0125;
    }

    // ================================================================== PETS
    static final String[] PETS = {"drake", "fox", "penguin", "panda", "axolotl"};

    static void pet(String id, float age) {
        Vox.TEXEL = 0.0185; Vox.reset(); Vox.marker = null; Vox.yo = -0.05;
        if (id.equals("drake")) drake(age);
        else if (id.equals("fox")) fox(age);
        else if (id.equals("penguin")) penguin(age);
        else if (id.equals("panda")) panda(age);
        else axolotl(age);
        Vox.TEXEL = 0.0125; Vox.yo = 0;
    }

    private static void drake(float age) {
        double ph = age * 0.28, hop = Math.abs(Math.sin(ph)) * 0.016;
        Vox.yo = -0.05 - hop;
        float[] gr = c(0x39B54A), dg = c(0x1E7F33), cr = c(0xFFF0C0);
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.034, 0.112, -0.006, 0.026, 0.020, 0.036, gr, dg, 3);
        Vox.box(0, 0.050, 0.004, 0.056, 0.050, 0.048, gr, dg, 1);
        Vox.box(0, 0.058, -0.040, 0.034, 0.040, 0.010, cr, c(0xE8D090), 1);                         // belly
        for (int i = 0; i < 4; i++) {                                                                // tail with a spade tip
            double sw = Math.sin(age * 0.18 - i * 0.7) * 0.012 * (i + 1) * 0.5;
            Vox.box(sw, 0.075 - i * 0.012, 0.062 + i * 0.034, 0.024 - i * 0.003, 0.020 - i * 0.003, 0.018, gr, dg, 4);
        }
        Vox.box(Math.sin(age * 0.18 - 2.8) * 0.02, 0.040, 0.205, 0.020, 0.020, 0.012, c(0xE8262B), c(0xA81A20), 4);
        double fl = Math.sin(age * 0.5) * 0.022;
        for (int s = -1; s <= 1; s += 2) {                                                           // wings
            Vox.box(s * 0.092, 0.010 - fl, 0.020, 0.040, 0.008, 0.034, c(0x2E8F42), dg, 5);
            Vox.box(s * 0.130, -0.004 - fl * 1.5, 0.028, 0.030, 0.008, 0.026, c(0xE8262B), c(0xA81A20), 5);
        }
        double hy = -0.058;
        Vox.box(0, hy, -0.016, 0.054, 0.046, 0.046, gr, dg, 0);
        Vox.box(0, hy + 0.026, -0.070, 0.032, 0.022, 0.022, gr, dg, 2);                              // snout
        Vox.box(-0.012, hy + 0.022, -0.094, 0.005, 0.005, 0.003, c(0x1B1B22), c(0x1B1B22), 2);
        Vox.box(0.012, hy + 0.022, -0.094, 0.005, 0.005, 0.003, c(0x1B1B22), c(0x1B1B22), 2);
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.030, hy - 0.010, -0.058, 0.014, 0.014, 0.004, g(0xFFE24A, 0.7f), g(0xFFE24A, 0.7f), 0);
            Vox.box(s * 0.030, hy - 0.010, -0.062, 0.006, 0.010, 0.002, c(0x1B1B22), c(0x1B1B22), 0);
            Vox.box(s * 0.038, hy - 0.056, -0.020, 0.010, 0.026, 0.010, cr, c(0xE8D090), 0);          // horns
            Vox.box(s * 0.042, hy - 0.090, -0.018, 0.006, 0.014, 0.006, cr, c(0xE8D090), 0);
        }
        Vox.box(0, hy - 0.052, 0.020, 0.010, 0.010, 0.026, c(0x2E8F42), dg, 0);                      // back spikes
    }

    private static void fox(float age) {
        double bob = Math.sin(age * 0.10) * 0.005;
        Vox.yo = -0.05 + bob;
        float[] or = c(0xF08A2A), dor = c(0xC8641A), wh = c(0xFFF6E8), bk = c(0x24202A);
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.040, 0.108, -0.020, 0.022, 0.022, 0.032, bk, bk, 3);        // black socks
        Vox.box(0, 0.056, 0.012, 0.058, 0.046, 0.062, or, dor, 1);
        Vox.box(0, 0.062, -0.044, 0.034, 0.036, 0.010, wh, wh, 1);                                                  // white chest
        for (int i = 0; i < 4; i++) {                                                                                // big bushy tail
            double sw = Math.sin(age * 0.16 - i * 0.7) * 0.016 * (i + 1) * 0.5;
            double sz = 0.030 + 0.012 * Math.sin(i * 1.3);
            Vox.box(0.030 + sw + i * 0.014, 0.070 - i * 0.016, 0.082 + i * 0.030, sz + 0.012, sz + 0.010, 0.022, i == 3 ? wh : or, i == 3 ? wh : dor, 4);
        }
        double hy = -0.050;
        Vox.box(0, hy, -0.014, 0.060, 0.044, 0.046, or, dor, 0);
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.040, hy + 0.020, -0.056, 0.024, 0.020, 0.010, wh, wh, 0);                                // white cheeks
            Vox.box(s * 0.046, hy - 0.058, -0.012, 0.020, 0.030, 0.014, or, dor, 0);                              // ears
            Vox.box(s * 0.048, hy - 0.092, -0.012, 0.012, 0.016, 0.010, bk, bk, 0);                               // black ear tips
            Vox.box(s * 0.028, hy - 0.006, -0.060, 0.008, 0.012, 0.003, bk, bk, 0);                               // eyes
        }
        Vox.box(0, hy + 0.012, -0.076, 0.020, 0.016, 0.022, wh, wh, 2);                                            // snout
        Vox.box(0, hy + 0.002, -0.096, 0.008, 0.007, 0.004, bk, bk, 2);                                            // nose
    }

    private static void penguin(float age) {
        double ph = age * 0.22, wad = Math.sin(ph) * 0.012;
        Vox.yo = -0.05;
        float[] bk = c(0x23262E), wh = c(0xFFFFFF), or = c(0xFF9A1F);
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.036 + wad * s * 0.5, 0.112, -0.030, 0.026, 0.014, 0.040, or, or, 3);   // orange feet
        Vox.box(wad, 0.050, 0.010, 0.060, 0.056, 0.050, bk, bk, 1);
        Vox.box(wad, 0.056, -0.042, 0.040, 0.046, 0.010, wh, wh, 1);                                                // white belly
        double fl = Math.sin(age * 0.35) * 0.014;
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.076 + wad, 0.044 - fl * s * 0.3, 0.004, 0.012, 0.040, 0.024, bk, bk, 2);   // flippers
        Vox.box(wad, -0.002, -0.010, 0.050, 0.006, 0.044, c(0x2F9BE8), c(0x1E6DB0), 1);                             // blue scarf
        Vox.box(wad + 0.036, 0.016, -0.042, 0.010, 0.026, 0.008, c(0x2F9BE8), c(0x1E6DB0), 1);
        double hy = -0.050;
        Vox.box(wad, hy, -0.010, 0.054, 0.044, 0.046, bk, bk, 0);
        Vox.box(wad, hy + 0.004, -0.048, 0.036, 0.034, 0.006, wh, wh, 0);                                           // white face
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(wad + s * 0.022, hy - 0.006, -0.056, 0.012, 0.014, 0.003, Poly.INK, Poly.INK, 0);
            Vox.box(wad + s * 0.022, hy - 0.010, -0.060, 0.004, 0.005, 0.002, wh, wh, 0);
            Vox.box(wad + s * 0.040, hy + 0.014, -0.054, 0.010, 0.007, 0.003, c(0xFFB0B0), c(0xFFB0B0), 0);        // blush
        }
        Vox.box(wad, hy + 0.016, -0.068, 0.014, 0.008, 0.012, or, or, 2);                                           // beak
    }

    private static void panda(float age) {
        double bob = Math.sin(age * 0.10) * 0.006;
        Vox.yo = -0.05 + bob;
        float[] w = c(0xFFFFFF), wd = c(0xE4E4EC), bl = c(0x23232B);
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.050, 0.108, -0.030, 0.030, 0.022, 0.040, bl, bl, 3);     // feet
        Vox.box(0, 0.050, 0.004, 0.066, 0.050, 0.052, w, wd, 1);
        Vox.box(0, 0.040, 0.004, 0.068, 0.020, 0.054, bl, bl, 1);                                                // black belt across the chest
        double eat = Math.sin(age * 0.3) * 0.012;
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.086, 0.040, -0.030, 0.024, 0.034, 0.024, bl, bl, 2);
        Vox.box(0.062, 0.020 + eat, -0.072, 0.012, 0.050, 0.012, c(0x6BCB4A), c(0x3E9A2A), 2);                       // bamboo
        Vox.box(0.062, 0.020 + eat, -0.072, 0.014, 0.004, 0.014, c(0x2E7A22), c(0x2E7A22), 2);
        Vox.box(0.062, -0.014 + eat, -0.072, 0.014, 0.004, 0.014, c(0x2E7A22), c(0x2E7A22), 2);
        Vox.box(0.072, -0.050 + eat, -0.072, 0.022, 0.010, 0.010, c(0x8EE070), c(0x6BCB4A), 2);                  // leaf
        double hy = -0.062;
        Vox.box(0, hy, -0.010, 0.064, 0.054, 0.052, w, wd, 0);
        for (int s = -1; s <= 1; s += 2) {
            Vox.box(s * 0.052, hy - 0.058, -0.006, 0.022, 0.022, 0.016, bl, bl, 0);                              // ears
            Vox.box(s * 0.030, hy - 0.004, -0.060, 0.022, 0.030, 0.006, bl, bl, 0);                              // eye patches
            Vox.box(s * 0.032, hy - 0.008, -0.067, 0.006, 0.008, 0.002, w, w, 0);
        }
        Vox.box(0, hy + 0.020, -0.062, 0.016, 0.012, 0.008, bl, bl, 0);                                          // nose
        Vox.box(0, hy + 0.040, -0.058, 0.014, 0.004, 0.004, bl, bl, 0);
    }

    private static void axolotl(float age) {
        double ph = age * 0.22, hop = Math.abs(Math.sin(ph)) * 0.012;
        Vox.yo = -0.05 - hop;
        float[] pk = c(0xFFB3C8), dp = c(0xF08AA8), rd = c(0xE8527A);
        for (int s = -1; s <= 1; s += 2) for (int i = 0; i < 2; i++)
            Vox.box(s * 0.062, 0.106, -0.040 + i * 0.080, 0.018, 0.016, 0.022, pk, dp, 3);                       // four little legs
        Vox.box(0, 0.064, 0.020, 0.058, 0.040, 0.092, pk, dp, 1);
        for (int i = 0; i < 5; i++) {                                                                            // flat tail fin
            double sw = Math.sin(age * 0.2 - i * 0.6) * 0.010 * (i + 1) * 0.5;
            Vox.box(sw, 0.072 + 0.004 * i, 0.128 + i * 0.030, 0.012, 0.030 - i * 0.003, 0.016, pk, dp, 4);
        }
        Vox.box(0, 0.050, 0.020, 0.010, 0.040, 0.080, rd, rd, 1);                                                // dorsal stripe
        double hy = 0.040;
        Vox.box(0, hy, -0.090, 0.074, 0.040, 0.044, pk, dp, 0);
        for (int s = -1; s <= 1; s += 2) for (int i = 0; i < 3; i++) {
            double sw = Math.sin(age * 0.25 + i * 1.1 + s) * 0.010;
            Vox.box(s * (0.090 + i * 0.012), hy - 0.030 + i * 0.026 + sw, -0.086, 0.010 + 0.002 * (2 - i), 0.010, 0.008, rd, c(0xFF8FB0), 0);    // gill stalks
            Vox.box(s * (0.108 + i * 0.012), hy - 0.030 + i * 0.026 + sw, -0.086, 0.008, 0.008, 0.008, c(0xFF8FB0), c(0xFF8FB0), 0);
        }
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.038, hy - 0.010, -0.138, 0.010, 0.010, 0.003, Poly.INK, Poly.INK, 0);
        Vox.box(0, hy + 0.022, -0.136, 0.022, 0.004, 0.003, rd, rd, 0);
        Vox.box(-0.026, hy + 0.016, -0.136, 0.004, 0.004, 0.003, rd, rd, 0);
        Vox.box(0.026, hy + 0.016, -0.136, 0.004, 0.004, 0.003, rd, rd, 0);
        for (int s = -1; s <= 1; s += 2) Vox.box(s * 0.054, hy + 0.012, -0.134, 0.010, 0.006, 0.003, c(0xFF8FA8), c(0xFF8FA8), 0);   // cheeks
    }
}
