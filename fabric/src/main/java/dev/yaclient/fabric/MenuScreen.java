package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Mod menu: click a row to toggle, drag a HUD box to move it, mouse wheel over it to scale. */
public class MenuScreen extends Screen {
    private HudModule dragging;
    private float dragDX, dragDY;

    public MenuScreen() { super(Component.literal("YA CLIENT")); }

    @Override public boolean isPauseScreen() { return false; }

    private static final int ROW = 16, W = 130, GAPX = 8;

    private int perCol() { return Math.max(4, (height - 70) / ROW); }

    private int cols() { return (Modules.ALL.size() + perCol() - 1) / perCol(); }

    private int cx(int i) { return listX() + (i / perCol()) * (W + GAPX); }

    private int cy(int i) { return listY() + (i % perCol()) * ROW; }

    private int hit(double mx, double my) {
        for (int i = 0; i < Modules.ALL.size(); i++) if (mx >= cx(i) && mx < cx(i) + W && my >= cy(i) && my < cy(i) + ROW - 2) return i;
        return -1;
    }

    private int listX() { return 12; }

    private int listY() { return 34; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        Minecraft mc = Minecraft.getInstance();
        g.fill(0, 0, width, height, 0x99000000);
        // preview boxes of enabled HUD modules (draggable)
        for (Module m : Modules.ALL) {
            if (m.enabled && m instanceof HudModule h) {
                h.render(g, mc);
                int x = Math.round(h.px * width), y = Math.round(h.py * height);
                g.outline(x, y, Math.round(h.width(mc) * h.scale), Math.round(h.height(mc) * h.scale), 0xFFFFFFFF);
            }
        }
        g.fill(listX() - 6, listY() - 24, listX() + cols() * (W + GAPX) - GAPX + 6, listY() + Math.min(perCol(), Modules.ALL.size()) * ROW + 8, 0xF00A0A0C);
        g.text(mc.font, "YA ", listX(), listY() - 16, 0xFFFFFFFF, true);
        g.text(mc.font, "CLIENT", listX() + mc.font.width("YA "), listY() - 16, 0xFFE11D2E, true);
        int i = 0;
        for (Module m : Modules.ALL) {
            int x = cx(i), y = cy(i);
            boolean hover = hit(mx, my) == i;
            g.fill(x, y, x + W, y + ROW - 2, hover ? 0xFF2A2A30 : 0xFF16161A);
            g.text(mc.font, m.name, x + 6, y + 4, 0xFFFFFFFF, false);
            g.text(mc.font, m.enabled ? "ON" : "OFF", x + W - 6 - mc.font.width(m.enabled ? "ON" : "OFF"), y + 4, m.enabled ? 0xFF55FF55 : 0xFF888890, false);
            i++;
        }
        g.text(mc.font, "Drag HUD boxes to move - mouse wheel to resize", width / 2 - 100, height - 14, 0xFF9A9AA2, false);
    }

    private HudModule hudAt(double mx, double my) {
        Minecraft mc = Minecraft.getInstance();
        for (int i = Modules.ALL.size() - 1; i >= 0; i--) {
            Module m = Modules.ALL.get(i);
            if (m.enabled && m instanceof HudModule h) {
                int x = Math.round(h.px * width), y = Math.round(h.py * height);
                if (mx >= x && mx <= x + h.width(mc) * h.scale && my >= y && my <= y + h.height(mc) * h.scale) return h;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
        double mx = e.x(), my = e.y();
        int hi = hit(mx, my);
        if (hi >= 0) { Modules.ALL.get(hi).toggle(); return true; }
        HudModule h = hudAt(mx, my);
        if (h != null) {
            dragging = h;
            dragDX = (float) (mx - h.px * width);
            dragDY = (float) (my - h.py * height);
            return true;
        }
        return super.mouseClicked(e, dbl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if (dragging != null) {
            dragging.px = Math.max(0f, Math.min(0.98f, (float) ((e.x() - dragDX) / width)));
            dragging.py = Math.max(0f, Math.min(0.98f, (float) ((e.y() - dragDY) / height)));
            return true;
        }
        return super.mouseDragged(e, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent e) {
        if (dragging != null) { dragging.save(); Config.save(); dragging = null; return true; }
        return super.mouseReleased(e);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        HudModule h = hudAt(mx, my);
        if (h != null) {
            h.scale = Math.max(0.5f, Math.min(2.5f, h.scale + (float) sy * 0.1f));
            h.save();
            Config.save();
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }
}
