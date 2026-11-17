package dev.yaclient;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/** Animated photo backdrop for the title screen (assets/yaclient/menu_bg.png, drawn at full resolution, never downscaled). */
final class MenuBackdrop {
    private static DynamicTexture tex;
    private static boolean failed;
    private static final float AR = 16f / 9f, SUN_U = 0.51f, SUN_V = 0.385f;

    private MenuBackdrop() {}

    /** @return false if the image is missing so the caller can fall back to the panorama. */
    static boolean draw(int w, int h, float t) {
        if (tex == null && !failed) {
            try {
                InputStream in = MenuBackdrop.class.getResourceAsStream("/assets/yaclient/menu_bg.png");
                if (in != null) {
                    try {
                        BufferedImage img = ImageIO.read(in);
                        tex = new DynamicTexture(img);
                        GlStateManager.bindTexture(tex.getGlTextureId());
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                    } finally { in.close(); }
                } else failed = true;
            } catch (Throwable e) { failed = true; }
        }
        if (tex == null) return false;

        float sa = (float) w / h;
        float uR = sa > AR ? 1f : sa / AR, vR = sa > AR ? AR / sa : 1f;
        double tau = Math.PI * 2;

        GlStateManager.shadeModel(7425);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.bindTexture(tex.getGlTextureId());

        // layer A: slow drifting zoom of the photo
        float zA = 1.10f + 0.05f * (float) Math.sin(t * tau / 46);
        float pxA = 0.38f * (float) Math.sin(t * tau / 52), pyA = 0.30f * (float) Math.sin(t * tau / 41 + 1);
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        quad(w, h, uR, vR, zA, pxA, pyA);

        // layer B: translucent copy drifting the other way = water shimmer
        float zB = 1.16f + 0.04f * (float) Math.sin(t * tau / 31 + 2);
        float pxB = -0.40f * (float) Math.sin(t * tau / 29), pyB = 0.25f * (float) Math.sin(t * tau / 37 + 3);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1f, 1f, 1f, 0.34f);
        quad(w, h, uR, vR, zB, pxB, pyB);
        GlStateManager.color(1f, 1f, 1f, 1f);

        // pulsing warm sun glow (additive), following the sun through the current view window
        float uw = uR / zA, vh = vR / zA;
        float u0 = (1 - uw) * (0.5f + pxA), v0 = (1 - vh) * (0.5f + pyA);
        float sx = (SUN_U - u0) / uw * w, sy = (SUN_V - v0) / vh * h;
        float pulse = 0.65f + 0.35f * (float) Math.sin(t * tau / 7);
        float rad = Math.max(w, h) * 0.42f * (0.95f + 0.08f * pulse);
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        Tessellator ts = Tessellator.getInstance();
        WorldRenderer wr = ts.getWorldRenderer();
        wr.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(sx, sy, 0).color(255, 226, 150, (int) (92 * pulse)).endVertex();
        for (int i = 0; i <= 40; i++) {
            double a = i * tau / 40;
            wr.pos(sx + Math.cos(a) * rad, sy + Math.sin(a) * rad * 0.7, 0).color(255, 200, 120, 0).endVertex();
        }
        ts.draw();

        // readability gradient (top/bottom)
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(w, 0, 0).color(0, 0, 0, 90).endVertex();
        wr.pos(0, 0, 0).color(0, 0, 0, 90).endVertex();
        wr.pos(0, h * 0.35, 0).color(0, 0, 0, 0).endVertex();
        wr.pos(w, h * 0.35, 0).color(0, 0, 0, 0).endVertex();
        ts.draw();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(w, h * 0.55, 0).color(0, 0, 0, 0).endVertex();
        wr.pos(0, h * 0.55, 0).color(0, 0, 0, 0).endVertex();
        wr.pos(0, h, 0).color(0, 0, 0, 105).endVertex();
        wr.pos(w, h, 0).color(0, 0, 0, 105).endVertex();
        ts.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.color(1f, 1f, 1f, 1f);
        return true;
    }

    private static void quad(int w, int h, float uR, float vR, float z, float px, float py) {
        float uw = uR / z, vh = vR / z;
        float u0 = (1 - uw) * (0.5f + px), v0 = (1 - vh) * (0.5f + py);
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(0, h, 0).tex(u0, v0 + vh).endVertex();
        wr.pos(w, h, 0).tex(u0 + uw, v0 + vh).endVertex();
        wr.pos(w, 0, 0).tex(u0 + uw, v0).endVertex();
        wr.pos(0, 0, 0).tex(u0, v0).endVertex();
        t.draw();
    }
}
