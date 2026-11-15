package dev.yaclient.modules;

import dev.yaclient.Hud;
import net.minecraft.client.settings.KeyBinding;

/** Premi una volta il tasto per accovacciarti, premi di nuovo per smettere. */
public class ToggleSneakModule extends Module {
    private boolean toggled, was;

    public ToggleSneakModule() { super("Toggle Sneak", false); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.currentScreen != null) return;
        KeyBinding kb = mc.gameSettings.keyBindSneak;
        boolean down = Hud.down(kb);
        if (down && !was) {
            toggled = !toggled;
            if (!toggled) KeyBinding.setKeyBindState(kb.getKeyCode(), false);
        }
        was = down;
        if (toggled) KeyBinding.setKeyBindState(kb.getKeyCode(), true);
    }

    @Override
    public void onToggle() {
        if (!enabled) {
            toggled = false;
            if (mc != null && mc.gameSettings != null) {
                KeyBinding kb = mc.gameSettings.keyBindSneak;
                KeyBinding.setKeyBindState(kb.getKeyCode(), Hud.down(kb));
            }
        }
    }
}
