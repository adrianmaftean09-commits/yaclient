package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Crystal PvP supplies: end crystals, obsidian, totems and golden apples in your inventory (and offhand). */
public class CrystalCounterModule extends HudModule {
    public CrystalCounterModule() { super("crystalcount", "Crystal Supplies", "Counts crystals, obsidian, totems and gapples", Category.CRYSTAL, false, 0.008f, 0.5f); }

    private static int count(Minecraft mc, Item item) {
        if (mc.player == null) return 0;
        Inventory inv = mc.player.getInventory();
        int n = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.is(item)) n += s.getCount();
        }
        return n;
    }

    private static final String[] NAMES = {"Crystals", "Obsidian", "Totems", "Gapples"};

    private int[] values(Minecraft mc) {
        return new int[]{count(mc, Items.END_CRYSTAL), count(mc, Items.OBSIDIAN), count(mc, Items.TOTEM_OF_UNDYING), count(mc, Items.GOLDEN_APPLE)};
    }

    @Override public int width(Minecraft mc) { return 84; }

    @Override public int height(Minecraft mc) { return 4 + NAMES.length * 11 + 3; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        g.fill(0, 0, width(mc), height(mc), 0x90101014);
        int[] v = values(mc);
        for (int i = 0; i < NAMES.length; i++) {
            int y = 4 + i * 11;
            g.text(mc.font, NAMES[i], 5, y, 0xFFB0B0B8, true);
            String s = String.valueOf(v[i]);
            g.text(mc.font, s, width(mc) - 5 - mc.font.width(s), y, v[i] == 0 ? 0xFFFF5555 : 0xFFFFFFFF, true);
        }
    }
}
