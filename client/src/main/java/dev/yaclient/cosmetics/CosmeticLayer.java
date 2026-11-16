package dev.yaclient.cosmetics;

import dev.yaclient.CosmeticsApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

import java.util.Map;

/** Render layer that draws the equipped cosmetics (wings, cape, hat, bandana) on a player. */
public class CosmeticLayer implements LayerRenderer<AbstractClientPlayer> {
    private static final float S = 0.0625f;

    private final RenderPlayer renderer;
    private final CosmeticModels.Wings wings = new CosmeticModels.Wings();
    private final CosmeticModels.Cape cape = new CosmeticModels.Cape();
    private final CosmeticModels.TopHat topHat = new CosmeticModels.TopHat();
    private final CosmeticModels.Crown crown = new CosmeticModels.Crown();
    private final CosmeticModels.Bandana bandana = new CosmeticModels.Bandana();

    public CosmeticLayer(RenderPlayer renderer) { this.renderer = renderer; }

    @Override
    public void doRenderLayer(AbstractClientPlayer p, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (p.isInvisible()) return;
        Map<String, String> eq = CosmeticsApi.equippedFor(p);
        if (eq == null) return;
        ModelPlayer model = renderer.getMainModel();

        String w = eq.get("wings");
        if (w != null && !w.equals("none")) {
            begin(w, true);
            model.bipedBody.postRender(S);
            wings.render(ageInTicks, S);
            end();
        }

        String c = eq.get("cape");
        if (c != null && !c.equals("none")) {
            begin(c, false);
            model.bipedBody.postRender(S);
            float angle = (float) Math.toRadians(8f + Math.min(1f, limbSwingAmount) * 35f + (p.isSneaking() ? 22f : 0f));
            float back = p.inventory.armorItemInSlot(2) != null ? 0.9f : 0f;
            cape.render(angle, back, S);
            end();
        }

        String h = eq.get("hat");
        if (h != null && !h.equals("none")) {
            begin(h, true);
            model.bipedHead.postRender(S);
            if (h.equals("crown")) crown.render(S); else topHat.render(S);
            end();
        }

        String b = eq.get("bandana");
        if (b != null && !b.equals("none")) {
            begin(b, true);
            model.bipedHead.postRender(S);
            bandana.render(S);
            end();
        }
    }

    private void begin(String item, boolean cullOff) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(CosmeticTextures.get(item));
        GlStateManager.pushMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        if (cullOff) GlStateManager.disableCull();
    }

    private void end() {
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() { return false; }
}
