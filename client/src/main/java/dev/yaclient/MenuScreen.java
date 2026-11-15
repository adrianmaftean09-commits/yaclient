package dev.yaclient;

import dev.yaclient.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menu moderno di YA CLIENT: barra laterale con categorie, schede con interruttori.
 * Solo "CLIENT" e' rosso; il resto e' nero / grigio scuro / bianco. Si chiude solo con ESC.
 */
public class MenuScreen extends GuiScreen {
    private static final int PW = 584, PH = 340, SB = 124;
    private static final int CW = 138, CH = 40, GAP = 8, VGAP = 6, GX = SB + 14, GY = 58;
    private static final int TAB_Y = 116, TAB_H = 30;

    private static final int WHITE = 0xFFF5F5F5;
    private static final int MUTED = 0xFF8A8A8A;
    private static final int BORDER = 0xFF262626;
    private static final int PANEL = 0xFF0F0F0F;
    private static final int SIDEBAR = 0xFF0A0A0A;
    private static final int CARD = 0xFF171717;
    private static final int CARD_HOVER = 0xFF1F1F1F;

    private static final String[] TABS = {"Tutti", "Info", "PvP", "Utilit\u00e0"};
    private static final String[] TITLES = {"Tutti i moduli", "Informazioni", "Combattimento", "Utilit\u00e0"};

    private final Map<Module, Float> anim = new HashMap<Module, Float>();
    private final Map<Module, Float> hover = new HashMap<Module, Float>();
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private float fade = 0.05f;
    private float scale = 1f;
    private int originX, originY;
    private int tab = 0;

    // ---------- dati ----------
    private static int category(String n) {
        switch (n) {
            case "FPS": case "Ping": case "Coordinate": case "Direzione":
            case "Orologio": case "Velocit\u00e0": case "Memoria": case "Salute":
                return 1;
            case "CPS": case "Keystrokes": case "Armatura": case "Pozioni": case "Reach":
                return 2;
            default:
                return 3;
        }
    }

    private static String desc(String n) {
        switch (n) {
            case "FPS": return "Fotogrammi al secondo";
            case "CPS": return "Click al secondo";
            case "Coordinate": return "Posizione X Y Z";
            case "Keystrokes": return "Tasti e mouse";
            case "Armatura": return "Armatura e durata";
            case "Toggle Sprint": return "Corsa automatica";
            case "Zoom (C)": return "Tieni premuto C";
            case "Ping": return "Latenza col server";
            case "Direzione": return "Punti cardinali";
            case "Orologio": return "Ora del computer";
            case "Velocit\u00e0": return "Blocchi al secondo";
            case "Memoria": return "RAM usata";
            case "Pozioni": return "Effetti attivi";
            case "Reach": return "Distanza dal bersaglio";
            case "Salute": return "Punti vita";
            case "Toggle Sneak": return "Accovacciati fisso";
            case "Luce Sempre": return "Luminosit\u00e0 massima";
            default: return "";
        }
    }

    private List<Module> visible() {
        List<Module> out = new ArrayList<Module>();
        for (Module m : YAClient.MODULES) {
            if (tab == 0 || category(m.name) == tab) out.add(m);
        }
        return out;
    }

    private static int cardX(int i) { return GX + (i % 3) * (CW + GAP); }
    private static int cardY(int i) { return GY + (i / 3) * (CH + VGAP); }
    private static int tabY(int i) { return TAB_Y + i * (TAB_H + 4); }

    private float lx(int mx) { return (mx - originX) / scale; }
    private float ly(int my) { return (my - originY) / scale; }

    private int c(int argb) {
        int a = (int) (((argb >>> 24) & 0xFF) * fade);
        return (a << 24) | (argb & 0xFFFFFF);
    }

