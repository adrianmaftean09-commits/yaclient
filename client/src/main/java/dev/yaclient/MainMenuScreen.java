package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

/** YA CLIENT title screen: same glass style as the in-game menu. */
public class MainMenuScreen extends GuiScreen {
    private static final int LW = 400, LH = 360;
    private static final int BW = 250, BH = 36, BGAP = 10, BX = (LW - BW) / 2, BY = 160;
    private static final String[] KEYS = {"menu.play", "menu.multiplayer", "menu.options", "menu.quit"};
    private static final String[] ICONS = {"sword", "globe", "gear", "close"};

    private final float[] hover = new float[KEYS.length];
    private float hoverMods;
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private float scale = 1f;
    private int originX, originY;

    private static int by(int i) { return BY + i * (BH + BGAP); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        Gfx.fade = 0.05f + 0.95f * Ui.ease((now - opened) / 300f);
        scale = Math.min(1f, Math.min((height - 16f) / LH, (width - 16f) / LW));

        int gs = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        Gfx.setScale(Math.max(1, Math.round(gs * scale)));

        originX = Math.round(width / 2f - LW * scale / 2f);
        originY = Math.round(height / 2f - LH * scale / 2f);
        float mxl = (mx - originX) / scale, myl = (my - originY) / scale;

        drawGradientRect(0, 0, width, height, 0xFF141418, 0xFF000000);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) originX, (float) originY, 0f);
        GlStateManager.scale(scale, scale, 1f);

        // logo: "YA" white, "CLIENT" brand red
        Gfx.textCentered("YA", LW / 2, 4, 64, true, Theme.TEXT);
        Gfx.textCentered("CLIENT", LW / 2, 4 + Gfx.lineHeight(64, true) - 4, 28, true, Theme.BRAND_RED);

        for (int i = 0; i < KEYS.length; i++) {
            int y = by(i);
            boolean over = mxl >= BX && mxl < BX + BW && myl >= y && myl < y + BH;
            hover[i] = Ui.approach(hover[i], over ? 1f : 0f, dt, 16f);
            boolean primary = i == 0;

            int fill = primary ? Ui.mix(Theme.accent(), 0xFFFFFFFF, hover[i] * 0.18f) : Ui.mix(0x14FFFFFF, 0x26FFFFFF, hover[i]);
            Gfx.rr(BX, y, BW, BH, 11, fill);
            if (!primary) Gfx.rrOutline(BX, y, BW, BH, 11, 1, Ui.mix(0x1AFFFFFF, 0x40FFFFFF, hover[i]));
            int fg = primary ? Theme.onAccent() : Theme.TEXT;
            String label = Lang.t(KEYS[i]);
            int lw = Gfx.width(label, 13, true);
            int ix = BX + (BW - lw - 26) / 2;
            Gfx.icon(ICONS[i], ix, y + (BH - 18) / 2, 18, fg);
            Gfx.text(label, ix + 26, y + (BH - Gfx.lineHeight(13, true)) / 2, 13, true, fg);
        }

        // client settings (module menu)
        int my2 = by(KEYS.length);
        boolean overMods = mxl >= BX && mxl < BX + BW && myl >= my2 && myl < my2 + BH;
        hoverMods = Ui.approach(hoverMods, overMods ? 1f : 0f, dt, 16f);
        String modsLabel = Lang.t("menu.client_settings");
        int mw = Gfx.width(modsLabel, 11, false);
        Gfx.icon("star", BX + (BW - mw - 22) / 2, my2 + 9, 16, Ui.mix(Theme.MUTED, Theme.TEXT, hoverMods));
        Gfx.text(modsLabel, BX + (BW - mw - 22) / 2 + 22, my2 + (BH - Gfx.lineHeight(11, false)) / 2, 11, false,
                Ui.mix(Theme.MUTED, Theme.TEXT, hoverMods));

        String foot = "YA CLIENT 1.1.0  |  Minecraft 1.8.9";
        Gfx.textCentered(foot, LW / 2, LH - 22, 10, false, Theme.DIM);

        GlStateManager.popMatrix();

        Gfx.setScale(Math.max(1, gs));
        String acc = Minecraft.getMinecraft().getSession().getUsername();
        Gfx.text(acc, width - Gfx.width(acc, 10, false) - 10, height - 18, 10, false, Theme.MUTED);
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        float x0 = (mx - originX) / scale, y0 = (my - originY) / scale;
        Minecraft mc = Minecraft.getMinecraft();
        for (int i = 0; i <= KEYS.length; i++) {
            int y = by(i);
            if (x0 >= BX && x0 < BX + BW && y0 >= y && y0 < y + BH) {
                switch (i) {
                    case 0: mc.displayGuiScreen(new GuiSelectWorld(this)); break;
                    case 1: mc.displayGuiScreen(new GuiMultiplayer(this)); break;
                    case 2: mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)); break;
                    case 3: mc.shutdown(); break;
                    default: mc.displayGuiScreen(new MenuScreen()); break;
                }
                return;
            }
        }
    }

    /** ESC does nothing on the title screen. */
    @Override
    protected void keyTyped(char typed, int key) {}

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
