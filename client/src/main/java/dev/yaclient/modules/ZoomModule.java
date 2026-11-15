package dev.yaclient.modules;

import org.lwjgl.input.Keyboard;

/** Zoom tenendo premuto C. */
public class ZoomModule extends Module {
    private float saved = -1;
    private boolean zooming;

    public ZoomModule() { super("Zoom (C)", true); }

    @Override
    public void onTick() {
        boolean down = mc.currentScreen == null && Keyboard.isKeyDown(Keyboard.KEY_C);
        if (down && !zooming) {
            saved = mc.gameSettings.fovSetting;
            mc.gameSettings.fovSetting = 20f;
            mc.gameSettings.smoothCamera = true;
            zooming = true;
        } else if (!down && zooming) {
            mc.gameSettings.fovSetting = saved;
            mc.gameSettings.smoothCamera = false;
            zooming = false;
        }
    }

    @Override
    public void onToggle() {
        if (!enabled && zooming) {
            mc.gameSettings.fovSetting = saved;
            mc.gameSettings.smoothCamera = false;
            zooming = false;
        }
    }
}
