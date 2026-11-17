package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CoordsModule extends HudModule {
    public CoordsModule() { super("coords", "Coordinates", "Shows your XYZ position", Category.INFO, false, 0.008f, 0.116f); }

    private String text(Minecraft mc) {
        if (mc.player == null) return "XYZ";
        return "XYZ " + (int) Math.floor(mc.player.getX()) + " " + (int) Math.floor(mc.player.getY()) + " " + (int) Math.floor(mc.player.getZ());
    }

    @Override public int width(Minecraft mc) { return mc.font.width(text(mc)) + 10; }

    @Override public int height(Minecraft mc) { return 16; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        g.fill(0, 0, width(mc), height(mc), 0x90101014);
        g.text(mc.font, text(mc), 5, 4, 0xFFFFFFFF, true);
    }
}
