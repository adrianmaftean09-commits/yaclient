package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class FpsModule extends HudModule {
    public FpsModule() { super("fps", "FPS", "Shows your frames per second", Category.INFO, true, 0.008f, 0.012f); }

    private String text(Minecraft mc) { return mc.getFps() + " FPS"; }

    @Override public int width(Minecraft mc) { return mc.font.width(text(mc)) + 10; }

    @Override public int height(Minecraft mc) { return 16; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        g.fill(0, 0, width(mc), height(mc), 0x90101014);
        g.text(mc.font, text(mc), 5, 4, 0xFFFFFFFF, true);
    }
}
