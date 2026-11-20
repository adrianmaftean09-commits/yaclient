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
        wr.pos(w, 0, 0).color(0xEC, 0xF0, 0xF8, 255).endVertex();
        wr.pos(0, 0, 0).color(0xF7, 0xF9, 0xFC, 255).endVertex();
        wr.pos(0, h, 0).color(0xE6, 0xEB, 0xF4, 255).endVertex();
        wr.pos(w, h, 0).color(0xD9, 0xE0, 0xEE, 255).endVertex();
        tess.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        // restrained: one cool glow top right, a faint warm one bottom left, a white lift behind the menu
        blob(w * 0.88f + (float) Math.sin(t * 0.15f) * 10f, h * 0.05f, w * 0.55f, h * 0.8f, 0x8FB0F0, 70);
        blob(w * 0.05f, h * 1.0f, w * 0.45f, h * 0.7f, 0xE2B8C8, 45);
        blob(w * 0.50f, h * 0.48f, w * 0.34f, h * 0.55f, 0xFFFFFF, 160);
        dots(w, h);
    }

    /** A faint dot grid over the backdrop: gives the flat gradient some structure. */
    static void dots(int w, int h) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (int y = 9; y < h; y += 14) {
            for (int x = 9; x < w; x += 14) {
                wr.pos(x, y, 0).color(0x1A, 0x22, 0x38, 14).endVertex();
                wr.pos(x, y + 1, 0).color(0x1A, 0x22, 0x38, 14).endVertex();
                wr.pos(x + 1, y + 1, 0).color(0x1A, 0x22, 0x38, 14).endVertex();
                wr.pos(x + 1, y, 0).color(0x1A, 0x22, 0x38, 14).endVertex();
            }
        }
        tess.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /** Flat translucent disc. */
    static void disc(float cx, float cy, float r, int rgb, int alpha) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        int rr = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(6, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(cx, cy, 0).color(rr, g, b, alpha).endVertex();
        for (int i = 0; i <= 40; i++) {
            double a = i * Math.PI * 2 / 40;
            wr.pos(cx + Math.cos(a) * r, cy + Math.sin(a) * r, 0).color(rr, g, b, (int) (alpha * 0.45f)).endVertex();
        }
        tess.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /** Thin elliptical ring (a floor marker under the player). */
    static void ring(float cx, float cy, float rx, float ry, float th, int rgb, int alpha) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
        int n = 64;
        for (int i = 0; i < n; i++) {
            double a0 = i * Math.PI * 2 / n, a1 = (i + 1) * Math.PI * 2 / n;
            wr.pos(cx + Math.cos(a0) * rx, cy + Math.sin(a0) * ry, 0).color(r, g, b, alpha).endVertex();
            wr.pos(cx + Math.cos(a0) * (rx - th), cy + Math.sin(a0) * (ry - th * ry / rx), 0).color(r, g, b, alpha).endVertex();
            wr.pos(cx + Math.cos(a1) * (rx - th), cy + Math.sin(a1) * (ry - th * ry / rx), 0).color(r, g, b, alpha).endVertex();
            wr.pos(cx + Math.cos(a1) * rx, cy + Math.sin(a1) * ry, 0).color(r, g, b, alpha).endVertex();
        }
        tess.draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
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
