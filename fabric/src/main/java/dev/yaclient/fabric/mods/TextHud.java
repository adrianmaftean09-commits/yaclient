package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Function;

/** A HUD box that shows one or more lines of text computed every frame. */
public class TextHud extends HudModule {
    private final Function<Minecraft, String> fn;

    public TextHud(String id, String name, String desc, Category cat, boolean def, float x, float y, Function<Minecraft, String> fn) {
        super(id, name, desc, cat, def, x, y);
        this.fn = fn;
    }

    private String[] lines(Minecraft mc) {
        try { String s = fn.apply(mc); return (s == null ? "" : s).split("\n"); } catch (Exception e) { return new String[]{"-"}; }
    }

    @Override
    public int width(Minecraft mc) {
        int w = 20;
        for (String l : lines(mc)) w = Math.max(w, mc.font.width(l) + 10);
        return w;
    }

    @Override public int height(Minecraft mc) { return lines(mc).length * 11 + 5; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        g.fill(0, 0, width(mc), height(mc), 0x90101014);
        int y = 4;
        for (String l : lines(mc)) { g.text(mc.font, l, 5, y, 0xFFFFFFFF, true); y += 11; }
    }
}
