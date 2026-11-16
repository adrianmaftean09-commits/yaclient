package dev.yaclient;

import dev.yaclient.cosmetics.Logo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

/** YA CLIENT title screen: dark animated backdrop, centered logo, stacked buttons and a small icon dock. */
public class MainMenuScreen extends GuiScreen {
    private static final int BW = 200, BH = 22, GAP = 6;
    private static final String[] ICONS = {"shirt", "gear", "wrench", "close"};
    private static final String[] TIPS = {"tab.cosmetics", "menu.options", "tab.settings", "menu.quit"};

    private final float[] hover = new float[3];
    private final float[] dock = new float[ICONS.length];
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int bx, by0, dockX, dockY;

    private int btnY(int i) { return by0 + i * (BH + GAP); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float t = (now - opened) / 1000f;
        float fadeIn = Ui.ease((now - opened) / 500f);

        // backdrop: deep gradient + slow drifting soft glows (a cheap blurred-scene look)
        drawGradientRect(0, 0, width, height, 0xFF0B0B0F, 0xFF050507);
        for (int i = 0; i < 4; i++) {
            float px = width * (0.2f + 0.2f * i) + (float) Math.sin(t * 0.25f + i * 1.7f) * width * 0.08f;
            float py = height * (0.35f + 0.15f * (i % 2)) + (float) Math.cos(t * 0.2f + i) * height * 0.07f;
            int r = Math.round(Math.min(width, height) * (0.30f + 0.04f * i));
            for (int k = 6; k >= 1; k--) {
                int a = i % 2 == 0 ? 0x0E0E1318 : 0x0A1A0608;
                glow(px, py, r * k / 6f, (a >>> 24) == 0 ? 0 : a);
            }
        }
        // vignette
        drawGradientRect(0, 0, width, height / 4, 0x66000000, 0x00000000);
        drawGradientRect(0, height * 3 / 4, width, height, 0x00000000, 0x88000000);

        int gs = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        Gfx.setScale(Math.max(1, gs));
        Gfx.fade = fadeIn;

        int logo = Math.min(96, height / 4);
        int cx = width / 2;
        int total = logo + 34 + 3 * BH + 2 * GAP;
        int top = Math.max(16, (height - total) / 2 - 12);
        float bob = (float) Math.sin(t * 1.4f) * 1.5f;
        Logo.draw(cx - logo / 2f, top + bob, logo, logo, fadeIn);
        int wy = top + logo + 6;
        int yaw = Gfx.width("YA ", 14, true), clw = Gfx.width("CLIENT", 14, true);
        Gfx.text("YA ", cx - (yaw + clw) / 2, wy, 14, true, Theme.TEXT);
        Gfx.text("CLIENT", cx - (yaw + clw) / 2 + yaw, wy, 14, true, Theme.BRAND_RED);

        bx = cx - BW / 2;
        by0 = wy + 26;
        String[] labels = {Lang.t("menu.play"), Lang.t("menu.multiplayer"), Lang.t("menu.client_settings")};
        String[] icons = {"sword", "globe", "star"};
        for (int i = 0; i < 3; i++) {
            int y = btnY(i);
            boolean over = mx >= bx && mx < bx + BW && my >= y && my < y + BH;
            hover[i] = Ui.approach(hover[i], over ? 1f : 0f, dt, 16f);
            boolean primary = i == 2;
            Gfx.rr(bx, y, BW, BH, 6, primary ? Ui.mix(0xFFEDEDF0, 0xFFFFFFFF, hover[i]) : Ui.mix(0xE0101014, 0xF0202026, hover[i]));
            if (!primary) Gfx.rrOutline(bx, y, BW, BH, 6, 1, Ui.mix(0x1AFFFFFF, 0x44FFFFFF, hover[i]));
            int fg = primary ? 0xFF101014 : Theme.TEXT;
            int lw = Gfx.width(labels[i], 11, true) + 20;
            int ix = bx + (BW - lw) / 2;
            Gfx.icon(icons[i], ix, y + (BH - 14) / 2, 14, fg);
            Gfx.text(labels[i], ix + 20, y + (BH - Gfx.lineHeight(11, true)) / 2, 11, true, fg);
        }

        // icon dock
        int dsz = 22, dg = 6;
        dockX = cx - (ICONS.length * dsz + (ICONS.length - 1) * dg) / 2;
        dockY = btnY(3) + 6;
        for (int i = 0; i < ICONS.length; i++) {
            int x = dockX + i * (dsz + dg);
            boolean over = mx >= x && mx < x + dsz && my >= dockY && my < dockY + dsz;
            dock[i] = Ui.approach(dock[i], over ? 1f : 0f, dt, 16f);
            Gfx.rr(x, dockY, dsz, dsz, 6, Ui.mix(0xE0101014, 0xF0202026, dock[i]));
            Gfx.rrOutline(x, dockY, dsz, dsz, 6, 1, Ui.mix(0x1AFFFFFF, 0x44FFFFFF, dock[i]));
            Gfx.icon(ICONS[i], x + 4, dockY + 4, 14, Ui.mix(Theme.MUTED, Theme.TEXT, dock[i]));
            if (over) Gfx.textCentered(Lang.t(TIPS[i]), cx, dockY + dsz + 6, 9, false, Theme.MUTED);
        }

        Gfx.text("YA CLIENT 1.8.9", 8, height - 16, 10, false, Theme.DIM);
        String acc = Minecraft.getMinecraft().getSession().getUsername();
        Gfx.text(acc, width - Gfx.width(acc, 10, false) - 8, height - 16, 10, false, Theme.MUTED);
        Gfx.fade = 1f;
    }

    /** Soft circle approximated with stacked translucent rectangles (cheap, no textures). */
    private void glow(float cx, float cy, float r, int argb) {
        if (argb == 0) return;
        int steps = 14;
        for (int i = 0; i < steps; i++) {
            float yy = -r + 2 * r * i / steps, h = 2 * r / steps;
            float half = (float) Math.sqrt(Math.max(0, r * r - (yy + h / 2) * (yy + h / 2)));
            Gui.drawRect(Math.round(cx - half), Math.round(cy + yy), Math.round(cx + half), Math.round(cy + yy + h + 1), argb);
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        for (int i = 0; i < 3; i++) {
            int y = btnY(i);
            if (mx >= bx && mx < bx + BW && my >= y && my < y + BH) {
                if (i == 0) mc.displayGuiScreen(new GuiSelectWorld(this));
                else if (i == 1) mc.displayGuiScreen(new GuiMultiplayer(this));
                else mc.displayGuiScreen(new MenuScreen());
                return;
            }
        }
        int dsz = 22, dg = 6;
        for (int i = 0; i < ICONS.length; i++) {
            int x = dockX + i * (dsz + dg);
            if (mx >= x && mx < x + dsz && my >= dockY && my < dockY + dsz) {
                if (i == 0) mc.displayGuiScreen(new MenuScreen(4));
                else if (i == 1) mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                else if (i == 2) mc.displayGuiScreen(new MenuScreen(5));
                else mc.shutdown();
                return;
            }
        }
    }

    @Override
    protected void keyTyped(char typed, int key) {}

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
