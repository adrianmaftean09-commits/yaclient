package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Hud;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Mouse;

public class KeystrokesModule extends Module {
    public KeystrokesModule() { super("Keystrokes", true); }

    private void key(String label, boolean down, int x, int y, int w, int h) {
        Gfx.rr(x, y, w, h, 5, down ? 0xE6F5F5F5 : Hud.BG);
        int lh = Gfx.lineHeight(10, true);
        Gfx.text(label, x + (w - Gfx.width(label, 10, true)) / 2, y + (h - lh) / 2, 10, true,
                down ? 0xFF101010 : 0xFFF5F5F5);
    }

    @Override
    public void onRender() {
        GameSettings s = mc.gameSettings;
        int x = 6, y = Hud.leftY, k = 22, g = 3;
        key("W", Hud.down(s.keyBindForward), x + k + g, y, k, k);
        key("A", Hud.down(s.keyBindLeft), x, y + k + g, k, k);
        key("S", Hud.down(s.keyBindBack), x + k + g, y + k + g, k, k);
        key("D", Hud.down(s.keyBindRight), x + 2 * (k + g), y + k + g, k, k);
        int half = (3 * k + 2 * g - 4) / 2;
        key("LMB", Mouse.isButtonDown(0), x, y + 2 * (k + g), half, 20);
        key("RMB", Mouse.isButtonDown(1), x + half + 4, y + 2 * (k + g), half, 20);
        key("SPAZIO", Hud.down(s.keyBindJump), x, y + 2 * (k + g) + 23, 3 * k + 2 * g, 16);
        Hud.leftY += 2 * (k + g) + 23 + 16 + 6;
    }
}
