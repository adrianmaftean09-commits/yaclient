package dev.yaclient.modules;

import net.minecraft.client.Minecraft;

public class FpsModule extends PillModule {
    public FpsModule() { super("fps", Category.INFO, true, 0.008f, 0.012f); }

    @Override
    protected String text() { return Minecraft.getDebugFPS() + " FPS"; }
}
