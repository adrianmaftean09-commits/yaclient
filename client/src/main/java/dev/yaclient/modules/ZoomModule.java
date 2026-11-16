package dev.yaclient.modules;

import dev.yaclient.Setting;
import org.lwjgl.input.Keyboard;

/** Hold C to zoom. */
public class ZoomModule extends Module {
    private final Setting.Num fov = add(new Setting.Num("fov", 10f, 60f, 1f, 20f, ""));
    private final Setting.Bool smooth = add(new Setting.Bool("smooth", true));
    private float saved = -1;
    private boolean zooming;

    public ZoomModule() { super("zoom", Category.UTILITY, true); }

    @Override
    public void onTick() {
        boolean down = mc.currentScreen == null && Keyboard.isKeyDown(Keyboard.KEY_C);
        if (down && !zooming) {
            saved = mc.gameSettings.fovSetting;
            mc.gameSettings.smoothCamera = smooth.value;
            zooming = true;
        } else if (!down && zooming) {
            restore();
        }
        if (zooming) mc.gameSettings.fovSetting = fov.value;
    }

    private void restore() {
        mc.gameSettings.fovSetting = saved;
        mc.gameSettings.smoothCamera = false;
        zooming = false;
    }

    @Override
    public void onToggle() {
        if (!enabled && zooming && mc != null) restore();
    }
}
