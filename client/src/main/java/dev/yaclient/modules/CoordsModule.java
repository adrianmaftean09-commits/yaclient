package dev.yaclient.modules;

import dev.yaclient.YAClient;
import net.minecraft.client.gui.Gui;

public class CoordsModule extends Module {
    public CoordsModule() { super("Coordinate", false); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null) return;
        String t = String.format("XYZ %d %d %d", (int) mc.thePlayer.posX, (int) mc.thePlayer.posY, (int) mc.thePlayer.posZ);
        int w = mc.fontRendererObj.getStringWidth(t);
        Gui.drawRect(4, 36, 10 + w, 48, YAClient.BLACK_BG);
        Gui.drawRect(4, 36, 6, 48, YAClient.RED);
        mc.fontRendererObj.drawStringWithShadow(t, 8, 38, YAClient.WHITE);
    }
}
