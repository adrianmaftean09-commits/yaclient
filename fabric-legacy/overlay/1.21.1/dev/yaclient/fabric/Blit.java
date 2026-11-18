package dev.yaclient.fabric;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** 1.21.1 has no tinted blit: apply the colour through setColor around the plain one. */
public final class Blit {
    private Blit() {}

    public static int abgr(int argb) {
        return (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16);
    }

    public static void blit(GuiGraphics g, ResourceLocation id, int x, int y, float u, float v, int w, int h, int uw, int vh, int tw, int th, int color) {
        g.setColor(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f, ((color >>> 24) & 255) / 255f);
        g.blit(id, x, y, w, h, u, v, uw, vh, tw, th);
        g.setColor(1f, 1f, 1f, 1f);
    }
}
