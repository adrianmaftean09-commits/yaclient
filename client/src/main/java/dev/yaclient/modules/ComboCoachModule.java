package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Theme;
import dev.yaclient.Ui;

import java.util.Locale;

/**
 * Looks at the rhythm of your hits. After a hit the target is protected for 10 ticks (500 ms): hitting faster wastes clicks,
 * hitting slower gives them time to hit back. The coach shows your average gap between the last hits against that ideal.
 */
public class ComboCoachModule extends HudModule {
    private static final int N = 6;
    private final long[] t = new long[N];
    private int n, seenHits;

    public ComboCoachModule() { super("combo_coach", Category.PVP, false, 0.40f, 0.60f); }

    @Override public int width() { return 124; }

    @Override public int height() { return 40; }

    @Override
    public void onAlwaysTick() {
        if (!enabled) return;
        int h = HitTracker.hits();
        if (h < seenHits) seenHits = h;
        if (h > seenHits) {
            seenHits = h;
            if (n == N) { System.arraycopy(t, 1, t, 0, N - 1); n = N - 1; }
            t[n++] = HitTracker.lastHitTime();
        }
    }

    private float avg() {
        if (n < 2 || System.currentTimeMillis() - t[n - 1] > 3000) return -1f;
        float sum = 0f; int c = 0;
        for (int i = 1; i < n; i++) { long d = t[i] - t[i - 1]; if (d < 2000) { sum += d; c++; } }
        return c == 0 ? -1f : sum / c;
    }

    @Override
    protected void draw() {
        float a = avg();
        if (a < 0 && preview) a = 520f;
        Gfx.rr(0, 0, 124, 40, 8, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, 124, 40, 8, 1, 0x1AFFFFFF);
        String verdict; int col;
        if (a < 0) { verdict = "Hit to start"; col = Theme.MUTED; }
        else if (a < 430f) { verdict = "Too fast"; col = 0xFFEF4444; }
        else if (a <= 600f) { verdict = "Perfect"; col = 0xFF34D27B; }
        else { verdict = "Too slow"; col = 0xFFFFC21A; }
        Gfx.text("COMBO COACH", 8, 5, 8, true, Theme.DIM);
        Gfx.text(verdict, 8, 16, 10, true, col);
        if (a >= 0) {
            String s = String.format(Locale.ROOT, "%d ms", Math.round(a));
            Gfx.text(s, 116 - Gfx.width(s, 9, false), 17, 9, false, Theme.MUTED);
        }
        // gauge 300..800 ms with the ideal zone (450..600) marked
        Gfx.rr(8, 31, 108, 4, 2, 0x33FFFFFF);
        Gfx.rr(8 + Math.round(108 * (450f - 300f) / 500f), 31, Math.round(108 * 150f / 500f), 4, 2, 0x6634D27B);
        if (a >= 0) {
            float f = Ui.clamp((a - 300f) / 500f, 0f, 1f);
            Gfx.rr(8 + Math.round(f * 106f), 29, 3, 8, 1, col);
        }
    }
}
