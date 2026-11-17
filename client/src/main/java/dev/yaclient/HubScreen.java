package dev.yaclient;

import dev.yaclient.cosmetics.Logo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

/**
 * The small menu in the middle of the screen (Right Shift): logo, a MODS button and a cosmetics button,
 * plus a row with cosmetics, HUD editor and settings. Mods and cosmetics are separate screens.
 */
public class HubScreen extends GuiScreen {
    private static final int BW = 108, BH = 22, RH = 22, GAP = 6, SW = (BW - 2 * GAP) / 3;

    private final float[] hover = new float[4];
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int x0, y0;
    private int nx, ny;
    private static final int NW = 120, NH = 82;
    private float newsHover;

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        float fade = Ui.ease((now - opened) / 220f);
        drawGradientRect(0, 0, width, height, Ui.alpha(0x70000000, fade), Ui.alpha(0xA0000000, fade));

        int gs = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        Gfx.setScale(Math.max(1, gs));
        Gfx.fade = fade;

        int cx = width / 2;
        int logo = 50;
        int total = logo + 6 + 10 + BH + GAP + RH;
        int top = (height - total) / 2;
        Logo.draw(cx - logo / 2f, top, logo, logo, fade);
        int wy = top + logo + 6;

        x0 = cx - BW / 2;
        y0 = wy + 10;
        // MODS, centered
        button(0, x0, y0, BW, BH, mx, my, dt);
        Gfx.textCentered(Lang.t("hub.mods"), cx, y0 + (BH - Gfx.lineHeight(10, true)) / 2, 10, true, Theme.TEXT);
        // second row: cosmetics, HUD editor, settings - same total width as MODS, evenly spaced
        String[] icons = {"shirt", "move", "gear"};
        for (int k = 0; k < 3; k++) {
            int[] r = rect(k + 1);
            button(k + 1, r[0], r[1], r[2], r[3], mx, my, dt);
            Gfx.icon(icons[k], r[0] + (r[2] - 14) / 2, r[1] + (r[3] - 14) / 2, 14, Theme.TEXT);
        }
        int sy = y0 + BH + GAP;

        int hv = -1;
        for (int i = 0; i < 4; i++) if (hover[i] > 0.5f) hv = i;
        String[] tips = {"hub.mods", "hub.cosmetics", "hub.hud", "hub.settings"};
        if (hv >= 0) Gfx.textCentered(Lang.t(tips[hv]), cx, sy + RH + 6, 8, false, Theme.MUTED);
        // changelog card (bottom left), same as on the main menu
        nx = 10;
        ny = height - 20 - 8 - NH;
        boolean nOver = mx >= nx && mx < nx + NW && my >= ny && my < ny + NH;
        newsHover = Ui.approach(newsHover, nOver ? 1f : 0f, dt, 16f);
        Gfx.rr(nx, ny, NW, NH, 8, Ui.mix(0xB0101014, 0xD0242429, newsHover));
        int artH = Math.round((NW - 8) / NewsArt.ASPECT);
        NewsArt.draw(nx + 4, ny + 4, NW - 8, artH, fade);
        Gfx.rrOutline(nx, ny, NW, NH, 8, 1, Ui.mix(0x38FFFFFF, 0x80FFFFFF, newsHover));
        Gfx.text(Lang.t("news.card"), nx + 8, ny + 4 + artH + 5, 9, true, Theme.TEXT);
        Gfx.text(Gfx.trim("Update " + NewsScreen.VERSION + " · " + Lang.t("news.open"), 7, false, NW - 14), nx + 8, ny + 4 + artH + 18, 7, false, Theme.MUTED);

        Gfx.fade = 1f;
    }

    private int[] rect(int i) {
        if (i == 0) return new int[]{x0, y0, BW, BH};
        return new int[]{x0 + (i - 1) * (SW + GAP), y0 + BH + GAP, SW, RH};
    }

    private void button(int i, int x, int y, int w, int h, int mx, int my, float dt) {
        boolean over = mx >= x && mx < x + w && my >= y && my < y + h;
        hover[i] = Ui.approach(hover[i], over ? 1f : 0f, dt, 16f);
        Gfx.rr(x, y, w, h, 8, Ui.mix(0xB0141418, 0xD0292930, hover[i]));
        Gfx.rrOutline(x, y, w, h, 8, 1, Ui.mix(0x30FFFFFF, 0x77FFFFFF, hover[i]));
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mx >= nx && mx < nx + NW && my >= ny && my < ny + NH) { mc.displayGuiScreen(new NewsScreen(this)); return; }
        for (int i = 0; i < 4; i++) {
            int[] r = rect(i);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) {
                if (i == 0) mc.displayGuiScreen(new MenuScreen(0));
                else if (i == 1) mc.displayGuiScreen(new MenuScreen(4));
                else if (i == 2) mc.displayGuiScreen(new HudEditorScreen());
                else mc.displayGuiScreen(new MenuScreen(5));
                return;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
