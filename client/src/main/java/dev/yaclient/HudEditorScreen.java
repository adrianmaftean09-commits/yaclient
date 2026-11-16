package dev.yaclient;

import dev.yaclient.modules.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;

/**
 * HUD editor: drag elements anywhere, resize with the corner handle or the mouse wheel, snap to
 * the screen center and edges. Positions are saved when you leave the editor.
 */
public class HudEditorScreen extends GuiScreen {
    private static final int HANDLE = 8, SNAP = 4;

    private HudModule selected, dragging, resizing;
    private float grabX, grabY;
    private int gs = 2;
    private float guideX = -1, guideY = -1;
    private int btnResetX, btnDoneX, btnY, btnW = 70, btnH = 22;

    private float sx(HudModule m) { return m.screenX(width); }
    private float sy(HudModule m) { return m.screenY(height); }
    private float sw(HudModule m) { return m.width() * m.screenScale(); }
    private float sh(HudModule m) { return m.height() * m.screenScale(); }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        gs = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        Gui.drawRect(0, 0, width, height, 0x66000000);

        List<HudModule> list = Hud.elements();
        for (HudModule m : list) m.renderHud(true, width, height, gs);

        Gfx.setScale(gs);
        Gfx.fade = 1f;

        // guides
        if (guideX >= 0) Gui.drawRect(Math.round(guideX), 0, Math.round(guideX) + 1, height, Ui.alpha(Theme.accent(), 0.8f));
        if (guideY >= 0) Gui.drawRect(0, Math.round(guideY), width, Math.round(guideY) + 1, Ui.alpha(Theme.accent(), 0.8f));

        for (HudModule m : list) {
            int x = Math.round(sx(m)), y = Math.round(sy(m));
            int w = Math.max(6, Math.round(sw(m))), h = Math.max(6, Math.round(sh(m)));
            boolean hot = m == selected || m == dragging || m == resizing
                    || (mx >= x && mx < x + w && my >= y && my < y + h);
            Gfx.rrOutline(x - 2, y - 2, w + 4, h + 4, 4, 1, hot ? Theme.accent() : 0x66FFFFFF);
            if (m == selected) {
                Gfx.rr(x + w - HANDLE + 2, y + h - HANDLE + 2, HANDLE, HANDLE, 3, Theme.accent());
                Gfx.text(m.name(), x - 2, y - 14, 9, false, Theme.TEXT);
            }
        }

        // toolbar
        String title = Lang.t("editor.title");
        String hint = Lang.t("editor.hint");
        int tw = Math.max(Gfx.width(hint, 10, false), Gfx.width(title, 13, true)) + 40 + 2 * btnW + 20;
        int tx = (width - tw) / 2, ty = 10, th = 38;
        Gfx.rr(tx, ty, tw, th, 12, Theme.GLASS);
        Gfx.rrOutline(tx, ty, tw, th, 12, 1, Theme.GLASS_LINE);
        Gfx.text(title, tx + 16, ty + 5, 13, true, Theme.TEXT);
        Gfx.text(hint, tx + 16, ty + 22, 10, false, Theme.MUTED);
        btnY = ty + 8;
        btnDoneX = tx + tw - btnW - 8;
        btnResetX = btnDoneX - btnW - 6;
        button(Lang.t("editor.reset"), btnResetX, mx, my, false);
        button(Lang.t("editor.done"), btnDoneX, mx, my, true);

