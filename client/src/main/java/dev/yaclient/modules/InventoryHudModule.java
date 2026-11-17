package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Theme;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

/** Your backpack (the 27 inventory slots) shown on the HUD. */
public class InventoryHudModule extends HudModule {
    public InventoryHudModule() { super("inventory_hud", Category.INFO, false, 0.3f, 0.02f); }

    @Override
    public int width() { return 9 * 18 + 8; }

    @Override
    public int height() { return 3 * 18 + 8; }

    @Override
    protected void draw() {
        Gfx.rr(0, 0, width(), height(), 8, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, width(), height(), 8, 1, 0x1AFFFFFF);
        if (mc == null || mc.thePlayer == null) return;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < 27; i++) {
            ItemStack s = mc.thePlayer.inventory.mainInventory[9 + i];
            if (s == null) continue;
            int x = 4 + (i % 9) * 18 + 1, y = 4 + (i / 9) * 18 + 1;
            mc.getRenderItem().renderItemAndEffectIntoGUI(s, x, y);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, s, x, y, null);
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
        GlStateManager.disableLighting();
    }
}
