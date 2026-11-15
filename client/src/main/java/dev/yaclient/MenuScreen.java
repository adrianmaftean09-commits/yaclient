package dev.yaclient;

import dev.yaclient.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

/** Menu moderno: pannello nero, schede con interruttori. Solo il nome e' rosso/bianco. */
public class MenuScreen extends GuiScreen {
    private static final int PW = 400, PAD = 14, HEAD = 56, CW = 182, CH = 36, GAP = 8;
    private static final int PH = HEAD + 4 * CH + 3 * GAP + 34;

    private static final int WHITE = 0xFFF5F5F5;
    private static final int MUTED = 0xFF8C8C8C;
    private static final int PANEL = 0xFF111111;
    private static final int BORDER = 0xFF2A2A2A;
    private static final int CARD = 0xFF1A1A1A;
    private static final int CARD_HOVER = 0xFF232323;

    private final Map<Module, Float> anim = new HashMap<>();
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private float fade = 0.1f;
    private float scale = 1f;
    private int originX, originY;

    // ---------- helpers grafici ----------
    private int c(int argb) {
        int a = (int) (((argb >>> 24) & 0xFF) * fade);
        return (a << 24) | (argb & 0xFFFFFF);
    }

    private void rr(int x, int y, int w, int h, int r, int color) {
        color = c(color);
        if (r <= 0) { Gui.drawRect(x, y, x + w, y + h, color); return; }
        Gui.drawRect(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            Gui.drawRect(x + inset, y + i, x + w - inset, y + i + 1, color);
            Gui.drawRect(x + inset, y + h - i - 1, x + w - inset, y + h - i, color);
        }
    }

    private void rrb(int x, int y, int w, int h, int r, int fill, int border) {
        rr(x, y, w, h, r, border);
        rr(x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }

    private static int lerp(int c1, int c2, float t) {
        int a = (int) (((c1 >>> 24) & 255) + (((c2 >>> 24) & 255) - ((c1 >>> 24) & 255)) * t);
        int r = (int) (((c1 >> 16) & 255) + (((c2 >> 16) & 255) - ((c1 >> 16) & 255)) * t);
        int g = (int) (((c1 >> 8) & 255) + (((c2 >> 8) & 255) - ((c1 >> 8) & 255)) * t);
        int b = (int) ((c1 & 255) + ((c2 & 255) - (c1 & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static String desc(Module m) {
        switch (m.name) {
            case "FPS": return "Fotogrammi al secondo";
            case "CPS": return "Click al secondo";
            case "Coordinate": return "Posizione X Y Z";
            case "Keystrokes": return "Tasti WASD e mouse";
            case "Armatura": return "Armatura e durabilita'";
            case "Toggle Sprint": return "Corsa automatica";
            case "Zoom (C)": return "Tieni premuto C";
            default: return "";
        }
    }

    private int cardX(int i) { return PAD + (i % 2) * (CW + GAP); }
    private int cardY(int i) { return HEAD + (i / 2) * (CH + GAP); }

    private float lx(int mx) { return (mx - originX) / scale; }
    private float ly(int my) { return (my - originY) / scale; }

    // ---------- disegno ----------
    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        fade = 0.1f + 0.9f * Math.min(1f, (now - opened) / 160f);
        scale = Math.min(1f, Math.min((height - 16f) / PH, (width - 16f) / PW));
        originX = Math.round(width / 2f - PW * scale / 2f);
        originY = Math.round(height / 2f - PH * scale / 2f);
        float mxl = lx(mx), myl = ly(my);

        Gui.drawRect(0, 0, width, height, c(0xB0000000));

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) originX, (float) originY, 0f);
        GlStateManager.scale(scale, scale, 1f);

        rrb(0, 0, PW, PH, 6, PANEL, BORDER);

        // titolo: solo YA bianco e CLIENT rosso
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) PAD, 14f, 0f);
        GlStateManager.scale(2f, 2f, 1f);
        int yaW = fontRendererObj.getStringWidth("YA ");
        fontRendererObj.drawString("YA ", 0, 0, c(WHITE));
        fontRendererObj.drawString("CLIENT", yaW, 0, c(YAClient.RED));
        GlStateManager.popMatrix();
        fontRendererObj.drawString("Client PvP 1.8.9", PAD, 36, c(MUTED));
        Gui.drawRect(PAD, HEAD - 8, PW - PAD, HEAD - 7, c(BORDER));

        // schede moduli
        for (int i = 0; i < YAClient.MODULES.size(); i++) {
            Module m = YAClient.MODULES.get(i);
            int x = cardX(i), y = cardY(i);
            boolean hover = mxl >= x && mxl < x + CW && myl >= y && myl < y + CH;
            rrb(x, y, CW, CH, 5, hover ? CARD_HOVER : CARD, BORDER);

            fontRendererObj.drawString(m.name, x + 10, y + 7, c(WHITE));
            String d = fontRendererObj.trimStringToWidth(desc(m), CW - 64);
            fontRendererObj.drawString(d, x + 10, y + 20, c(MUTED));

            float a = anim.containsKey(m) ? anim.get(m) : (m.enabled ? 1f : 0f);
            float target = m.enabled ? 1f : 0f;
            a += (target - a) * Math.min(1f, dt / 80f);
            if (Math.abs(target - a) < 0.01f) a = target;
            anim.put(m, a);

            int tx = x + CW - 42, ty = y + (CH - 16) / 2;
            rr(tx, ty, 32, 16, 8, lerp(0xFF3A3A3A, WHITE, a));
            int kx = tx + 2 + Math.round(16 * a);
            rr(kx, ty + 2, 12, 12, 6, lerp(0xFF8C8C8C, 0xFF111111, a));
        }

        // piede
        Gui.drawRect(PAD, PH - 28, PW - PAD, PH - 27, c(BORDER));
        fontRendererObj.drawString("Shift destro o Esc per chiudere", PAD, PH - 18, c(MUTED));
        String ver = "v1.0";
        fontRendererObj.drawString(ver, PW - PAD - fontRendererObj.getStringWidth(ver), PH - 18, c(MUTED));

        GlStateManager.popMatrix();
    }

    // ---------- input (senza "throws": compatibile con qualsiasi mappatura) ----------
    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        float x0 = lx(mx), y0 = ly(my);
        for (int i = 0; i < YAClient.MODULES.size(); i++) {
            int x = cardX(i), y = cardY(i);
            if (x0 >= x && x0 < x + CW && y0 >= y && y0 < y + CH) {
                YAClient.MODULES.get(i).toggle();
                return;
            }
        }
    }

    @Override
    protected void keyTyped(char typed, int key) {
        if (key == Keyboard.KEY_ESCAPE || key == YAClient.MENU_KEY) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
