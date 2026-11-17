package dev.yaclient;

import dev.yaclient.cosmetics.Logo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

/**
 * The small menu in the middle of the screen (Right Shift): logo, a MODS button and a cosmetics button,
 * plus two small shortcuts (HUD editor, settings). Mods and cosmetics are separate screens.
 */
public class HubScreen extends GuiScreen {
    private static final int BW = 104, BH = 22, SQ = 22, GAP = 6, SM = 18;

    private final float[] hover = new float[4];
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int x0, y0;

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
        int total = logo + 6 + 10 + BH + 8 + SM;
        int top = (height - total) / 2;
        Logo.draw(cx - logo / 2f, top, logo, logo, fade);
        int wy = top + logo + 6;

        int rowW = BW + GAP + SQ;
        x0 = cx - rowW / 2;
        y0 = wy + 10;
        // MODS
        button(0, x0, y0, BW, BH, mx, my, dt);
        Gfx.textCentered(Lang.t("hub.mods"), x0 + BW / 2, y0 + (BH - Gfx.lineHeight(10, true)) / 2, 10, true, Theme.TEXT);
        // cosmetics (shirt)
        button(1, x0 + BW + GAP, y0, SQ, BH, mx, my, dt);
        Gfx.icon("shirt", x0 + BW + GAP + (SQ - 14) / 2, y0 + (BH - 14) / 2, 14, Theme.TEXT);
        // small shortcuts
        int sy = y0 + BH + 8;
        int sx = cx - (2 * SM + GAP) / 2;
        button(2, sx, sy, SM, SM, mx, my, dt);
        Gfx.icon("move", sx + 3, sy + 3, 12, Theme.TEXT);
        button(3, sx + SM + GAP, sy, SM, SM, mx, my, dt);
        Gfx.icon("gear", sx + SM + GAP + 3, sy + 3, 12, Theme.TEXT);

        int hv = -1;
        for (int i = 0; i < 4; i++) if (hover[i] > 0.5f) hv = i;
        String[] tips = {"hub.mods", "hub.cosmetics", "hub.hud", "hub.settings"};
        if (hv >= 0) Gfx.textCentered(Lang.t(tips[hv]), cx, sy + SM + 6, 8, false, Theme.MUTED);
        Gfx.fade = 1f;
    }

    private int[] rect(int i) {
        int cx = width / 2, sy = y0 + BH + 8, sx = cx - (2 * SM + GAP) / 2;
        switch (i) {
            case 0: return new int[]{x0, y0, BW, BH};
            case 1: return new int[]{x0 + BW + GAP, y0, SQ, BH};
            case 2: return new int[]{sx, sy, SM, SM};
            default: return new int[]{sx + SM + GAP, sy, SM, SM};
        }
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
