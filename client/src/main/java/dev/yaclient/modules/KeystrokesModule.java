package dev.yaclient.modules;

import dev.yaclient.YAClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class KeystrokesModule extends Module {
    public KeystrokesModule() { super("Keystrokes", true); }

    private void key(String label, boolean down, int x, int y, int w, int h) {
        Gui.drawRect(x, y, x + w, y + h, down ? YAClient.RED : YAClient.BLACK_BG);
        mc.fontRendererObj.drawStringWithShadow(label,
                x + (w - mc.fontRendererObj.getStringWidth(label)) / 2f,
                y + (h - 8) / 2f, YAClient.WHITE);
    }

    @Override
    public void onRender() {
        GameSettings s = mc.gameSettings;
        int x = 4, y = 60, k = 22, g = 2, full = 3 * k + 2 * g;
        key("W", Keyboard.isKeyDown(s.keyBindForward.getKeyCode()), x + k + g, y, k, k);
        key("A", Keyboard.isKeyDown(s.keyBindLeft.getKeyCode()), x, y + k + g, k, k);
        key("S", Keyboard.isKeyDown(s.keyBindBack.getKeyCode()), x + k + g, y + k + g, k, k);
        key("D", Keyboard.isKeyDown(s.keyBindRight.getKeyCode()), x + 2 * (k + g), y + k + g, k, k);
        key("LMB", Mouse.isButtonDown(0), x, y + 2 * (k + g), full / 2 - 1, 18);
        key("RMB", Mouse.isButtonDown(1), x + full / 2 + 1, y + 2 * (k + g), full / 2 - 1, 18);
        key("SPAZIO", Keyboard.isKeyDown(s.keyBindJump.getKeyCode()), x, y + 2 * (k + g) + 20, full, 14);
    }
}
