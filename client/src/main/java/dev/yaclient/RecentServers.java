package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.OldServerPinger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** The last servers you played on, shown on the title screen with their ping; click one to connect. */
public final class RecentServers {
    private RecentServers() {}

    private static final List<String> list = new ArrayList<String>();
    private static boolean loaded;
    private static String cur = "";
    /** The server you are (or were last) connected to, for auto reconnect. */
    public static volatile ServerData lastData;
    private static final Map<String, Long> pings = new ConcurrentHashMap<String, Long>();
    private static long lastPing;
    private static final int RH = 16, W = 120;

    private static void load() {
        loaded = true;
        String v = Config.get("recent.servers", "");
        if (v != null) for (String s : v.split(",")) if (!s.trim().isEmpty()) list.add(s.trim());
    }

    public static void tick(Minecraft mc) {
        try {
            if (!loaded) load();
            ServerData sd = mc.getCurrentServerData();
            if (mc.theWorld == null || sd == null || sd.serverIP == null) return;
            lastData = sd;
            if (sd.serverIP.equals(cur)) return;
            cur = sd.serverIP;
            list.remove(cur);
            list.add(0, cur);
            while (list.size() > 3) list.remove(list.size() - 1);
            Config.set("recent.servers", String.join(",", list));
            Config.save();
        } catch (Throwable ignored) { }
    }

    private static void refresh() {
        if (System.currentTimeMillis() - lastPing < 20000) return;
        lastPing = System.currentTimeMillis();
        for (final String ip : new ArrayList<String>(list)) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        ServerData sd = new ServerData("", ip, false);
                        OldServerPinger p = new OldServerPinger();
                        p.ping(sd);
                        for (int i = 0; i < 60 && sd.pingToServer <= 0; i++) { p.pingPendingNetworks(); Thread.sleep(100); }
                        pings.put(ip, sd.pingToServer > 0 ? sd.pingToServer : -1L);
                    } catch (Throwable e) { pings.put(ip, -1L); }
                }
            }, "YA-ServerPing");
            t.setDaemon(true);
            t.start();
        }
    }

    private static int top(int height) { return height - 20 - 8 - 82 - 10 - list.size() * RH - 14; }

    public static void draw(int mx, int my, int height) {
        if (!loaded) load();
        if (list.isEmpty()) return;
        refresh();
        int y0 = top(height);
        Gfx.text("RECENT", 12, y0, 8, true, Theme.DIM);
        for (int i = 0; i < list.size(); i++) {
            String ip = list.get(i);
            int y = y0 + 12 + i * RH;
            boolean over = mx >= 10 && mx < 10 + W && my >= y && my < y + RH - 2;
            Gfx.rr(10, y, W, RH - 2, 2, over ? 0xC0242429 : 0xA0101014);
            Gfx.rrOutline(10, y, W, RH - 2, 2, 1, over ? 0x66FFFFFF : 0x2EFFFFFF);
            Long p = pings.get(ip);
            String ps = p == null ? "..." : (p < 0 ? "--" : p + "ms");
            int pc = p == null || p < 0 ? Theme.DIM : (p <= 80 ? 0xFF34D27B : (p <= 160 ? 0xFFFFC21A : 0xFFEF4444));
            Gfx.text(ps, 10 + W - 6 - Gfx.width(ps, 7, false), y + 3, 7, false, pc);
            Gfx.text(Gfx.trim(ip, 8, false, W - 14 - Gfx.width(ps, 7, false)), 16, y + 3, 8, false, Theme.TEXT);
        }
    }

    public static boolean click(int mx, int my, int height, GuiScreen from) {
        if (list.isEmpty()) return false;
        int y0 = top(height);
        for (int i = 0; i < list.size(); i++) {
            int y = y0 + 12 + i * RH;
            if (mx >= 10 && mx < 10 + W && my >= y && my < y + RH - 2) {
                Minecraft mc = Minecraft.getMinecraft();
                mc.displayGuiScreen(new GuiConnecting(from, mc, new ServerData("YA", list.get(i), false)));
                return true;
            }
        }
        return false;
    }
}
