package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

/** Menu principale di YA CLIENT: stesso stile del menu dei moduli. */
public class MainMenuScreen extends GuiScreen {
    private static final int LW = 400, LH = 360;
    private static final int BW = 240, BH = 34, BGAP = 10, BX = (LW - BW) / 2, BY = 158;
    private static final String[] LABELS = {"Gioca", "Multiplayer", "Impostazioni", "Esci"};

    private final float[] hover = new float[LABELS.length];
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private float scale = 1f;
    private int originX, originY;

    private static int lerp(int c1, int c2, float t) {
        int r = (int) (((c1 >> 16) & 255) + (((c2 >> 16) & 255) - ((c1 >> 16) & 255)) * t);
        int g = (int) (((c1 >> 8) & 255) + (((c2 >> 8) & 255) - ((c1 >> 8) & 255)) * t);
        int b = (int) ((c1 & 255) + ((c2 & 255) - (c1 & 255)) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int by(int i) { return BY + i * (BH + BGAP); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        Gfx.fade = 0.05f + 0.95f * Math.min(1f, (now - opened) / 250f);
        scale = Math.min(1f, Math.min((height - 16f) / LH, (width - 16f) / LW));

        int gs = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        Gfx.setScale(Math.max(1, Math.round(gs * scale)));

        originX = Math.round(width / 2f - LW * scale / 2f);
        originY = Math.round(height / 2f - LH * scale / 2f);
        float mxl = (mx - originX) / scale, myl = (my - originY) / scale;

        drawGradientRect(0, 0, width, height, 0xFF121212, 0xFF000000);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) originX, (float) originY, 0f);
        GlStateManager.scale(scale, scale, 1f);

        // logo
        int yaW = Gfx.width("YA", 64, true);
        Gfx.text("YA", (LW - yaW) / 2, 4, 64, true, 0xFFF5F5F5);
        int cs = 28;
        int clW = Gfx.width("CLIENT", cs, true);
        Gfx.text("CLIENT", (LW - clW) / 2, 4 + Gfx.lineHeight(64, true) - 4, cs, true, YAClient.RED);

        // pulsanti
        for (int i = 0; i < LABELS.length; i++) {
            int y = by(i);
            boolean over = mxl >= BX && mxl < BX + BW && myl >= y && myl < y + BH;
            hover[i] += ((over ? 1f : 0f) - hover[i]) * Math.min(1f, dt / 90f);
            boolean primary = i == 0;

            int fill = primary ? lerp(0xFFF5F5F5, 0xFFFFFFFF, hover[i]) : lerp(0xFF171717, 0xFF242424, hover[i]);
            int border = primary ? 0xFFF5F5F5 : lerp(0xFF2A2A2A, 0xFF4A4A4A, hover[i]);
            Gfx.rrb(BX, y, BW, BH, 9, fill, border);

            int lh = Gfx.lineHeight(14, true);
            int lw = Gfx.width(LABELS[i], 14, true);
            Gfx.text(LABELS[i], BX + (BW - lw) / 2, y + (BH - lh) / 2, 14, true, primary ? 0xFF101010 : 0xFFF5F5F5);
        }

        // piede
        String foot = "YA CLIENT v1.0  |  Minecraft 1.8.9";
        Gfx.text(foot, (LW - Gfx.width(foot, 10, false)) / 2, LH - 22, 10, false, 0xFF6A6A6A);

        GlStateManager.popMatrix();

        // account in basso a destra (sullo schermo, non nel layout centrale)
        Gfx.setScale(Math.max(1, gs));
        String acc = Minecraft.getMinecraft().getSession().getUsername();
        int aw = Gfx.width(acc, 10, false);
        Gfx.text(acc, width - aw - 10, height - 18, 10, false, 0xFF8A8A8A);
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        float x0 = (mx - originX) / scale, y0 = (my - originY) / scale;
        for (int i = 0; i < LABELS.length; i++) {
            int y = by(i);
            if (x0 >= BX && x0 < BX + BW && y0 >= y && y0 < y + BH) {
                Minecraft mc = Minecraft.getMinecraft();
                switch (i) {
                    case 0: mc.displayGuiScreen(new GuiSelectWorld(this)); break;
                    case 1: mc.displayGuiScreen(new GuiMultiplayer(this)); break;
                    case 2: mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)); break;
                    default: mc.shutdown(); break;
                }
                return;
            }
        }
    }

    /** Nel menu principale ESC non fa nulla. */
    @Override
    protected void keyTyped(char typed, int key) {}

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
