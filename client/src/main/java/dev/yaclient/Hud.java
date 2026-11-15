package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Utility per la HUD in gioco: riquadri compatti, grafica nitida, scala ridotta. */
public final class Hud {
    public static final float SCALE = 0.8f;
    public static final int BG = 0xC8101010;
    public static final int WHITE = 0xFFF5F5F5;
    public static final int TEXT = 10;
    public static final int H = 16;

    /** Dimensioni dello schermo nello spazio della HUD. */
    public static int vw, vh;
    /** Prossima posizione libera nelle colonne di sinistra e destra. */
    public static int leftY, rightY;

    private Hud() {}

    public static void begin() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        Gfx.setScale(Math.max(1, Math.round(sr.getScaleFactor() * SCALE)));
        Gfx.fade = 1f;
        vw = (int) (sr.getScaledWidth() / SCALE);
        vh = (int) (sr.getScaledHeight() / SCALE);
        leftY = 6;
        rightY = 6;
        GlStateManager.pushMatrix();
        GlStateManager.scale(SCALE, SCALE, 1f);
    }

    public static void end() {
        GlStateManager.popMatrix();
    }

    /** Riquadro con testo. Ritorna la larghezza. */
    public static int pill(int x, int y, String t) {
        int w = Gfx.width(t, TEXT, false) + 16;
        Gfx.rr(x, y, w, H, 5, BG);
        Gfx.text(t, x + 8, y + (H - Gfx.lineHeight(TEXT, false)) / 2, TEXT, false, WHITE);
        return w;
    }

    public static void left(String t) {
        pill(6, leftY, t);
        leftY += H + 4;
    }

    public static void right(String t) {
        int w = Gfx.width(t, TEXT, false) + 16;
        pill(vw - w - 6, rightY, t);
        rightY += H + 4;
    }

    /** Tasto premuto (tastiera o mouse). */
    public static boolean down(KeyBinding kb) {
        int code = kb.getKeyCode();
        if (code < 0) return Mouse.isButtonDown(code + 100);
        return Keyboard.isKeyDown(code);
    }
}
