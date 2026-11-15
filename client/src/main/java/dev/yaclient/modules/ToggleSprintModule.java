package dev.yaclient.modules;

import net.minecraft.client.settings.KeyBinding;

/** Corsa automatica: tiene premuto sprint mentre vai avanti. */
public class ToggleSprintModule extends Module {
    public ToggleSprintModule() { super("Toggle Sprint", true); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.currentScreen != null) return;
        if (mc.gameSettings.keyBindForward.isKeyDown() && !mc.thePlayer.isSneaking()
                && !mc.thePlayer.isUsingItem() && mc.thePlayer.getFoodStats().getFoodLevel() > 6) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }
}
