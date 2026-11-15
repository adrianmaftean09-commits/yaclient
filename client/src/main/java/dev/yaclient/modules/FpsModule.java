package dev.yaclient.modules;

import dev.yaclient.YAClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class FpsModule extends Module {
    public FpsModule() { super("FPS", true); }

    @Override
    public void onRender() {
        String t = Minecraft.getDebugFPS() + " FPS";
        int w = mc.fontRendererObj.getStringWidth(t);
        Gui.drawRect(4, 4, 10 + w, 16, YAClient.BLACK_BG);
        Gui.drawRect(4, 4, 6, 16, YAClient.RED);
        mc.fontRendererObj.drawStringWithShadow(t, 8, 6, YAClient.WHITE);
    }
}