        if (list.isEmpty()) {
            Gfx.textCentered(Lang.t("editor.empty"), width / 2, height / 2, 12, false, Theme.MUTED);
        }
    }

    private void button(String label, int x, int mx, int my, boolean primary) {
        boolean over = mx >= x && mx < x + btnW && my >= btnY && my < btnY + btnH;
        int fill = primary ? (over ? Ui.mix(Theme.accent(), 0xFFFFFFFF, 0.15f) : Theme.accent())
                : (over ? 0x26FFFFFF : 0x14FFFFFF);
        Gfx.rr(x, btnY, btnW, btnH, 8, fill);
        Gfx.textCentered(label, x + btnW / 2, btnY + (btnH - Gfx.lineHeight(11, true)) / 2, 11, true,
                primary ? Theme.onAccent() : Theme.TEXT);
    }

    private void leave() {
        Hud.saveLayout();
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        if (my >= btnY && my < btnY + btnH) {
            if (mx >= btnDoneX && mx < btnDoneX + btnW) { leave(); return; }
            if (mx >= btnResetX && mx < btnResetX + btnW) { Hud.resetLayout(); selected = null; return; }
        }
        List<HudModule> list = Hud.elements();
        for (int i = list.size() - 1; i >= 0; i--) {
            HudModule m = list.get(i);
            int x = Math.round(sx(m)), y = Math.round(sy(m));
            int w = Math.max(6, Math.round(sw(m))), h = Math.max(6, Math.round(sh(m)));
            if (m == selected && mx >= x + w - HANDLE && mx <= x + w + 4 && my >= y + h - HANDLE && my <= y + h + 4) {
                resizing = m;
                return;
            }
            if (mx >= x && mx < x + w && my >= y && my < y + h) {
                selected = m;
                dragging = m;
                grabX = mx - sx(m);
                grabY = my - sy(m);
                return;
            }
        }
        selected = null;
    }

    @Override
    protected void mouseClickMove(int mx, int my, int button, long time) {
        guideX = guideY = -1;
        if (dragging != null) {
            HudModule m = dragging;
            float w = sw(m), h = sh(m);
            float x = mx - grabX, y = my - grabY;
            // snap element center / edges to the screen center and borders
            float cx = x + w / 2f, cy = y + h / 2f;
            if (Math.abs(cx - width / 2f) < SNAP) { x = width / 2f - w / 2f; guideX = width / 2f; }
            else if (Math.abs(x) < SNAP) x = 0;
            else if (Math.abs(x + w - width) < SNAP) x = width - w;
            if (Math.abs(cy - height / 2f) < SNAP) { y = height / 2f - h / 2f; guideY = height / 2f; }
            else if (Math.abs(y) < SNAP) y = 0;
            else if (Math.abs(y + h - height) < SNAP) y = height - h;
            m.px = Ui.clamp(x / width, 0f, 1f);
            m.py = Ui.clamp(y / height, 0f, 1f);
        } else if (resizing != null) {
            HudModule m = resizing;
            float base = Math.max(1, m.width()) * Hud.globalScale();
            m.scale = Ui.clamp((mx - sx(m)) / base, HudModule.MIN_SCALE, HudModule.MAX_SCALE);
        }
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        dragging = null;
        resizing = null;
        guideX = guideY = -1;
        Hud.saveLayout();
    }

    @Override
    public void handleMouseInput() {
        int d = Mouse.getEventDWheel();
        if (d != 0 && selected != null) {
            selected.scale = Ui.clamp(selected.scale + (d > 0 ? 0.05f : -0.05f), HudModule.MIN_SCALE, HudModule.MAX_SCALE);
        }
        try { super.handleMouseInput(); } catch (Exception ignored) { }
    }

    @Override
    protected void keyTyped(char typed, int key) {
        if (key == Keyboard.KEY_ESCAPE) { leave(); return; }
        if (selected == null) return;
        float step = (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 5f : 1f);
        if (key == Keyboard.KEY_LEFT) selected.px = Ui.clamp(selected.px - step / width, 0f, 1f);
        if (key == Keyboard.KEY_RIGHT) selected.px = Ui.clamp(selected.px + step / width, 0f, 1f);
        if (key == Keyboard.KEY_UP) selected.py = Ui.clamp(selected.py - step / height, 0f, 1f);
        if (key == Keyboard.KEY_DOWN) selected.py = Ui.clamp(selected.py + step / height, 0f, 1f);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
