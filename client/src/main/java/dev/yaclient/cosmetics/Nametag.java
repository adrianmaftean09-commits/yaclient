package dev.yaclient.cosmetics;

import net.minecraft.client.Minecraft;
import dev.yaclient.CosmeticsApi;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/** Vanilla-style nametag above your own head in third person (F5), with the YA logo next to the name. */
final class Nametag {
    private static final float LOGO = 10f, GAP = 3f;

    private Nametag() {}

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
        FontRenderer fr = mc.fontRendererObj;
        float textW = fr.getStringWidth(name);
        boolean plus = CosmeticsApi.plus();
        float badgeW = LOGO + (plus ? LOGO + 1f : 0f);
        float x0 = -(badgeW + GAP + textW) / 2f;

        float bodyYaw = lerpYaw(p.prevRenderYawOffset, p.renderYawOffset, pt);
        GlStateManager.pushMatrix();
        // undo RendererLivingEntity: translate, rotate(180-yaw), scale(-1,-1,1), scale(0.9375), translate(0,-1.5078,0)
        GlStateManager.translate(0f, 1.5078125f, 0f);
        GlStateManager.scale(1f / 0.9375f, 1f / 0.9375f, 1f / 0.9375f);
        GlStateManager.scale(-1f, -1f, 1f);
        GlStateManager.rotate(-(180f - bodyYaw), 0f, 1f, 0f);
        // billboard above the head, same height and size as a vanilla nametag
        GlStateManager.translate(0f, p.height + (p.isSneaking() ? 0.25f : 0.5f), 0f);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1f, 0f, 0f);
        float s = 0.016666668f * 1.6f;
        GlStateManager.scale(-s, -s, s);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        // logo
        LogoTexture.bind();
        Poly.beginTex();
        Poly.tex(x0, -1f, 0, 0f, 0f, 1f, 1f, 1f, 1f);
        Poly.tex(x0, -1f + LOGO, 0, 0f, 1f, 1f, 1f, 1f, 1f);
        Poly.tex(x0 + LOGO, -1f + LOGO, 0, 1f, 1f, 1f, 1f, 1f, 1f);
        Poly.tex(x0 + LOGO, -1f, 0, 1f, 0f, 1f, 1f, 1f, 1f);
        Poly.end();
        // name, normal Minecraft font
        if (plus) PlusBadge.draw(x0 + LOGO + 1f, -1f, LOGO);
        fr.drawStringWithShadow(name, x0 + badgeW + GAP, 0f, 0xFFFFFFFF);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    /** Other YA CLIENT players: the logo (and the gold plus for YA Plus) just left of their vanilla nametag. */
    static void renderOther(AbstractClientPlayer p, float pt, boolean plus) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.hideGUI || p.isInvisible() || p.isSneaking() || p.getDistanceSqToEntity(mc.thePlayer) > 48 * 48) return;
        FontRenderer fr = mc.fontRendererObj;
        float textW = fr.getStringWidth(p.getDisplayName().getFormattedText());
        float groupW = LOGO + (plus ? LOGO + 1f : 0f);
        float x0 = -textW / 2f - GAP - groupW;
        float bodyYaw = lerpYaw(p.prevRenderYawOffset, p.renderYawOffset, pt);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0f, 1.5078125f, 0f);
        GlStateManager.scale(1f / 0.9375f, 1f / 0.9375f, 1f / 0.9375f);
        GlStateManager.scale(-1f, -1f, 1f);
        GlStateManager.rotate(-(180f - bodyYaw), 0f, 1f, 0f);
        GlStateManager.translate(0f, p.height + 0.5f, 0f);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1f, 0f, 0f);
        float s = 0.016666668f * 1.6f;
        GlStateManager.scale(-s, -s, s);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        LogoTexture.bind();
        Poly.beginTex();
        Poly.tex(x0, -1f, 0, 0f, 0f, 1f, 1f, 1f, 1f);
        Poly.tex(x0, -1f + LOGO, 0, 0f, 1f, 1f, 1f, 1f, 1f);
        Poly.tex(x0 + LOGO, -1f + LOGO, 0, 1f, 1f, 1f, 1f, 1f, 1f);
        Poly.tex(x0 + LOGO, -1f, 0, 1f, 0f, 1f, 1f, 1f, 1f);
        Poly.end();
        if (plus) PlusBadge.draw(x0 + LOGO + 1f, -1f, LOGO);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
