package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Mod menu, same look as the 1.8.9 client: glass panel, segmented tabs, search, module cards and a HUD editor. */
public class MenuScreen extends Screen {
    private static final int PW = 780, PH = 450;
    private static final int CX = 24, VY = 124, VW = PW - 48, VH = PH - VY - 18;
    private static final int PANEL = 0xFC0F1012, SURF = 0xFF1A1B1F, SURF2 = 0xFF222328, SURF3 = 0xFF2D2E34;
    private static final int ACC = 0xFFE8E8E8, ON_ACC = 0xFF101014, OFF = 0xFF3A3B42;
    private static final String[] TABS = {"All", "Info", "PvP", "Crystal", "Utility"};
    private static final String[] TAB_ICONS = {"grid", "info", "sword", "target_hud", "wrench"};
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath("yaclient", "textures/gui/logo.png");

    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private int tab;
    private String query = "";
    private boolean searchFocus, editing;
    private float scroll, scrollTarget, contentH, scale;
    private int originX, originY;
    private float mxl, myl;
    private final Map<String, Float> anims = new HashMap<String, Float>();
    private HudModule dragging;
    private float dragDX, dragDY;
    private float dtMs;

    public MenuScreen() { super(Component.literal("YA CLIENT")); }

    @Override public boolean isPauseScreen() { return false; }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) { }

    private float anim(String key, float target, float speed) {
        float v = anims.containsKey(key) ? anims.get(key) : target;
        v = Ui.approach(v, target, dtMs, speed);
        anims.put(key, v);
        return v;
    }

    private boolean over(int x, int y, int w, int h) { return mxl >= x && mxl < x + w && myl >= y && myl < y + h; }

    private List<Module> visible() {
        List<Module> out = new ArrayList<Module>();
        String q = query.trim().toLowerCase(Locale.ROOT);
        for (Module m : Modules.ALL) {
            if (tab > 0 && m.category.ordinal() != tab - 1) continue;
            if (!q.isEmpty() && !m.name.toLowerCase(Locale.ROOT).contains(q) && !m.description.toLowerCase(Locale.ROOT).contains(q)) continue;
            out.add(m);
        }
        return out;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.currentTimeMillis();
        dtMs = Math.min(100, now - last);
        last = now;
        Gfx.g = g;
        int gs = Math.max(1, mc.getWindow().getGuiScale());
        if (editing) { renderEditor(g, mc, mx, my, gs); return; }

        float fade = 0.05f + 0.95f * Ui.ease((now - opened) / 200f);
        float open = Ui.ease((now - opened) / 240f);
        scale = Math.min(0.62f, Math.min((height - 16f) / PH, (width - 16f) / PW));
        float sc = scale * (0.95f + 0.05f * open);
        Gfx.setScale(Math.max(1, Math.round(gs * sc)));
        originX = Math.round(width / 2f - PW * scale / 2f);
        originY = Math.round(height / 2f - PH * scale / 2f);
        mxl = (mx - width / 2f) / sc + PW / 2f;
        myl = (my - height / 2f) / sc + PH / 2f;
        scroll = Ui.approach(scroll, scrollTarget, dtMs, 16f);

        g.fill(0, 0, width, height, Ui.alpha(0x99000000, fade));
        g.pose().pushMatrix();
        g.pose().translate(width / 2f, height / 2f);
        g.pose().scale(sc, sc);
        g.pose().translate(-PW / 2f, -PH / 2f);
        Gfx.fade = fade;

        for (int i = 5; i >= 1; i--) Gfx.rr(-i * 3, -i * 3 + 10, PW + i * 6, PH + i * 6, 18 + i * 3, 0x12000000);
        Gfx.rr(0, 0, PW, PH, 18, PANEL);
        Gfx.rrOutline(0, 0, PW, PH, 18, 1, 0x24FFFFFF);
        drawTabs();
        drawHeader(g);
        drawBody(g, fade);

        Gfx.fade = 1f;
        g.pose().popMatrix();
    }

    private void drawTabs() {
        int[] ws = new int[TABS.length];
        int total = 0;
        for (int i = 0; i < TABS.length; i++) { ws[i] = Gfx.width(TABS[i], 12, true) + 46; total += ws[i]; }
        int y = 70, x = CX;
        Gfx.rr(x, y, total + 8, 38, 11, SURF);
        int px = x + 4;
        for (int i = 0; i < TABS.length; i++) {
            boolean active = tab == i;
            float h = anim("nav" + i, active ? 1f : (over(px, y + 4, ws[i], 30) ? 0.5f : 0f), 16f);
            if (h > 0.01f) Gfx.rr(px, y + 4, ws[i], 30, 8, Ui.alpha(active ? SURF3 : SURF2, Math.min(1f, h)));
            int fg = active ? Theme.TEXT : Ui.mix(Theme.MUTED, Theme.TEXT, h);
            Gfx.icon(TAB_ICONS[i], px + 13, y + 4 + 6, 18, fg);
            Gfx.text(TABS[i], px + 36, y + 4 + (30 - Gfx.lineHeight(12, true)) / 2, 12, true, fg);
            px += ws[i];
        }
        String lab = "HUD editor";
        int w = Gfx.width(lab, 11, false) + 48, bx = PW - 24 - w;
        boolean ov = over(bx, y, w, 38);
        Gfx.rr(bx, y, w, 38, 11, ov ? SURF2 : SURF);
        Gfx.icon("move", bx + 14, y + 10, 18, ov ? Theme.TEXT : Theme.MUTED);
        Gfx.text(lab, bx + 38, y + (38 - Gfx.lineHeight(11, false)) / 2, 11, false, ov ? Theme.TEXT : Theme.MUTED);
    }

    private void drawHeader(GuiGraphicsExtractor g) {
        g.blit(RenderPipelines.GUI_TEXTURED, LOGO, 24, 14, 0f, 0f, 38, 38, 128, 128, 128, 128, Ui.alpha(0xFFFFFFFF, Gfx.fade));
        int ls = 18;
        Gfx.text("CLIENT", 70, 16, ls, true, Theme.BRAND_RED);
        int total = visible().size(), on = 0;
        for (Module m : visible()) if (m.enabled) on++;
        Gfx.text(total + " mods  -  " + on + " on", 70, 16 + Gfx.lineHeight(ls, true) + 2, 10, false, Theme.MUTED);

        final int x = PW - 24 - 230, y = 18, w = 230, h = 34;
        boolean ov = over(x, y, w, h);
        Gfx.rr(x, y, w, h, 10, searchFocus ? SURF3 : (ov ? SURF2 : SURF));
        if (searchFocus) Gfx.rrOutline(x, y, w, h, 10, 1, 0x66FFFFFF);
        Gfx.icon("search", x + 11, y + (h - 16) / 2, 16, Theme.MUTED);
        int ty = y + (h - Gfx.lineHeight(12, false)) / 2;
        if (query.isEmpty()) Gfx.text("Search mods...", x + 34, ty, 12, false, Theme.DIM);
        else Gfx.text(Gfx.trim(query, 12, false, w - 46), x + 34, ty, 12, false, Theme.TEXT);
        if (searchFocus && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cx = x + 34 + (query.isEmpty() ? 0 : Math.min(w - 46, Gfx.width(query, 12, false)));
            g.fill(cx + 1, ty, cx + 2, ty + Gfx.lineHeight(12, false), Ui.alpha(0xFFFFFFFF, Gfx.fade));
        }
    }

    private void drawBody(GuiGraphicsExtractor g, float fade) {
        List<Module> list = visible();
        int gap = 12, cw = (VW - 2 * gap) / 3, ch = 120;
        contentH = list.isEmpty() ? 0 : ((list.size() + 2) / 3) * (ch + gap);
        scrollTarget = Ui.clamp(scrollTarget, 0f, Math.max(0f, contentH - VH));
        g.enableScissor(CX - 6, VY - 2, CX + VW + 6, VY + VH + 2);
        if (list.isEmpty()) {
            Gfx.textCentered("No mods found", CX + VW / 2, VY + 70, 13, false, Theme.MUTED);
        }
        int top = VY - Math.round(scroll);
        for (int i = 0; i < list.size(); i++) {
            int x = CX + (i % 3) * (cw + gap), y = top + (i / 3) * (ch + gap);
            if (y + ch < VY - 2 || y > VY + VH + 2) continue;
            drawCard(list.get(i), x, y, cw, ch);
        }
        g.disableScissor();
        if (contentH > VH) {
            float kh = Math.max(24f, VH * VH / contentH);
            float ky = VY + (scroll / (contentH - VH)) * (VH - kh);
            Gfx.rr(PW - 12, Math.round(ky), 4, Math.round(kh), 2, 0x33FFFFFF);
        }
    }

    private void drawCard(Module m, int x, int y, int cw, int ch) {
        boolean inView = myl >= VY && myl < VY + VH;
        float h = anim("h" + m.id, inView && over(x, y, cw, ch) ? 1f : 0f, 16f);
        float a = anim("e" + m.id, m.enabled ? 1f : 0f, 14f);
        Gfx.rr(x, y, cw, ch, 10, Ui.mix(SURF, SURF2, h));
        Gfx.rrOutline(x, y, cw, ch, 10, 1, Ui.mix(0x14FFFFFF, 0x30FFFFFF, h));
        Gfx.rr(x + 14, y + 14, 36, 36, 10, Ui.mix(SURF3, 0xFF3A3B42, Math.max(a * 0.7f, h * 0.5f)));
        Gfx.icon(m.icon(), x + 14 + 8, y + 14 + 8, 20, Ui.mix(Theme.MUTED, Theme.TEXT, Math.max(a, h)));
        Gfx.text(Gfx.trim(m.name, 14, true, cw - 74), x + 60, y + 14 + (36 - Gfx.lineHeight(14, true)) / 2, 14, true, Theme.TEXT);
        Gfx.text(Gfx.trim(m.description, 10, false, cw - 28), x + 14, y + 58, 10, false, Theme.MUTED);
        int sx = x + cw - 14 - 38, sy = y + ch - 14 - 26 + 3;
        Gfx.rr(sx, sy, 38, 20, 10, Ui.mix(OFF, ACC, a));
        Gfx.rr(sx + 2 + Math.round(18 * a), sy + 2, 16, 16, 8, Ui.mix(0xFF9A9BA3, ON_ACC, a));
        String st = m.enabled ? "ON" : "OFF";
        Gfx.text(st, x + 14, sy + (20 - Gfx.lineHeight(10, true)) / 2, 10, true, m.enabled ? Theme.TEXT : Theme.DIM);
    }

    // ---------- HUD editor ----------
    private void renderEditor(GuiGraphicsExtractor g, Minecraft mc, int mx, int my, int gs) {
        g.fill(0, 0, width, height, 0x66000000);
        for (Module m : Modules.ALL) {
            if (!m.enabled || !(m instanceof HudModule h)) continue;
            h.measure(mc);
            if (h.width(mc) <= 0) continue;
            h.render(g, mc);
            float x = h.screenX(width, mc), y = h.screenY(height, mc);
            boolean hov = mx >= x && mx <= x + h.screenW(mc) && my >= y && my <= y + h.screenH(mc);
            g.outline(Math.round(x) - 1, Math.round(y) - 1, Math.round(h.screenW(mc)) + 2, Math.round(h.screenH(mc)) + 2, hov || h == dragging ? 0xFFF4F4F5 : 0x66FFFFFF);
        }
        Gfx.g = g;
        Gfx.fade = 1f;
        Gfx.setScale(gs);
        String done = "Done", reset = "Reset", hint = "Drag to move  -  mouse wheel to resize";
        int dw = Gfx.width(done, 11, true) + 40, rw = Gfx.width(reset, 11, false) + 40, y = height - 34;
        int total = dw + rw + 10, x0 = width / 2 - total / 2;
        mxl = mx;
        myl = my;
        boolean od = over(x0, y, dw, 24), or = over(x0 + dw + 10, y, rw, 24);
        Gfx.rr(x0, y, dw, 24, 8, od ? 0xFFF4F4F5 : 0xFFE8E8E8);
        Gfx.textCentered(done, x0 + dw / 2, y + (24 - Gfx.lineHeight(11, true)) / 2, 11, true, ON_ACC);
        Gfx.rr(x0 + dw + 10, y, rw, 24, 8, or ? SURF3 : SURF2);
        Gfx.textCentered(reset, x0 + dw + 10 + rw / 2, y + (24 - Gfx.lineHeight(11, false)) / 2, 11, false, Theme.TEXT);
        Gfx.textCentered(hint, width / 2, y - 18, 10, false, Theme.MUTED);
    }

    private HudModule hudAt(double mx, double my) {
        Minecraft mc = Minecraft.getInstance();
        for (int i = Modules.ALL.size() - 1; i >= 0; i--) {
            Module m = Modules.ALL.get(i);
            if (!m.enabled || !(m instanceof HudModule h)) continue;
            h.measure(mc);
            float x = h.screenX(width, mc), y = h.screenY(height, mc);
            if (h.width(mc) > 0 && mx >= x && mx <= x + h.screenW(mc) && my >= y && my <= y + h.screenH(mc)) return h;
        }
        return null;
    }

    // ---------- input ----------
    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        if (e.button() != 0) return super.mouseClicked(e, dbl);
        Minecraft mc = Minecraft.getInstance();
        if (editing) {
            mxl = (float) e.x();
            myl = (float) e.y();
            Gfx.setScale(Math.max(1, mc.getWindow().getGuiScale()));
            int dw = Gfx.width("Done", 11, true) + 40, rw = Gfx.width("Reset", 11, false) + 40, y = height - 34;
            int x0 = width / 2 - (dw + rw + 10) / 2;
            if (over(x0, y, dw, 24)) { editing = false; saveAll(); return true; }
            if (over(x0 + dw + 10, y, rw, 24)) { for (Module m : Modules.ALL) if (m instanceof HudModule h) h.resetLayout(); saveAll(); return true; }
            HudModule h = hudAt(e.x(), e.y());
            if (h != null) {
                dragging = h;
                h.measure(mc);
                dragDX = (float) (e.x() - h.screenX(width, mc));
                dragDY = (float) (e.y() - h.screenY(height, mc));
                return true;
            }
            return true;
        }
        searchFocus = false;
        if (over(PW - 24 - 230, 18, 230, 34)) { searchFocus = true; return true; }
        int[] ws = new int[TABS.length];
        for (int i = 0; i < TABS.length; i++) ws[i] = Gfx.width(TABS[i], 12, true) + 46;
        int px = CX + 4;
        for (int i = 0; i < TABS.length; i++) {
            if (over(px, 74, ws[i], 30)) { tab = i; scroll = scrollTarget = 0; return true; }
            px += ws[i];
        }
        int w = Gfx.width("HUD editor", 11, false) + 48;
        if (over(PW - 24 - w, 70, w, 38)) { editing = true; return true; }
        if (myl >= VY && myl < VY + VH) {
            List<Module> list = visible();
            int gap = 12, cw = (VW - 2 * gap) / 3, ch = 120, top = VY - Math.round(scroll);
            for (int i = 0; i < list.size(); i++) {
                int x = CX + (i % 3) * (cw + gap), y = top + (i / 3) * (ch + gap);
                if (over(x, y, cw, ch)) { list.get(i).toggle(); return true; }
            }
        }
        return super.mouseClicked(e, dbl);
    }

    private void saveAll() {
        for (Module m : Modules.ALL) m.save();
        Config.save();
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (editing && dragging != null) {
            Minecraft mc = Minecraft.getInstance();
            dragging.measure(mc);
            float x = (float) (e.x() - dragDX), y = (float) (e.y() - dragDY);
            dragging.px = Ui.clamp(x / width, 0f, 1f);
            dragging.py = Ui.clamp(y / height, 0f, 1f);
            return true;
        }
        return super.mouseDragged(e, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        if (dragging != null) { dragging = null; saveAll(); return true; }
        return super.mouseReleased(e);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (editing) {
            HudModule h = hudAt(mx, my);
            if (h != null) { h.scale = Ui.clamp(h.scale + (float) sy * 0.05f, HudModule.MIN_SCALE, HudModule.MAX_SCALE); saveAll(); }
            return true;
        }
        scrollTarget = Ui.clamp(scrollTarget - (float) sy * 60f / Math.max(0.3f, scale) * 0.6f, 0f, Math.max(0f, contentH - VH));
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent e) {
        if (searchFocus && !editing) {
            int cp = e.codepoint();
            if (cp >= 32 && cp != 127 && query.length() < 24) { query += new String(Character.toChars(cp)); scroll = scrollTarget = 0; return true; }
        }
        return super.charTyped(e);
    }

    @Override
    public boolean keyPressed(KeyEvent e) {
        if (searchFocus && !editing) {
            if (e.key() == 259) { if (!query.isEmpty()) query = query.substring(0, query.length() - 1); return true; }
            if (e.key() == 256) { searchFocus = false; return true; }
        }
        if (editing && e.key() == 256) { editing = false; saveAll(); return true; }
        return super.keyPressed(e);
    }
}
