package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/** The 27 inventory slots on the HUD. */
public class InventoryHud extends HudModule {
    public InventoryHud() { super("inventory", "Inventory", "Shows your inventory on the HUD", Category.UTILITY, false, 0.8f, 0.5f); }

    @Override public int width(Minecraft mc) { return 9 * 18 + 4; }

    @Override public int height(Minecraft mc) { return 3 * 18 + 4; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        g.fill(0, 0, width(mc), height(mc), 0x90101014);
        if (mc.player == null) return;
        for (int i = 0; i < 27; i++) {
            ItemStack st = mc.player.getInventory().getItem(9 + i);
            if (st.isEmpty()) continue;
            int x = 2 + (i % 9) * 18, y = 2 + (i / 9) * 18;
            g.item(st, x + 1, y + 1);
            g.itemDecorations(mc.font, st, x + 1, y + 1);
        }
    }
}
