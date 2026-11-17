package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Full screen things: custom crosshair and the low health vignette. They have no draggable box. */
public final class Overlays {
    private Overlays() {}

    public static class Crosshair extends HudModule {
        public Crosshair() { super("crosshair", "Crosshair", "Small white cross in the middle (hides nothing else)", Category.PVP, false, 0.5f, 0.5f); }

        @Override public int width(Minecraft mc) { return 13; }

        @Override public int height(Minecraft mc) { return 13; }

        @Override
        public void draw(GuiGraphicsExtractor g, Minecraft mc) {
            g.fill(6, 0, 7, 5, 0xFFF4F4F5);
            g.fill(6, 8, 7, 13, 0xFFF4F4F5);
            g.fill(0, 6, 5, 7, 0xFFF4F4F5);
            g.fill(8, 6, 13, 7, 0xFFF4F4F5);
        }
    }

    public static class LowHealth extends HudModule {
        public LowHealth() { super("lowhealth", "Low Health", "Red border when your health is low", Category.PVP, false, 0f, 0f); }

        @Override public int width(Minecraft mc) { return 0; }

        @Override public int height(Minecraft mc) { return 0; }

        @Override
        public void draw(GuiGraphicsExtractor g, Minecraft mc) {
            if (mc.player == null || mc.player.getHealth() > 6f) return;
            int w = g.guiWidth(), h = g.guiHeight(), t = 8, c = 0x66FF0000;
            g.pose().pushMatrix();
            g.pose().scale(1f / scale, 1f / scale);
            g.pose().translate(-Math.round(px * w), -Math.round(py * h));
            g.fill(0, 0, w, t, c);
            g.fill(0, h - t, w, h, c);
            g.fill(0, t, t, h - t, c);
            g.fill(w - t, t, w, h - t, c);
            g.pose().popMatrix();
        }
    }
}
