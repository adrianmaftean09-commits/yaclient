package dev.yaclient.modules;

import dev.yaclient.Hud;
import net.minecraft.client.Minecraft;

public class FpsModule extends Module {
    public FpsModule() { super("FPS", true); }

    @Override
    public void onRender() {
        Hud.left(Minecraft.getDebugFPS() + " FPS");
    }
}
