package dev.yaclient.cosmetics;

import dev.yaclient.Config;
import dev.yaclient.CosmeticsApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import org.lwjgl.opengl.GL11;

import java.util.Map;

/** Render layer that draws the equipped cosmetics (cape, wings, bandana, pet) and the own nametag. */
public class CosmeticLayer implements LayerRenderer<AbstractClientPlayer> {
    private static final float S = 0.0625f;

    private final RenderPlayer renderer;

    public CosmeticLayer(RenderPlayer renderer) { this.renderer = renderer; }

    @Override
    public void doRenderLayer(AbstractClientPlayer p, float limbSwing, float limbSwingAmount, float partialTicks,
                              float age, float netHeadYaw, float headPitch, float scale) {
        Minecraft mc = Minecraft.getMinecraft();
        if (p == mc.thePlayer && mc.gameSettings.thirdPersonView != 0 && mc.currentScreen == null
                && Config.getBool("nametag.self", true)) {
            Nametag.render(p, partialTicks);
        }
        if (p.isInvisible()) return;
        Map<String, String> eq = CosmeticsApi.equippedFor(p);
        if (eq == null) return;
        ModelPlayer model = renderer.getMainModel();

        boolean cape = on(eq, "cape"), wings = on(eq, "wings"), bandana = on(eq, "bandana"), pet = on(eq, "pet");
        if (!cape && !wings && !bandana && !pet) return;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GlStateManager.color(1f, 1f, 1f, 1f);

        if (wings) drawWings(p, model, age);
        if (cape) drawCape(p, partialTicks, age);
        if (bandana) {
            GlStateManager.pushMatrix();
            model.bipedHead.postRender(S);
            Poly.begin();
            CosmeticRender.bandana(age, limbSwingAmount);
            Poly.end();
            GlStateManager.popMatrix();
        }
        if (pet) drawPet(p, age);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    private static boolean on(Map<String, String> eq, String slot) {
        String v = eq.get(slot);
        return v != null && !v.equals("none");
    }

    private void drawWings(AbstractClientPlayer p, ModelPlayer model, float age) {
        boolean air = !p.onGround;
        float flap = (float) Math.sin(age * (air ? 0.32f : 0.10f)) * (air ? 22f : 7f);
        float raise = 4f + flap;
        float spread = 26f + (air ? 8f : 0f);
        GlStateManager.pushMatrix();
        model.bipedBody.postRender(S);
        for (int side = -1; side <= 1; side += 2) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * 0.07f, 0.08f, 0.19f);
            GlStateManager.rotate(-side * spread, 0f, 1f, 0f);
            GlStateManager.rotate(-side * raise, 0f, 0f, 1f);
            GlStateManager.scale((float) side, -1f, 1f);
            Poly.begin();
            CosmeticRender.wing(age);
            Poly.end();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    private void drawCape(AbstractClientPlayer p, float pt, float age) {
        double d0 = p.prevChasingPosX + (p.chasingPosX - p.prevChasingPosX) * pt - (p.prevPosX + (p.posX - p.prevPosX) * pt);
        double d1 = p.prevChasingPosY + (p.chasingPosY - p.prevChasingPosY) * pt - (p.prevPosY + (p.posY - p.prevPosY) * pt);
        double d2 = p.prevChasingPosZ + (p.chasingPosZ - p.prevChasingPosZ) * pt - (p.prevPosZ + (p.posZ - p.prevPosZ) * pt);
        float yaw = p.prevRenderYawOffset + (p.renderYawOffset - p.prevRenderYawOffset) * pt;
        double sin = Math.sin(yaw * Math.PI / 180.0), cos = -Math.cos(yaw * Math.PI / 180.0);
        float f1 = (float) d1 * 10f;
        f1 = Math.max(-6f, Math.min(32f, f1));
        float f2 = Math.max(0f, (float) (d0 * sin + d2 * cos) * 100f);
        float f3 = (float) (d0 * cos - d2 * sin) * 100f;
        float cam = p.prevCameraYaw + (p.cameraYaw - p.prevCameraYaw) * pt;
        f1 += (float) Math.sin((p.prevDistanceWalkedModified + (p.distanceWalkedModified - p.prevDistanceWalkedModified) * pt) * 6f) * 32f * cam;
        float back = p.inventory.armorItemInSlot(2) != null ? 0.0625f : 0f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0f, 0f, 0.125f + back);
        if (p.isSneaking()) {
            f1 += 25f;
            GlStateManager.translate(0f, 0.142f, -0.0178f);
        }
        GlStateManager.rotate(6f + f2 / 2f + f1, 1f, 0f, 0f);
        GlStateManager.rotate(f3 / 2f, 0f, 0f, 1f);
        GlStateManager.rotate(-f3 / 2f, 0f, 1f, 0f);
        Poly.begin();
        CosmeticRender.capeBase(age);
        Poly.end();
        // logo emblem (textured)
        GlStateManager.enableTexture2D();
        LogoTexture.bind();
        Poly.beginTex();
        CosmeticRender.capeEmblem(age);
        Poly.end();
        GlStateManager.disableTexture2D();
        GlStateManager.popMatrix();
    }

    private void drawPet(AbstractClientPlayer p, float age) {
        GlStateManager.pushMatrix();
        double bob = Math.sin(age * 0.10) * 0.035;
        double swayX = Math.sin(age * 0.05) * 0.04, swayZ = Math.cos(age * 0.04) * 0.05;
        GlStateManager.translate((float) (-0.64 + swayX), (float) (-0.20 + bob), (float) (0.04 + swayZ));
        GlStateManager.rotate((float) (Math.sin(age * 0.05) * 12.0), 0f, 1f, 0f);
        Poly.begin();
        CosmeticRender.pet(age);
        Poly.end();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() { return false; }
}
