package dev.yaclient.cosmetics;

import dev.yaclient.Gfx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/** Smooth nametag (logo + player name) drawn above your own head in third person (F5). */
final class Nametag {
    private static final int SS = 4;          // supersampling of the label image
    private static final int H = 16;          // logical height in label pixels
    private static String cachedName;
    private static DynamicTexture tex;
    private static int logicalW;

    private Nametag() {}

    private static void build(String name) {
        if (tex != null) tex.deleteGlTexture();
        Font font = new Font(Gfx.family(), Font.BOLD, 10 * SS);
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = probe.createGraphics();
        FontMetrics fm = pg.getFontMetrics(font);
        int textW = fm.stringWidth(name) / SS + 1;
        pg.dispose();
        int logo = 11, pad = 6, gap = 5;
        logicalW = pad + logo + gap + textW + pad;
        BufferedImage img = new BufferedImage(logicalW * SS, H * SS, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        float w = logicalW * SS, h = H * SS;
        g.setColor(new Color(12, 12, 16, 205));
        g.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
        g.setColor(new Color(225, 6, 0, 150));
        g.setStroke(new java.awt.BasicStroke(1.2f * SS / 2f));
        g.draw(new RoundRectangle2D.Float(SS * 0.4f, SS * 0.4f, w - SS * 0.8f, h - SS * 0.8f, h - SS * 0.8f, h - SS * 0.8f));
        g.drawImage(LogoTexture.image(), pad * SS, (H - logo) * SS / 2, logo * SS, logo * SS, null);
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString(name, (pad + logo + gap) * SS, (H * SS - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
        tex = new DynamicTexture(img);
        GlStateManager.bindTexture(tex.getGlTextureId());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        cachedName = name;
    }

    private static float lerpYaw(float a, float b, float t) {
        float d = b - a;
        while (d < -180f) d += 360f;
        while (d >= 180f) d -= 360f;
        return a + d * t;
    }

    /**
     * Called from the player's render layer. The current matrix is the player's model matrix, so it is
     * first undone back to "entity position, no rotation" and then the label is billboarded.
     */
    static void render(AbstractClientPlayer p, float pt) {
        Minecraft mc = Minecraft.getMinecraft();
        String name = p.getName();
        if (name == null || name.isEmpty()) return;
        if (!name.equals(cachedName) || tex == null) build(name);

        float bodyYaw = lerpYaw(p.prevRenderYawOffset, p.renderYawOffset, pt);
        GlStateManager.pushMatrix();
        // undo RendererLivingEntity: translate, rotate(180-yaw), scale(-1,-1,1), scale(0.9375), translate(0,-1.5078,0)
        GlStateManager.translate(0f, 1.5078125f, 0f);
        GlStateManager.scale(1f / 0.9375f, 1f / 0.9375f, 1f / 0.9375f);
        GlStateManager.scale(-1f, -1f, 1f);
        GlStateManager.rotate(-(180f - bodyYaw), 0f, 1f, 0f);
        // billboard above the head
        GlStateManager.translate(0f, p.height + (p.isSneaking() ? 0.25f : 0.45f), 0f);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1f, 0f, 0f);
        float s = 0.02f;
        GlStateManager.scale(-s, -s, s);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.bindTexture(tex.getGlTextureId());
        float hw = logicalW / 2f, hh = H / 2f;
        Poly.beginTex();
        Poly.tex(-hw, -hh, 0, 0f, 0f, 1f, 1f, 1f, 1f);
        Poly.tex(-hw, hh, 0, 0f, 1f, 1f, 1f, 1f, 1f);
        Poly.tex(hw, hh, 0, 1f, 1f, 1f, 1f, 1f, 1f);
        Poly.tex(hw, -hh, 0, 1f, 0f, 1f, 1f, 1f, 1f);
        Poly.end();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
