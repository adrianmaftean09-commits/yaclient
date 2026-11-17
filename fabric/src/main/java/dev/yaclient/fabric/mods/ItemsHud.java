package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Armor (and held item) with durability bars. */
public class ItemsHud extends HudModule {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};

    public ItemsHud() { super("armor", "Armor", "Armor and held items with durability", Category.PVP, false, 0.008f, 0.62f); }

    @Override public int width(Minecraft mc) { return 62; }

    @Override public int height(Minecraft mc) { return SLOTS.length * 18 + 4; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        g.fill(0, 0, width(mc), height(mc), 0x90101014);
        if (mc.player == null) return;
        int y = 2;
        for (EquipmentSlot s : SLOTS) {
            ItemStack st = mc.player.getItemBySlot(s);
            if (!st.isEmpty()) {
                g.item(st, 3, y);
                if (st.isDamageableItem()) {
                    int left = st.getMaxDamage() - st.getDamageValue();
                    int pct = left * 100 / Math.max(1, st.getMaxDamage());
                    int col = pct > 50 ? 0xFF55FF55 : pct > 20 ? 0xFFFFFF55 : 0xFFFF5555;
                    g.text(mc.font, left + "", 23, y + 4, col, true);
                } else if (st.getCount() > 1) {
                    g.text(mc.font, "x" + st.getCount(), 23, y + 4, 0xFFFFFFFF, true);
                }
            }
            y += 18;
        }
    }
}