    private static int lerp(int c1, int c2, float t) {
        int a = (int) (((c1 >>> 24) & 255) + (((c2 >>> 24) & 255) - ((c1 >>> 24) & 255)) * t);
        int r = (int) (((c1 >> 16) & 255) + (((c2 >> 16) & 255) - ((c1 >> 16) & 255)) * t);
        int g = (int) (((c1 >> 8) & 255) + (((c2 >> 8) & 255) - ((c1 >> 8) & 255)) * t);
        int b = (int) ((c1 & 255) + ((c2 & 255) - (c1 & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static float approach(float v, float target, float step) {
        float n = v + (target - v) * step;
        return Math.abs(target - n) < 0.01f ? target : n;
    }

    // ---------- disegno ----------
    @Override
    public void drawScreen(int mx, int my, float pt) {
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        fade = 0.05f + 0.95f * Math.min(1f, (now - opened) / 180f);
        scale = Math.min(1f, Math.min((height - 16f) / PH, (width - 16f) / PW));

        int gs = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        Gfx.setScale(Math.max(1, Math.round(gs * scale)));
        Gfx.fade = fade;

        originX = Math.round(width / 2f - PW * scale / 2f);
        originY = Math.round(height / 2f - PH * scale / 2f);
        float mxl = lx(mx), myl = ly(my);

        Gui.drawRect(0, 0, width, height, c(0xB0000000));

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) originX, (float) originY, 0f);
        GlStateManager.scale(scale, scale, 1f);

        // ombra morbida + pannello
        for (int i = 4; i >= 1; i--) {
            Gfx.rr(-i * 3, -i * 3 + 8, PW + i * 6, PH + i * 6, 14 + i * 3, 0x14000000);
        }
        Gfx.rr(0, 0, PW, PH, 14, BORDER);
        Gfx.rr(1, 1, PW - 2, PH - 2, 13, PANEL);

        // barra laterale
        Gfx.rr(1, 1, SB + 14, PH - 2, 13, SIDEBAR);
        Gui.drawRect(SB, 1, SB + 14, PH - 1, c(PANEL));
        Gui.drawRect(SB, 1, SB + 1, PH - 1, c(BORDER));

        // logo: YA bianco, CLIENT rosso (la dimensione si adatta alla larghezza disponibile)
        Gfx.text("YA", 16, 14, 36, true, WHITE);
        int cs = 20;
        while (cs > 10 && Gfx.width("CLIENT", cs, true) > SB - 30) cs--;
        Gfx.text("CLIENT", 16, 62, cs, true, YAClient.RED);
        Gfx.text("Client PvP 1.8.9", 16, 62 + Gfx.lineHeight(cs, true) + 2, 10, false, MUTED);

        // categorie
        for (int i = 0; i < TABS.length; i++) {
            int y = tabY(i);
            boolean sel = i == tab;
            boolean over = mxl >= 10 && mxl < SB - 10 && myl >= y && myl < y + TAB_H;
            if (sel) {
                Gfx.rr(10, y, SB - 20, TAB_H, 7, 0xFF1E1E1E);
                Gfx.rr(16, y + 8, 3, TAB_H - 16, 1, WHITE);
            } else if (over) {
                Gfx.rr(10, y, SB - 20, TAB_H, 7, 0xFF141414);
            }
            int lh = Gfx.lineHeight(12, sel);
            String label = Gfx.trim(TABS[i], 12, sel, SB - 44);
            Gfx.text(label, 28, y + (TAB_H - lh) / 2, 12, sel, sel ? WHITE : MUTED);
        }

        Gfx.text("ESC per chiudere", 16, PH - 36, 10, false, MUTED);
        Gfx.text("v1.0", 16, PH - 22, 10, false, 0xFF5A5A5A);

        // intestazione contenuto
        List<Module> list = visible();
        int on = 0;
        for (Module m : list) if (m.enabled) on++;
        Gfx.text(TITLES[tab], GX, 14, 20, true, WHITE);
        Gfx.text(list.size() + " moduli, " + on + " attivi", GX, 14 + Gfx.lineHeight(20, true) + 1, 10, false, MUTED);

        // schede
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            int x = cardX(i), y = cardY(i);
            boolean over = mxl >= x && mxl < x + CW && myl >= y && myl < y + CH;

            float h = hover.containsKey(m) ? hover.get(m) : 0f;
            h = approach(h, over ? 1f : 0f, Math.min(1f, dt / 90f));
            hover.put(m, h);

            float a = anim.containsKey(m) ? anim.get(m) : (m.enabled ? 1f : 0f);
            a = approach(a, m.enabled ? 1f : 0f, Math.min(1f, dt / 70f));
            anim.put(m, a);

            Gfx.rrb(x, y, CW, CH, 7, lerp(CARD, CARD_HOVER, h), lerp(BORDER, 0xFF3A3A3A, h));

            int tw = 30, th = 16;
            int tx = x + CW - 12 - tw, ty = y + 7;

            // testi: ognuno limitato alla propria area, cosi' non finiscono mai sopra l'interruttore
            String name = Gfx.trim(m.name, 11, true, tx - (x + 12) - 6);
            Gfx.text(name, x + 12, y + 6, 11, true, WHITE);
            String d = Gfx.trim(desc(m.name), 10, false, CW - 24);
            Gfx.text(d, x + 12, y + 6 + Gfx.lineHeight(11, true) + 1, 10, false, MUTED);

            Gfx.rr(tx, ty, tw, th, 8, lerp(0xFF343434, WHITE, a));
            int kx = tx + 2 + Math.round((tw - 4 - 12) * a);
            Gfx.rr(kx, ty + 2, 12, 12, 6, lerp(0xFF8A8A8A, 0xFF0F0F0F, a));
        }

        GlStateManager.popMatrix();
    }

    // ---------- input (senza "throws": compatibile con qualsiasi mappatura) ----------
    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        float x0 = lx(mx), y0 = ly(my);

        for (int i = 0; i < TABS.length; i++) {
            int y = tabY(i);
            if (x0 >= 10 && x0 < SB - 10 && y0 >= y && y0 < y + TAB_H) {
                tab = i;
                return;
            }
        }
        List<Module> list = visible();
        for (int i = 0; i < list.size(); i++) {
            int x = cardX(i), y = cardY(i);
            if (x0 >= x && x0 < x + CW && y0 >= y && y0 < y + CH) {
                list.get(i).toggle();
                Config.save();
                return;
            }
        }
    }

    /** Si chiude solo con ESC. */
    @Override
    protected void keyTyped(char typed, int key) {
        if (key == Keyboard.KEY_ESCAPE) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
