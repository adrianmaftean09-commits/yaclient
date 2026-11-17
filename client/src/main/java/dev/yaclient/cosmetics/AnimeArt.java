package dev.yaclient.cosmetics;

import java.awt.image.BufferedImage;

/** Cape emblems of the anime bundles, drawn with vector strokes into a 512x512 texture (pure Java2D, no Minecraft deps). */
final class AnimeArt {
    private AnimeArt() {}
    static final int S = 512;

    /** Minimalist Minecraft-style pixel art: a 16x16 grid scaled up with hard pixels (use GL_NEAREST). */
    static final int G = 16, SCALE = 8;

    static BufferedImage paint(int theme) {
        int[][] px = new int[G][G];
        switch (theme) {
            case 0: sakura(px); break;
            case 1: shinobi(px); break;
            case 2: neon(px); break;
            default: kitsune(px); break;
        }
        BufferedImage img = new BufferedImage(G * SCALE, G * SCALE, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < G; y++) for (int x = 0; x < G; x++)
            for (int j = 0; j < SCALE; j++) for (int i = 0; i < SCALE; i++) img.setRGB(x * SCALE + i, y * SCALE + j, px[y][x]);
        return img;
    }

    private static final int PINK = 0xFFFF9CBF, LPINK = 0xFFFFD3E2, YEL = 0xFFFFD54A, DPINK = 0xFFE0709A;

    private static void disc(int[][] p, double cx, double cy, double r, int col) {
        for (int y = 0; y < G; y++) for (int x = 0; x < G; x++) {
            double dx = x + 0.5 - cx, dy = y + 0.5 - cy;
            if (dx * dx + dy * dy <= r * r) p[y][x] = col;
        }
    }

    private static void sakura(int[][] p) {
        // one five-petal blossom, two falling petals
        for (int i = 0; i < 5; i++) {
            double an = -Math.PI / 2 + i * Math.PI * 2 / 5;
            disc(p, 8 + Math.cos(an) * 3.6, 8 + Math.sin(an) * 3.6, 2.5, PINK);
        }
        for (int i = 0; i < 5; i++) {
            double an = -Math.PI / 2 + i * Math.PI * 2 / 5;
            disc(p, 8 + Math.cos(an) * 4.2, 8 + Math.sin(an) * 4.2, 1.0, LPINK);
        }
        disc(p, 8, 8, 1.6, YEL);
        p[2][13] = PINK; p[3][13] = LPINK; p[3][14] = PINK;
        p[13][2] = PINK; p[13][3] = LPINK; p[14][2] = LPINK;
    }

    private static void shinobi(int[][] p) {
        int silver = 0xFFDDE0F5, violet = 0xFF8C6BFF;
        for (int y = 0; y < G; y++) for (int x = 0; x < G; x++) {
            double dx = x + 0.5 - 7.6, dy = y + 0.5 - 8.0, ex = x + 0.5 - 10.0, ey = y + 0.5 - 6.6;
            if (dx * dx + dy * dy <= 6.3 * 6.3 && ex * ex + ey * ey > 5.6 * 5.6) p[y][x] = silver;
        }
        p[3][12] = violet; p[2][12] = violet; p[3][11] = violet; p[3][13] = violet; p[4][12] = violet;   // star
        p[12][12] = silver; p[13][13] = violet; p[10][13] = violet;
    }

    private static void neon(int[][] p) {
        int cyan = 0xFF00F0FF, mag = 0xFFFF2FBF, white = 0xFFFFFFFF;
        boolean[][] in = new boolean[G][G];
        for (int y = 0; y < G; y++) for (int x = 0; x < G; x++) {
            double u = (x + 0.5 - 8.0) / 5.9, v = -(y + 0.5 - 7.9) / 5.9;
            double q = u * u + v * v - 1;
            in[y][x] = q * q * q - u * u * v * v * v <= 0;
        }
        for (int y = 0; y < G; y++) for (int x = 0; x < G; x++) {
            if (!in[y][x]) continue;
            boolean edge = y == 0 || x == 0 || y == G - 1 || x == G - 1 || !in[y - 1][x] || !in[y + 1][x] || !in[y][x - 1] || !in[y][x + 1];
            p[y][x] = edge ? cyan : mag;
        }
        p[4][4] = white; p[4][5] = white; p[5][4] = white;
        p[1][14] = cyan; p[2][13] = cyan; p[2][14] = white; p[2][15] = cyan; p[3][14] = cyan;      // sparkle
        p[13][1] = mag; p[14][0] = mag; p[14][1] = white; p[14][2] = mag; p[15][1] = mag;
    }

    private static void kitsune(int[][] p) {
        String[] half = {
                "........",
                ".RR.....",
                ".RWR....",
                ".RWWRRRR",
                ".RWWWWWR",
                ".RWKKWWR",
                ".RWWWWWW",
                "..RWWWWW",
                "..RWWWWW",
                "...RWWWW",
                "...RWWWW",
                "....RWWK",
                ".....RWW",
                "......RR",
                ".......R",
                "........"};
        for (int y = 0; y < G; y++) for (int x = 0; x < 8; x++) {
            int c = 0;
            switch (half[y].charAt(x)) {
                case 'R': c = 0xFFE02B1A; break;
                case 'W': c = 0xFFFFFFFF; break;
                case 'K': c = 0xFF2A1215; break;
                default: c = 0;
            }
            p[y][x] = c; p[y][G - 1 - x] = c;
        }
    }
}
