package dev.yaclient;

import dev.yaclient.cosmetics.CosmeticPreview;
import dev.yaclient.cosmetics.Logo;
import dev.yaclient.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main client menu: glass panel, animated sidebar with icons, searchable module cards with a
 * per-module settings drawer, cosmetics page and global settings (language, accent color, HUD).
 * Immediate-mode layout: every frame registers its clickable regions in {@code hits}.
 */
public class MenuScreen extends GuiScreen {
    private interface Action { void run(float x, float y); }

    private static final class Hit {
        final int x, y, w, h;
        final Action click, drag, right;

        Hit(int x, int y, int w, int h, Action click, Action drag, Action right) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.click = click; this.drag = drag; this.right = right;
        }

        boolean contains(float px, float py) { return px >= x && px < x + w && py >= y && py < y + h; }
    }

    // ----- layout (GUI units) -----
    private static final int PW = 780, PH = 450, SB = 196;
    private static final int CX = SB + 24, VY = 76, VW = PW - CX - 24, VH = PH - VY - 18;
    private static final int NAV_H = 34;
    private static final String[] TABS = {"all", "info", "pvp", "utility", "cosmetics", "settings"};
    private static final String[] TAB_ICONS = {"grid", "info", "sword", "wrench", "shirt", "gear"};

    private final List<Hit> hits = new ArrayList<Hit>();
    private final Map<String, Float> anim = new HashMap<String, Float>();
    private final long opened = System.currentTimeMillis();
    private long last = opened;
    private float fade = 0.05f, scale = 1f, pageFade = 0f, indY = -1f;
    private float scroll, scrollTarget, contentH, drawerP;
    private int originX, originY, gs = 2;
    private float mxl, myl;
    private int tab = 0;
    /** True when opened from the hub's cosmetics button: only the cosmetics page, no module tabs. */
    private boolean cosMode;

    public MenuScreen() {}

    public MenuScreen(int startTab) { this.tab = startTab; this.cosMode = startTab == 4; if (startTab == 4) CosmeticsApi.refreshAsync(true); }
    private String query = "";
    private boolean searchFocus;
    private Module drawerModule, drawerLast;
    private Action activeDrag;

    // ================= helpers =================
    private static int navY(int i) { return i == 5 ? PH - 62 : (i == 4 ? 106 : 106 + i * 38); }

    private float anim(String key, float target, float dt, float speed, float initial) {
        Float cur = anim.get(key);
        float v = Ui.approach(cur == null ? initial : cur, target, dt, speed);
        anim.put(key, v);
        return v;
    }

    private boolean over(int x, int y, int w, int h) { return mxl >= x && mxl < x + w && myl >= y && myl < y + h; }

    private boolean overView(int x, int y, int w, int h) { return over(x, y, w, h) && myl >= VY && myl < VY + VH; }

    private void hit(int x, int y, int w, int h, Action click, Action drag, Action right) {
        hits.add(new Hit(x, y, w, h, click, drag, right));
    }

    /** Hit region for content inside the scrolling viewport (clipped to it). */
    private void hitView(int x, int y, int w, int h, Action click, Action drag, Action right) {
        int y0 = Math.max(y, VY), y1 = Math.min(y + h, VY + VH);
        if (y1 > y0) hits.add(new Hit(x, y0, w, y1 - y0, click, drag, right));
    }

    private void clip(int x, int y, int w, int h) {
        Minecraft mc = Minecraft.getMinecraft();
        int px = Math.round((originX + x * scale) * gs);
        int pw = Math.round(w * scale * gs);
        int ph = Math.round(h * scale * gs);
        int py = Math.round(mc.displayHeight - (originY + (y + h) * scale) * gs);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(px, py, Math.max(0, pw), Math.max(0, ph));
    }

    private void unclip() { GL11.glDisable(GL11.GL_SCISSOR_TEST); }

    private float lx(int mx) { return (mx - originX) / scale; }

    private float ly(int my) { return (my - originY) / scale; }

    private boolean moduleTab() { return tab >= 0 && tab <= 3; }

    private void selectTab(int i) {
        tab = i;
        scroll = 0f;
        scrollTarget = 0f;
        pageFade = 0f;
        drawerModule = null;
        searchFocus = false;
        if (i == 4) CosmeticsApi.refreshAsync(true);
    }

    private void saveAll() {
        for (Module m : YAClient.MODULES) m.saveConfig();
        Config.save();
    }

    private List<Module> visibleModules() {
        List<Module> out = new ArrayList<Module>();
        String q = query.toLowerCase(Locale.ROOT).trim();
        for (Module m : YAClient.MODULES) {
            if (tab >= 1 && tab <= 3 && m.category.ordinal() != tab - 1) continue;
            if (!q.isEmpty() && !m.name().toLowerCase(Locale.ROOT).contains(q)
                    && !m.desc().toLowerCase(Locale.ROOT).contains(q)) continue;
            out.add(m);
        }
        return out;
    }

    // ================= frame =================
    @Override
    public void drawScreen(int mx, int my, float pt) {
        Minecraft mc = Minecraft.getMinecraft();
        long now = System.currentTimeMillis();
        float dt = Math.min(100, now - last);
        last = now;
        fade = 0.05f + 0.95f * Ui.ease((now - opened) / 200f);
        float open = Ui.ease((now - opened) / 240f);
        scale = Math.min(0.62f, Math.min((height - 16f) / PH, (width - 16f) / PW));
        gs = new ScaledResolution(mc).getScaleFactor();
        Gfx.setScale(Math.max(1, Math.round(gs * scale)));
        originX = Math.round(width / 2f - PW * scale / 2f);
        originY = Math.round(height / 2f - PH * scale / 2f);
        mxl = lx(mx);
        myl = ly(my);

        hits.clear();
        pageFade = Ui.approach(pageFade, 1f, dt, 14f);
        scroll = Ui.approach(scroll, scrollTarget, dt, 16f);
        drawerP = Ui.approach(drawerP, drawerModule != null ? 1f : 0f, dt, 16f);
        if (drawerModule != null) drawerLast = drawerModule;

        Gui.drawRect(0, 0, width, height, Ui.alpha(0x99000000, fade));

        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, 0f);
        float sc = scale * (0.95f + 0.05f * open);
        GlStateManager.scale(sc, sc, 1f);
        GlStateManager.translate(-PW / 2f, -PH / 2f, 0f);
        Gfx.fade = fade;

        drawFrame();
        drawSidebar(dt);
        drawHeader();
        drawBody(dt);
        drawDrawer(dt);

        Gfx.fade = 1f;
        GlStateManager.popMatrix();
    }

    private void drawFrame() {
        for (int i = 5; i >= 1; i--) Gfx.rr(-i * 3, -i * 3 + 10, PW + i * 6, PH + i * 6, 16 + i * 3, 0x12000000);
        Gfx.rr(0, 0, PW, PH, 16, Theme.GLASS);
        Gfx.rrOutline(0, 0, PW, PH, 16, 1, Theme.GLASS_LINE);
        Gui.drawRect(SB, 18, SB + 1, PH - 18, Ui.alpha(0x1AFFFFFF, fade));
    }

    // ================= sidebar =================
    private void drawSidebar(float dt) {
        int ls = 22;
        while (ls > 12 && Gfx.width("YA CLIENT", ls, true) > SB - 44) ls--;
        int yaW = Gfx.width("YA ", ls, true);
        Logo.draw(20, 18, 36, 36, fade);
        Gfx.text("YA ", 64, 22, ls - 3, true, Theme.TEXT);
        Gfx.text("CLIENT", 64 + Gfx.width("YA ", ls - 3, true), 22, ls - 3, true, Theme.BRAND_RED);
        Gfx.text("Minecraft 1.8.9", 64, 22 + Gfx.lineHeight(ls - 3, true) + 1, 10, false, Theme.DIM);

        float target = navY(tab);
        indY = indY < 0f ? target : Ui.approach(indY, target, dt, 18f);
        int iy = Math.round(indY);
        Gfx.rr(10, iy, SB - 20, NAV_H, 9, Ui.alpha(Theme.accent(), 0.16f));

        final int[] nav = cosMode ? new int[]{4} : new int[]{0, 1, 2, 3, 5};
        for (int ni = 0; ni < nav.length; ni++) {
            final int i = nav[ni];
            final int idx = i;
            int y = navY(i);
            boolean active = tab == i;
            float h = anim("nav" + i, over(10, y, SB - 20, NAV_H) ? 1f : 0f, dt, 16f, 0f);
            if (!active && h > 0.01f) Gfx.rr(10, y, SB - 20, NAV_H, 9, Ui.alpha(0x14FFFFFF, h));
            Gfx.icon(TAB_ICONS[i], 26, y + (NAV_H - 18) / 2, 18, active ? Theme.accent() : Ui.mix(Theme.MUTED, Theme.TEXT, h));
            Gfx.text(Lang.t("tab." + TABS[i]), 54, y + (NAV_H - Gfx.lineHeight(13, active)) / 2, 13, active,
                    active ? Theme.TEXT : Ui.mix(Theme.MUTED, Theme.TEXT, h));
            hit(10, y, SB - 20, NAV_H, new Action() {
                @Override public void run(float x, float y2) { selectTab(idx); }
            }, null, null);
        }

        if (!cosMode) {
        // HUD editor entry
        int ey = PH - 100;
        float eh = anim("navEditor", over(10, ey, SB - 20, NAV_H) ? 1f : 0f, dt, 16f, 0f);
        if (eh > 0.01f) Gfx.rr(10, ey, SB - 20, NAV_H, 9, Ui.alpha(0x14FFFFFF, eh));
        Gfx.icon("move", 26, ey + (NAV_H - 18) / 2, 18, Ui.mix(Theme.MUTED, Theme.TEXT, eh));
        Gfx.text(Lang.t("ui.hud_editor"), 54, ey + (NAV_H - Gfx.lineHeight(13, false)) / 2, 13, false, Ui.mix(Theme.MUTED, Theme.TEXT, eh));
        hit(10, ey, SB - 20, NAV_H, new Action() {
            @Override public void run(float x, float y) { Minecraft.getMinecraft().displayGuiScreen(new HudEditorScreen()); }
        }, null, null);
        }

        Gfx.text(Lang.t("ui.esc_hint"), 24, PH - 26, 10, false, Theme.DIM);
    }

    // ================= header =================
    private void drawHeader() {
        Gfx.text(Lang.t("tab." + TABS[tab]), CX, 20, 22, true, Theme.TEXT);
        String sub;
        if (moduleTab()) {
            int total = visibleModules().size(), on = 0;
            for (Module m : visibleModules()) if (m.enabled) on++;
            sub = Lang.t("ui.count", total, on);
        } else {
            sub = Lang.t("tab." + TABS[tab] + ".sub");
        }
        Gfx.text(sub, CX, 20 + Gfx.lineHeight(22, true) + 1, 10, false, Theme.MUTED);

        if (!moduleTab()) return;
        final int x = PW - 24 - 210, y = 20, w = 210, h = 34;
        boolean ov = over(x, y, w, h);
        Gfx.rr(x, y, w, h, 11, searchFocus ? 0x1FFFFFFF : (ov ? 0x1AFFFFFF : 0x12FFFFFF));
        Gfx.rrOutline(x, y, w, h, 11, 1, searchFocus ? Theme.accent() : 0x1AFFFFFF);
        Gfx.icon("search", x + 11, y + (h - 16) / 2, 16, Theme.MUTED);
        int ty = y + (h - Gfx.lineHeight(12, false)) / 2;
        if (query.isEmpty()) {
            Gfx.text(Lang.t("ui.search"), x + 34, ty, 12, false, Theme.DIM);
        } else {
            Gfx.text(Gfx.trim(query, 12, false, w - 46), x + 34, ty, 12, false, Theme.TEXT);
        }
        if (searchFocus && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cx = x + 34 + (query.isEmpty() ? 0 : Math.min(w - 46, Gfx.width(query, 12, false)));
            Gui.drawRect(cx + 1, ty, cx + 2, ty + Gfx.lineHeight(12, false), Ui.alpha(0xFFFFFFFF, fade));
        }
        hit(x, y, w, h, new Action() {
            @Override public void run(float a, float b) { searchFocus = true; }
        }, null, null);
    }

    // ================= body =================
    private void drawBody(float dt) {
        float cf = Ui.ease(pageFade);
        Gfx.fade = fade * cf;
        int top = VY - Math.round(scroll) + Math.round((1f - cf) * 10f);
        clip(CX - 6, VY - 2, VW + 12, VH + 4);
        if (moduleTab()) contentH = drawModules(top, dt);
        else if (tab == 4) contentH = drawCosmetics();
        else contentH = drawSettings(top, dt);
        unclip();
        scrollTarget = Ui.clamp(scrollTarget, 0f, Math.max(0f, contentH - VH));

        if (contentH > VH) {
            float kh = Math.max(24f, VH * VH / contentH);
            float ky = VY + (scroll / (contentH - VH)) * (VH - kh);
            Gfx.rr(PW - 14, Math.round(ky), 4, Math.round(kh), 2, 0x33FFFFFF);
        }
        Gfx.fade = fade;
    }

    // ----- modules -----
    private float drawModules(int top, float dt) {
        List<Module> list = visibleModules();
        if (list.isEmpty()) {
            Gfx.textCentered(Lang.t("ui.no_results"), CX + VW / 2, VY + 70, 13, false, Theme.MUTED);
            return 0f;
        }
        int gap = 12, cw = (VW - 2 * gap) / 3, ch = 128;
        for (int i = 0; i < list.size(); i++) {
            int x = CX + (i % 3) * (cw + gap), y = top + (i / 3) * (ch + gap);
            if (y + ch < VY - 2 || y > VY + VH + 2) continue;
            drawCard(list.get(i), x, y, cw, ch, dt);
        }
        return ((list.size() + 2) / 3) * (ch + gap);
    }

    private void drawCard(final Module m, int x, int y, int cw, int ch, float dt) {
        float h = anim("h" + m.id, overView(x, y, cw, ch) ? 1f : 0f, dt, 16f, 0f);
        float a = anim("e" + m.id, m.enabled ? 1f : 0f, dt, 14f, m.enabled ? 1f : 0f);

        Gfx.rr(x, y, cw, ch, 12, Ui.mix(0x1AFFFFFF, 0x26FFFFFF, h));
        Gfx.rrOutline(x, y, cw, ch, 12, 1, Ui.mix(0x30FFFFFF, 0x77FFFFFF, a * 0.6f + h * 0.4f));

        // big icon, centered
        Gfx.icon(m.icon(), x + (cw - 30) / 2, y + 12, 30, Ui.mix(Theme.MUTED, Theme.TEXT, Math.max(a, h)));
        Gfx.textCentered(Gfx.trim(m.name(), 12, true, cw - 16), x + cw / 2, y + 48, 12, true, m.available ? Theme.TEXT : Theme.MUTED);

        hitView(x, y, cw, ch, null, null, new Action() {
            @Override public void run(float px, float py) { if (!m.settings.isEmpty() && m.available) drawerModule = m; }
        });
        boolean gear = m.available && !m.settings.isEmpty();
        int bx = x + 10, bw = cw - 20, oy = y + 70, oh = 20;
        int ow = gear ? bw : bw;
        boolean oo = gear && overView(bx, oy, ow, oh);
        Gfx.rr(bx, oy, ow, oh, 7, gear ? (oo ? 0x2BFFFFFF : 0x17FFFFFF) : 0x0DFFFFFF);
        String opt = Lang.t("ui.options");
        Gfx.textCentered(opt, bx + ow / 2, oy + (oh - Gfx.lineHeight(9, true)) / 2, 9, true, gear ? Theme.TEXT : Theme.DIM);
        if (gear) hitView(bx, oy, ow, oh, new Action() {
            @Override public void run(float px, float py) { drawerModule = m; }
        }, null, null);

        int ty = y + 95, th = 24;
        if (m.available) {
            boolean to = overView(bx, ty, bw, th);
            int off = Ui.mix(0xFF26262C, 0xFF3A3A44, to ? 1f : 0f);
            int on = Ui.mix(0xFFE9E9EC, 0xFFFFFFFF, to ? 1f : 0f);
            Gfx.rr(bx, ty, bw, th, 8, Ui.mix(off, on, a));
            String st = Lang.t(m.enabled ? "ui.enabled" : "ui.disabled");
            Gfx.textCentered(st, bx + bw / 2, ty + (th - Gfx.lineHeight(10, true)) / 2, 10, true, Ui.mix(Theme.MUTED, 0xFF101014, a));
            hitView(bx, ty, bw, th, new Action() {
                @Override public void run(float px, float py) { m.toggle(); }
            }, null, null);
        } else {
            Gfx.rr(bx, ty, bw, th, 8, 0x14FFFFFF);
            Gfx.textCentered(Lang.t("ui.soon"), bx + bw / 2, ty + (th - Gfx.lineHeight(10, true)) / 2, 10, true, Theme.DIM);
        }
    }

    // ----- settings drawer -----
    private void drawDrawer(float dt) {
        final Module m = drawerModule != null ? drawerModule : drawerLast;
        float p = Ui.ease(drawerP);
        if (m == null || p <= 0.003f) return;
        int dw = 276, y0 = 64, dh = PH - y0 - 16;
        int dx = Math.round(PW - 16 - dw * p);
        Gfx.fade = fade * p;

        if (drawerModule != null) {
            hits.add(new Hit(0, 0, PW, PH, new Action() {
                @Override public void run(float x, float y) { drawerModule = null; }
            }, null, null));
            hits.add(new Hit(dx, y0, dw, dh, null, null, null)); // consumes clicks inside the drawer
        }
        Gfx.rr(dx, y0, dw, dh, 14, 0xF5141419);
        Gfx.rrOutline(dx, y0, dw, dh, 14, 1, 0x2AFFFFFF);

        Gfx.text(Gfx.trim(m.name(), 14, true, dw - 70), dx + 18, y0 + 16, 14, true, Theme.TEXT);
        Gfx.text(Lang.t("ui.settings"), dx + 18, y0 + 16 + Gfx.lineHeight(14, true) + 1, 10, false, Theme.MUTED);
        boolean co = over(dx + dw - 40, y0 + 12, 26, 26);
        Gfx.icon("close", dx + dw - 36, y0 + 16, 18, co ? Theme.TEXT : Theme.MUTED);
        if (drawerModule != null) {
            hits.add(new Hit(dx + dw - 40, y0 + 12, 26, 26, new Action() {
                @Override public void run(float x, float y) { drawerModule = null; }
            }, null, null));
        }
        Gui.drawRect(dx + 16, y0 + 56, dx + dw - 16, y0 + 57, Ui.alpha(0x1AFFFFFF, fade * p));

        int ry = y0 + 66;
        for (final Setting s : m.settings) {
            if (s instanceof Setting.Bool) {
                final Setting.Bool b = (Setting.Bool) s;
                float a = anim("s" + m.id + s.id, b.value ? 1f : 0f, dt, 14f, b.value ? 1f : 0f);
                Gfx.text(Gfx.trim(s.label(), 12, false, dw - 90), dx + 18, ry + (28 - Gfx.lineHeight(12, false)) / 2, 12, false, Theme.TEXT);
                int sx = dx + dw - 18 - 34;
                Gfx.rr(sx, ry + 5, 34, 18, 9, Ui.mix(0x33FFFFFF, Theme.accent(), a));
                Gfx.rr(sx + 2 + Math.round(16 * a), ry + 7, 14, 14, 7, Ui.mix(0xFFB4B4BC, Theme.onAccent(), a));
                if (drawerModule != null) {
                    hits.add(new Hit(dx + 10, ry, dw - 20, 28, new Action() {
                        @Override public void run(float x, float y) { b.value = !b.value; saveAll(); }
                    }, null, null));
                }
                ry += 38;
            } else if (s instanceof Setting.Num) {
                final Setting.Num n = (Setting.Num) s;
                final int trackX = dx + 18, trackW = dw - 36, ty = ry + 32;
                Gfx.text(Gfx.trim(s.label(), 12, false, dw - 100), dx + 18, ry + 2, 12, false, Theme.TEXT);
                String val = n.text();
                Gfx.text(val, dx + dw - 18 - Gfx.width(val, 12, true), ry + 2, 12, true, Theme.MUTED);
                Gfx.rr(trackX, ty, trackW, 6, 3, 0x26FFFFFF);
                int fw = Math.max(6, Math.round(trackW * n.fraction()));
                Gfx.rr(trackX, ty, fw, 6, 3, Theme.accent());
                Gfx.rr(trackX + fw - 7, ty - 4, 14, 14, 7, 0xFFF5F5F5);
                if (drawerModule != null) {
                    Action drag = new Action() {
                        @Override public void run(float x, float y) { n.setFraction((x - trackX) / trackW); }
                    };
                    hits.add(new Hit(dx + 10, ry + 22, dw - 20, 28, drag, drag, null));
                }
                ry += 58;
            } else if (s instanceof Setting.Choice) {
                final Setting.Choice c = (Setting.Choice) s;
                Gfx.text(Gfx.trim(s.label(), 12, false, dw - 40), dx + 18, ry + 2, 12, false, Theme.TEXT);
                int cx = dx + 18, cy = ry + 24;
                for (int i = 0; i < c.options.length; i++) {
                    final int idx = i;
                    String lab = Lang.t("opt." + c.options[i]);
                    int cwid = Gfx.width(lab, 11, false) + 20;
                    if (cx + cwid > dx + dw - 18) { cx = dx + 18; cy += 30; }
                    boolean sel = c.index == i;
                    boolean cov = over(cx, cy, cwid, 24);
                    Gfx.rr(cx, cy, cwid, 24, 8, sel ? Ui.alpha(Theme.accent(), 0.22f) : (cov ? 0x1AFFFFFF : 0x10FFFFFF));
                    if (sel) Gfx.rrOutline(cx, cy, cwid, 24, 8, 1, Theme.accent());
                    Gfx.text(lab, cx + 10, cy + (24 - Gfx.lineHeight(11, false)) / 2, 11, false, sel ? Theme.TEXT : Theme.MUTED);
                    if (drawerModule != null) {
                        hits.add(new Hit(cx, cy, cwid, 24, new Action() {
                            @Override public void run(float x, float y) { c.index = idx; saveAll(); }
                        }, null, null));
                    }
                    cx += cwid + 6;
                }
                ry += (cy - (ry + 24)) + 24 + 24 + 14;
            }
        }
        if (m.settings.isEmpty()) {
            Gfx.text(Lang.t("ui.no_settings"), dx + 18, ry, 12, false, Theme.MUTED);
        }
        Gfx.fade = fade;
    }

    // ----- cosmetics (locker) -----
    private static final String[] COS_CATS = {"bundle", "cape", "wings", "bandana", "pet"};
    private int cosCat = 0;

    private static String cosIcon(String slotOrBundle) {
        if (slotOrBundle.equals("bundle")) return "gift";
        return slotOrBundle;
    }

    /** Spinning preview of the player with the equipped cosmetics (the same render layer as in game). */
    private void drawPlayerPreview(int cx, int baseY, int size) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer ent = mc.thePlayer;
        if (ent == null) return;
        float spin = (System.currentTimeMillis() % 11000L) / 11000f * 360f;
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) cx, (float) baseY, 50.0F);
        GlStateManager.scale((float) (-size), (float) size, (float) size);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float ro = ent.renderYawOffset, pro = ent.prevRenderYawOffset, ry = ent.rotationYaw, pry = ent.prevRotationYaw,
                rp = ent.rotationPitch, prh = ent.prevRotationYawHead, rh = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        ent.renderYawOffset = spin; ent.prevRenderYawOffset = spin;
        ent.rotationYaw = spin; ent.prevRotationYaw = spin;
        ent.rotationYawHead = spin; ent.prevRotationYawHead = spin;
        ent.rotationPitch = 0f;
        RenderManager rm = mc.getRenderManager();
        rm.setPlayerViewY(180.0F);
        rm.setRenderShadow(false);
        rm.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rm.setRenderShadow(true);
        ent.renderYawOffset = ro; ent.prevRenderYawOffset = pro;
        ent.rotationYaw = ry; ent.prevRotationYaw = pry;
        ent.rotationPitch = rp; ent.prevRotationYawHead = prh; ent.rotationYawHead = rh;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private float drawCosmetics() {
        int pw = 176;
        // left: player preview
        Gfx.rr(CX, VY, pw, VH, 14, 0x0FFFFFFF);
        Gfx.rrOutline(CX, VY, pw, VH, 14, 1, 0x14FFFFFF);
        Gfx.textCentered(Lang.t("cos.loadout"), CX + pw / 2, VY + 10, 11, true, Theme.MUTED);
        boolean plusOn = CosmeticsApi.plus();
        String plusLab = Lang.t(plusOn ? "cos.plus.on" : "cos.plus.off");
        int plw = Gfx.width(plusLab, 9, true) + 18;
        Gfx.rr(CX + (pw - plw) / 2, VY + 28, plw, 16, 8, plusOn ? Theme.accent() : 0x18FFFFFF);
        Gfx.text(plusLab, CX + (pw - plw) / 2 + 9, VY + 28 + (16 - Gfx.lineHeight(9, true)) / 2, 9, true, plusOn ? Theme.onAccent() : Theme.MUTED);
        drawPlayerPreview(CX + pw / 2, VY + VH - 40, 92);

        int x0 = CX + pw + 16, wRight = VW - pw - 16;

        // category pills
        int px = x0;
        for (int i = 0; i < COS_CATS.length; i++) {
            final int idx = i;
            String lab = Lang.t("cos.cat." + COS_CATS[i]);
            int w = Gfx.width(lab, 10, true) + 22;
            boolean on = cosCat == i, ov = over(px, VY, w, 24);
            Gfx.rr(px, VY, w, 24, 8, on ? Ui.alpha(Theme.accent(), 0.25f) : (ov ? 0x1AFFFFFF : 0x10FFFFFF));
            if (on) Gfx.rrOutline(px, VY, w, 24, 8, 1, Theme.accent());
            Gfx.text(lab, px + 11, VY + (24 - Gfx.lineHeight(10, true)) / 2, 10, true, on ? Theme.TEXT : Theme.MUTED);
            hit(px, VY, w, 24, new Action() {
                @Override public void run(float mx, float my) { cosCat = idx; }
            }, null, null);
            px += w + 6;
        }

        // cards: bundles first, or the items of one slot (from every bundle)
        List<String> entries = new ArrayList<String>();
        for (int bi = 0; bi < Cosmetics.BUNDLES.length; bi++) {
            if (cosCat == 0) entries.add("bundle:" + bi);
            else entries.add(COS_CATS[cosCat] + ":" + bi);
        }
        int gap = 10, cols = 3, cw = (wRight - gap * (cols - 1)) / cols, ch = 150;
        int gy = VY + 36;
        for (int n = 0; n < entries.size(); n++) {
            final String[] kv = entries.get(n).split(":");
            final String key = kv[0];
            final int bi = Integer.parseInt(kv[1]);
            int cx = x0 + (n % cols) * (cw + gap), cy = gy + (n / cols) * (ch + gap);
            final boolean bundle = key.equals("bundle");
            final String id = bundle ? Cosmetics.BUNDLES[bi] : Cosmetics.item(bi, key);
            boolean has, on;
            if (bundle) {
                has = Cosmetics.ownsBundle(bi);
                on = has && Cosmetics.wearsBundle(bi);
            } else {
                has = CosmeticsApi.owned(id);
                on = has && Cosmetics.selected(key).equals(id);
            }
            boolean ov = over(cx, cy, cw, ch);
            Gfx.rr(cx, cy, cw, ch, 12, on ? Ui.alpha(Theme.accent(), 0.16f) : (ov ? 0x24FFFFFF : 0x1AFFFFFF));
            Gfx.rrOutline(cx, cy, cw, ch, 12, 1, on ? Theme.accent() : 0x30FFFFFF);
            // tag band (FREE / EXCLUSIVE / lock) above the thumbnail, so nothing is drawn over the preview
            int band = 18, th = 70, ty = cy + band + 4;
            if (bundle) {
                String fr = Lang.t(bi == 0 ? "cos.free" : "cos.exclusive");
                int fw = Gfx.width(fr, 9, true) + 12;
                Gfx.rr(cx + 8, cy + 6, fw, 15, 7, bi == 0 ? Theme.accent() : 0xFFF4F4F5);
                Gfx.text(fr, cx + 14, cy + 6 + (15 - Gfx.lineHeight(9, true)) / 2, 9, true, bi == 0 ? Theme.onAccent() : 0xFF101014);
            }
            if (!has) Gfx.icon("lock", cx + cw - 24, cy + 6, 14, Theme.MUTED);
            // thumbnail (3D preview is clipped to its box)
            Gfx.rr(cx + 6, ty, cw - 12, th, 9, 0x22000000);
            float ps = key.equals("cape") ? 56f : key.equals("wings") ? 34f : key.equals("bandana") ? 90f : key.equals("pet") ? 90f : 30f;
            clip(cx + 6, ty, cw - 12, th);
            CosmeticPreview.draw(key, bi == 0 ? "ya" : "mono", cx + cw / 2f, ty + th / 2f + 3f, ps, System.currentTimeMillis(), CosmeticsApi.plus());
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            unclip();
            String name = Lang.t(bundle ? "cos.bundle." + id : "cos.item." + id);
            Gfx.text(Gfx.trim(name, 11, true, cw - 16), cx + 8, ty + th + 7, 11, true, Theme.TEXT);
            String st = on ? Lang.t("cos.equipped") : (has ? Lang.t("cos.click_equip") : Lang.t("cos.locked"));
            if (on) {
                Gfx.rr(cx + 6, cy + ch - 24, cw - 12, 18, 7, Ui.alpha(Theme.accent(), 0.30f));
                Gfx.icon("check", cx + 10, cy + ch - 23, 16, Theme.TEXT);
                Gfx.text(st, cx + 28, cy + ch - 24 + (18 - Gfx.lineHeight(10, true)) / 2, 10, true, Theme.TEXT);
            } else {
                Gfx.text(Gfx.trim(st, 10, false, cw - 16), cx + 8, cy + ch - 24 + (18 - Gfx.lineHeight(10, false)) / 2, 10, false, Theme.MUTED);
            }
            final boolean fHas = has;
            hit(cx, cy, cw, ch, new Action() {
                @Override public void run(float mx, float my) {
                    if (!fHas) { CosmeticsApi.openStore(); return; }
                    if (bundle) Cosmetics.toggleBundle(bi); else Cosmetics.toggle(bi, key);
                }
            }, null, null);
        }

        // footer: sync status + store button
        Minecraft mc = Minecraft.getMinecraft();
        int fy = VY + VH - 26;
        String stKey;
        if (!CosmeticsApi.enabled()) stKey = "cos.dev_mode";
        else if (CosmeticsApi.status().isEmpty()) stKey = "cos.syncing";
        else stKey = "cos.status." + CosmeticsApi.status();
        String store = Lang.t("cos.store");
        int sw = Gfx.width(store, 11, true) + 40;
        Gfx.text(Gfx.trim(Lang.t(stKey) + (CosmeticsApi.enabled() && !CosmeticsApi.detail().isEmpty() ? " [" + CosmeticsApi.detail() + "]" : ""), 10, false, wRight - sw - 12), x0, fy + (26 - Gfx.lineHeight(10, false)) / 2, 10, false, Theme.MUTED);
        if (CosmeticsApi.enabled()) {
            int bx = x0 + wRight - sw;
            boolean ov = over(bx, fy, sw, 26);
            Gfx.rr(bx, fy, sw, 26, 9, ov ? Ui.mix(Theme.accent(), 0xFFFFFFFF, 0.15f) : Theme.accent());
            Gfx.icon("cart", bx + 11, fy + 5, 16, Theme.onAccent());
            Gfx.text(store, bx + 31, fy + (26 - Gfx.lineHeight(11, true)) / 2, 11, true, Theme.onAccent());
            hit(bx, fy, sw, 26, new Action() {
                @Override public void run(float mx, float my) { CosmeticsApi.openStore(); }
            }, null, null);
        }
        return 0f;
    }

    // ----- global settings -----
    private int section(String key, int y) {
        Gfx.text(Lang.t(key), CX, y, 13, true, Theme.MUTED);
        return y + 24;
    }

    private float drawSettings(int top, float dt) {
        int y = top;

        // language
        y = section("settings.language", y);
        int cw = (VW - 20) / 3;
        for (int i = 0; i < Lang.CODES.length; i++) {
            final String code = Lang.CODES[i];
            int x = CX + (i % 3) * (cw + 10), yy = y + (i / 3) * 48;
            boolean sel = Lang.code().equals(code);
            boolean ov = overView(x, yy, cw, 40);
            Gfx.rr(x, yy, cw, 40, 11, sel ? Ui.alpha(Theme.accent(), 0.18f) : (ov ? 0x1AFFFFFF : 0x0FFFFFFF));
            Gfx.rrOutline(x, yy, cw, 40, 11, 1, sel ? Theme.accent() : 0x14FFFFFF);
            Gfx.icon("globe", x + 14, yy + 11, 18, sel ? Theme.accent() : Theme.MUTED);
            Gfx.text(Lang.NAMES[i], x + 42, yy + (40 - Gfx.lineHeight(12, true)) / 2, 12, true, sel ? Theme.TEXT : Theme.MUTED);
            if (sel) Gfx.icon("check", x + cw - 30, yy + 11, 18, Theme.accent());
            hitView(x, yy, cw, 40, new Action() {
                @Override public void run(float px, float py) { Lang.load(code); Config.save(); }
            }, null, null);
        }
        y += ((Lang.CODES.length + 2) / 3) * 48 + 14;

        // accent color
        y = section("settings.accent", y);
        for (int i = 0; i < Theme.ACCENT_IDS.length; i++) {
            final int idx = i;
            int x = CX + i * 50;
            boolean sel = Theme.accentIndex() == i;
            boolean ov = overView(x, y, 36, 36);
            if (sel || ov) Gfx.rrOutline(x - 3, y - 3, 42, 42, 21, 2, sel ? 0xFFF5F5F5 : 0x66FFFFFF);
            boolean locked = Theme.plusOnly(i) && !CosmeticsApi.plus();
            Gfx.rr(x, y, 36, 36, 18, locked ? Ui.mix(Theme.accentOf(i), 0xFF18181C, 0.7f) : Theme.accentOf(i));
            if (locked) Gfx.text("PLUS", x + 18 - Gfx.width("PLUS", 8, true) / 2, y + (36 - Gfx.lineHeight(8, true)) / 2, 8, true, 0xFFFFC21A);
            if (sel) Gfx.icon("check", x + 9, y + 9, 18, 0xFF101014);
            hitView(x - 3, y - 3, 42, 42, new Action() {
                @Override public void run(float px, float py) { if (Theme.plusOnly(idx) && !CosmeticsApi.plus()) return; Theme.setAccent(idx); Config.save(); }
            }, null, null);
        }
        y += 36 + 26;

        // HUD
        y = section("settings.hud", y);
        Gfx.text(Lang.t("settings.hud_scale"), CX, y, 12, false, Theme.TEXT);
        String val = Math.round(Hud.globalScale() * 100f) + "%";
        Gfx.text(val, CX + VW - Gfx.width(val, 12, true), y, 12, true, Theme.MUTED);
        y += 24;
        final int tx = CX, tw = VW;
        Gfx.rr(tx, y, tw, 6, 3, 0x26FFFFFF);
        float frac = (Hud.globalScale() - 0.5f) / 1.5f;
        int fw = Math.max(6, Math.round(tw * frac));
        Gfx.rr(tx, y, fw, 6, 3, Theme.accent());
        Gfx.rr(tx + fw - 7, y - 4, 14, 14, 7, 0xFFF5F5F5);
        Action drag = new Action() {
            @Override public void run(float px, float py) {
                Hud.setGlobalScale(0.5f + Ui.clamp((px - tx) / (float) tw, 0f, 1f) * 1.5f);
            }
        };
        hitView(tx - 6, y - 10, tw + 12, 26, drag, drag, null);
        y += 30;

        int bw = (VW - 10) / 2;
        button(Lang.t("settings.edit_hud"), "move", CX, y, bw, true, new Action() {
            @Override public void run(float px, float py) { Minecraft.getMinecraft().displayGuiScreen(new HudEditorScreen()); }
        });
        button(Lang.t("settings.reset_hud"), "reset", CX + bw + 10, y, bw, false, new Action() {
            @Override public void run(float px, float py) { Hud.resetLayout(); }
        });
        y += 44 + 20;

        // about
        y = section("settings.about", y);
        Gfx.text("YA CLIENT 1.1.0 - Minecraft 1.8.9", CX, y, 12, false, Theme.TEXT);
        y += 18;
        Gfx.text(Gfx.trim(Lang.t("settings.config_hint"), 10, false, VW), CX, y, 10, false, Theme.MUTED);
        y += 30;
        return y - top;
    }

    private void button(String label, String icon, int x, int y, int w, boolean primary, Action act) {
        boolean ov = overView(x, y, w, 36);
        int fill = primary ? (ov ? Ui.mix(Theme.accent(), 0xFFFFFFFF, 0.15f) : Theme.accent()) : (ov ? 0x24FFFFFF : 0x12FFFFFF);
        int fg = primary ? Theme.onAccent() : Theme.TEXT;
        Gfx.rr(x, y, w, 36, 11, fill);
        if (!primary) Gfx.rrOutline(x, y, w, 36, 11, 1, 0x1AFFFFFF);
        int lw = Gfx.width(label, 12, true);
        int ix = x + (w - lw - 26) / 2;
        Gfx.icon(icon, ix, y + 9, 18, fg);
        Gfx.text(label, ix + 26, y + (36 - Gfx.lineHeight(12, true)) / 2, 12, true, fg);
        hitView(x, y, w, 36, act, null, null);
    }

    // ================= input (no "throws": compatible with any mapping) =================
    @Override
    protected void mouseClicked(int mx, int my, int button) {
        float x = lx(mx), y = ly(my);
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.contains(x, y)) continue;
            if (button == 0) {
                searchFocus = false;
                if (h.click != null) h.click.run(x, y);
                if (h.drag != null) { activeDrag = h.drag; h.drag.run(x, y); }
            } else if (button == 1 && h.right != null) {
                h.right.run(x, y);
            }
            return;
        }
        searchFocus = false;
    }

    @Override
    protected void mouseClickMove(int mx, int my, int button, long time) {
        if (activeDrag != null) activeDrag.run(lx(mx), ly(my));
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        if (activeDrag != null) {
            activeDrag = null;
            saveAll();
        }
    }

    @Override
    public void handleMouseInput() {
        int d = Mouse.getEventDWheel();
        if (d != 0) scrollTarget = Ui.clamp(scrollTarget - Math.signum(d) * 48f, 0f, Math.max(0f, contentH - VH));
        try { super.handleMouseInput(); } catch (Exception ignored) { }
    }

    @Override
    protected void keyTyped(char typed, int key) {
        if (key == Keyboard.KEY_ESCAPE) {
            if (searchFocus) { searchFocus = false; return; }
            if (drawerModule != null) { drawerModule = null; return; }
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }
        if (searchFocus && moduleTab()) {
            if (key == Keyboard.KEY_BACK) {
                if (!query.isEmpty()) query = query.substring(0, query.length() - 1);
            } else if (typed >= 32 && typed != 127 && query.length() < 24) {
                query += typed;
            }
            scrollTarget = 0f;
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
