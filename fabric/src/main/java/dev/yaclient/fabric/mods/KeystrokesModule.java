package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** WASD and jump keys on the HUD. */
public class KeystrokesModule extends HudModule {
    private static final int K = 18, GAP = 2;

    public KeystrokesModule() { super("keystrokes", "Keystrokes", "WASD and space bar overlay", Category.PVP, true, 0.008f, 0.3f); }

    @Override public int width(Minecraft mc) { return K * 3 + GAP * 2; }

    @Override public int height(Minecraft mc) { return K * 3 + GAP * 2; }

    private void key(GuiGraphicsExtractor g, Minecraft mc, KeyMapping k, String label, int x, int y, int w) {
        boolean down = k.isDown();
        g.fill(x, y, x + w, y + K, down ? 0xE0F4F4F5 : 0x90101014);
        int tw = mc.font.width(label);
        g.text(mc.font, label, x + (w - tw) / 2, y + (K - 8) / 2, down ? 0xFF0A0A0C : 0xFFFFFFFF, false);
    }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        key(g, mc, mc.options.keyUp, "W", K + GAP, 0, K);
        key(g, mc, mc.options.keyLeft, "A", 0, K + GAP, K);
        key(g, mc, mc.options.keyDown, "S", K + GAP, K + GAP, K);
        key(g, mc, mc.options.keyRight, "D", (K + GAP) * 2, K + GAP, K);
        key(g, mc, mc.options.keyJump, "SPACE", 0, (K + GAP) * 2, K * 3 + GAP * 2);
    }
}
