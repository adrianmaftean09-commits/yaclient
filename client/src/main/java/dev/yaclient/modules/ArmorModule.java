package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Hud;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ArmorModule extends Module {
    public ArmorModule() { super("Armatura", true); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null) return;
        int x = Hud.vw - 62;
        int y = Hud.vh / 2 - 60;
        int lh = Gfx.lineHeight(10, false);
        for (int i = -1; i <= 3; i++) {
            ItemStack st = i == -1 ? mc.thePlayer.getHeldItem() : mc.thePlayer.inventory.armorItemInSlot(3 - i);
            if (st == null) continue;
            String txt = "";
            if (st.isItemStackDamageable()) txt = String.valueOf(st.getMaxDamage() - st.getItemDamage());
            else if (st.stackSize > 1) txt = String.valueOf(st.stackSize);

            Gfx.rr(x, y, 56, 22, 6, Hud.BG);
            if (!txt.isEmpty()) Gfx.text(txt, x + 25, y + (22 - lh) / 2, 10, false, Hud.WHITE);

            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(st, x + 5, y + 3);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
            GlStateManager.disableLighting();
            y += 26;
        }
    }
}
