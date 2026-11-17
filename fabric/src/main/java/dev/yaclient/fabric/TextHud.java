package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Function;

/** One line in a rounded pill, or several lines in a card (same look as the 1.8.9 pills). */
public class TextHud extends HudModule {
    private final Function<Minecraft, String> fn;
    private String sample;

    public TextHud(String id, String name, String desc, Category cat, boolean def, float x, float y, Function<Minecraft, String> fn) {
        super(id, name, desc, cat, def, x, y);
        this.fn = fn;
        this.sample = name;
    }

    private String[] lines(Minecraft mc) {
        String s;
        try { s = fn.apply(mc); } catch (Exception e) { s = ""; }
        if (s == null || s.isEmpty()) return new String[0];
        return s.split("\n");
    }

    @Override
    public int width(Minecraft mc) {
        String[] l = lines(mc);
        if (l.length == 0) return 0;
        int w = 0;
        for (String s : l) w = Math.max(w, Gfx.width(s, 10, false));
        return w + 16;
    }

    @Override
    public int height(Minecraft mc) {
        int n = lines(mc).length;
        if (n == 0) return 0;
        return n == 1 ? 16 : 10 + n * (Gfx.lineHeight(10, false) + 3) - 3;
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        String[] l = lines(mc);
        if (l.length == 0) return;
        int w = width(mc), h = height(mc), r = l.length == 1 ? 6 : 8;
        Gfx.rr(0, 0, w, h, r, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, w, h, r, 1, 0x1AFFFFFF);
        int lh = Gfx.lineHeight(10, false);
        if (l.length == 1) {
            Gfx.text(l[0], 8, (h - lh) / 2, 10, false, Theme.TEXT);
        } else {
            int y = 5;
            for (String s : l) { Gfx.text(s, 8, y, 10, false, Theme.TEXT); y += lh + 3; }
        }
    }
}
