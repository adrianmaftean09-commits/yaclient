package dev.yaclient.modules;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ArmorModule extends Module {
    public ArmorModule() { super("Armatura", true); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null) return;
        ScaledResolution sr = new ScaledResolution(mc);
        int x = sr.getScaledWidth() - 20;
        int y = sr.getScaledHeight() / 2 - 40;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 3; i >= -1; i--) {
            ItemStack st = i == -1 ? mc.thePlayer.getHeldItem() : mc.thePlayer.inventory.armorItemInSlot(i);
            if (st != null) {
                mc.getRenderItem().renderItemAndEffectIntoGUI(st, x, y);
                if (st.isItemStackDamageable()) {
                    String left = String.valueOf(st.getMaxDamage() - st.getItemDamage());
                    mc.fontRendererObj.drawStringWithShadow(left, x - mc.fontRendererObj.getStringWidth(left) - 2, y + 4, 0xFFF5F5F5);
                }
            }
            y += 18;
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
