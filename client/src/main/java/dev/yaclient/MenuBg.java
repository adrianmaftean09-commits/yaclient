package dev.yaclient;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

/** Bright, soft backdrop shared by the title screen and the server list: a gradient with slowly drifting color blobs. */
final class MenuBg {
    private MenuBg() {}

    static final int INK = 0xFF1A1C26, INK2 = 0xFF5B6272;

    static void draw(int w, int h, float t) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(w, 0, 0).color(0xEE, 0xF3, 0xFC, 255).endVertex();
        wr.pos(0, 0, 0).color(0xEE, 0xF3, 0xFC, 255).endVertex();
        wr.pos(0, h, 0).color(0xD5, 0xE0, 0xF4, 255).endVertex();
        wr.pos(w, h, 0).color(0xD5, 0xE0, 0xF4, 255).endVertex();
        tess.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        blob(w * 0.82f + (float) Math.sin(t * 0.21f) * 18f, h * 0.18f, w * 0.55f, h * 0.75f, 0x7FA2FF, 120);
        blob(w * 0.12f + (float) Math.cos(t * 0.17f) * 16f, h * 0.88f, w * 0.5f, h * 0.7f, 0xC9A4FF, 95);
        blob(w * 0.52f + (float) Math.sin(t * 0.13f) * 22f, h * 0.55f, w * 0.38f, h * 0.55f, 0xFFFFFF, 150);
        blob(w * 0.95f, h * 0.95f, w * 0.3f, h * 0.45f, 0xFF9A9A, 55);
    }

    /** Soft radial blob (an ellipse of color fading to transparent), alpha 0..255 at the center. */
    static void blob(float cx, float cy, float rx, float ry, int rgb, int alpha) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(6, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(cx, cy, 0).color(r, g, b, alpha).endVertex();
        for (int i = 0; i <= 32; i++) {
            double a = i * Math.PI * 2 / 32;
            wr.pos(cx + Math.cos(a) * rx, cy + Math.sin(a) * ry, 0).color(r, g, b, 0).endVertex();
        }
        t.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}
