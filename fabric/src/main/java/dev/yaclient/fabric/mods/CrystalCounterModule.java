package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Gfx;
import dev.yaclient.fabric.HudModule;
import dev.yaclient.fabric.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Crystal PvP supplies: end crystals, obsidian, totems and golden apples. */
public class CrystalCounterModule extends HudModule {
    private static final String[] NAMES = {"Crystals", "Obsidian", "Totems", "Gapples"};

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

    @Override public int width(Minecraft mc) { return 104; }

    @Override public int height(Minecraft mc) { return 10 + NAMES.length * (Gfx.lineHeight(10, false) + 3) - 3; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        int w = width(mc), h = height(mc);
        Gfx.rr(0, 0, w, h, 8, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, w, h, 8, 1, 0x1AFFFFFF);
        int[] v = {count(mc, Items.END_CRYSTAL), count(mc, Items.OBSIDIAN), count(mc, Items.TOTEM_OF_UNDYING), count(mc, Items.GOLDEN_APPLE)};
        int lh = Gfx.lineHeight(10, false), y = 5;
        for (int i = 0; i < NAMES.length; i++) {
            Gfx.text(NAMES[i], 8, y, 10, false, Theme.MUTED);
            String s = String.valueOf(v[i]);
            Gfx.text(s, w - 8 - Gfx.width(s, 10, true), y, 10, true, v[i] == 0 ? 0xFFEF4444 : Theme.TEXT);
            y += lh + 3;
        }
    }
}
