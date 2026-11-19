package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Theme;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Locale;

/** Small graph of your ping over the last minute, with the server TPS (worked out from the server clock). */
public class PingGraphModule extends HudModule {
    private static final int N = 60;
    private final int[] ping = new int[N];
    private int count, tick;
    private long lastWorld = -1, lastReal;
    private float tps = 20f;

    public PingGraphModule() { super("ping_graph", Category.INFO, false, 0.01f, 0.30f); }

    @Override public int width() { return 126; }

    @Override public int height() { return 54; }

    @Override
    public void onAlwaysTick() {
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) { lastWorld = -1; return; }
        if (!enabled) return;
        long w = mc.theWorld.getTotalWorldTime(), now = System.currentTimeMillis();
        if (lastWorld >= 0 && w != lastWorld && now - lastReal > 400) {
            float t = (w - lastWorld) / ((now - lastReal) / 1000f);
            if (t >= 0f && t <= 40f) tps = tps * 0.6f + Math.min(20f, t) * 0.4f;
        }
        if (w != lastWorld) { lastWorld = w; lastReal = now; }
        if (++tick % 20 != 0) return;
        int p = 0;
        try {
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            p = info == null ? 0 : Math.max(0, info.getResponseTime());
        } catch (Throwable ignored) { }
        System.arraycopy(ping, 1, ping, 0, N - 1);
        ping[N - 1] = p;
        if (count < N) count++;
    }

    private static int col(int ms) { return ms <= 80 ? 0xFF34D27B : (ms <= 160 ? 0xFFFFC21A : 0xFFEF4444); }

    @Override
    protected void draw() {
        Gfx.rr(0, 0, 126, 54, 8, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, 126, 54, 8, 1, 0x1AFFFFFF);
        int cur = preview && count == 0 ? 42 : ping[N - 1];
        Gfx.text(cur + " ms", 8, 5, 10, true, col(cur));
        String t = String.format(Locale.ROOT, "%.1f TPS", preview && count == 0 ? 20f : tps);
        Gfx.text(t, 118 - Gfx.width(t, 9, false), 6, 9, false, tps >= 18f ? Theme.MUTED : 0xFFEF4444);
        int max = 60;
        for (int i = 0; i < N; i++) max = Math.max(max, ping[i]);
        for (int i = 0; i < N; i++) {
            int v = preview && count == 0 ? 20 + (i * 7) % 30 : ping[i];
            if (v <= 0) continue;
            int h = Math.max(2, Math.round(26f * v / max));
            Gfx.rr(8 + i * 2, 46 - h, 1, h, 0, col(v));
        }
    }
}
