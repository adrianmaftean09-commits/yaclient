package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;

/** How many ender pearls, golden apples, splash potions and arrows you carry. */
public class ItemCounterModule extends HudModule {
    public ItemCounterModule() { super("item_counter", Category.PVP, false, 0.008f, 0.62f); }

    private final Setting.Bool hideZero = add(new Setting.Bool("hide_zero", false));

    private static final int ROW = 20, W = 52;

    private ItemStack[] icons() {
        return new ItemStack[]{new ItemStack(Items.ender_pearl), new ItemStack(Items.golden_apple), new ItemStack(Items.potionitem, 1, 16421), new ItemStack(Items.arrow)};
    }

    private int count(int kind) {
        if (mc == null || mc.thePlayer == null) return 0;
        int n = 0;
        for (ItemStack s : mc.thePlayer.inventory.mainInventory) {
            if (s == null) continue;
            boolean m;
            switch (kind) {
                case 0: m = s.getItem() == Items.ender_pearl; break;
                case 1: m = s.getItem() == Items.golden_apple; break;
                case 2: m = s.getItem() instanceof ItemPotion && (s.getMetadata() & 16384) != 0; break;
                default: m = s.getItem() == Items.arrow; break;
            }
            if (m) n += s.stackSize;
        }
        return n;
    }

    private int rows() {
        if (!hideZero.value) return 4;
        int r = 0;
        for (int i = 0; i < 4; i++) if (count(i) > 0) r++;
        return preview && r == 0 ? 4 : r;
    }

    @Override
    public int width() { return rows() == 0 ? 0 : W; }

    @Override
    public int height() { return Math.max(ROW, rows() * (ROW + 2) - 2); }

    @Override
    protected void draw() {
        ItemStack[] ic = icons();
        boolean fake = preview && rows() == 4 && count(0) + count(1) + count(2) + count(3) == 0;
        int y = 0;
        for (int i = 0; i < 4; i++) {
            int n = fake ? 8 + i : count(i);
            if (n <= 0 && hideZero.value) continue;
            Gfx.rr(0, y, W, ROW, 6, Theme.HUD_BG);
            Gfx.rrOutline(0, y, W, ROW, 6, 1, 0x1AFFFFFF);
            Gfx.text(String.valueOf(n), 24, y + (ROW - Gfx.lineHeight(10, false)) / 2, 10, false, n > 0 ? Theme.TEXT : 0x80FFFFFF);
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.color(1f, 1f, 1f, 1f);
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(ic[i], 4, y + 2);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
            GlStateManager.disableLighting();
            y += ROW + 2;
        }
    }
}
