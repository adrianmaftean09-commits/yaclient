package dev.yaclient.cosmetics;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/** The gold "+" with a soft glow that marks YA Plus players (tab list and above the head). Drawn in the current 2D/billboard space. */
public final class PlusBadge {
    private PlusBadge() {}

    private static void rect(float x0, float y0, float x1, float y1) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x0, y1); GL11.glVertex2f(x1, y1); GL11.glVertex2f(x1, y0); GL11.glVertex2f(x0, y0);
        GL11.glEnd();
    }

    /** Draws the badge in the square (x, y, s, s). */
    public static void draw(float x, float y, float s) {
        float t = (float) ((System.nanoTime() / 1000000L) % 100000L) / 1000f;
        float pulse = 0.72f + 0.28f * (float) Math.sin(t * 3.2f);
        float cx = x + s / 2f, cy = y + s / 2f;
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        // glow (additive)
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glColor4f(1f, 0.80f, 0.22f, 0.60f * pulse);
        GL11.glVertex2f(cx, cy);
        GL11.glColor4f(1f, 0.62f, 0.05f, 0f);
        for (int i = 0; i <= 28; i++) {
            double a = Math.PI * 2 * i / 28;
            GL11.glVertex2f(cx + (float) Math.cos(a) * s * 1.0f, cy + (float) Math.sin(a) * s * 1.0f);
        }
        GL11.glEnd();
        // the plus itself
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        float a = s * 0.40f, b = s * 0.13f;
        GL11.glColor4f(0.80f, 0.55f, 0.05f, 1f);   // dark gold edge
        rect(cx - a - 0.5f, cy - b - 0.5f, cx + a + 0.5f, cy + b + 0.5f);
        rect(cx - b - 0.5f, cy - a - 0.5f, cx + b + 0.5f, cy + a + 0.5f);
        GL11.glColor4f(1f, 0.84f, 0.26f, 1f);       // gold
        rect(cx - a, cy - b, cx + a, cy + b);
        rect(cx - b, cy - a, cx + b, cy + a);
        GL11.glColor4f(1f, 0.96f, 0.72f, 1f);       // highlight
        rect(cx - a * 0.55f, cy - b * 0.4f, cx + a * 0.55f, cy + b * 0.1f);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();
        if (cull) GlStateManager.enableCull();
    }
}
