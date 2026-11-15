package dev.yaclient.modules;

import dev.yaclient.Hud;

public class CoordsModule extends Module {
    public CoordsModule() { super("Coordinate", false); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null) return;
        Hud.left("XYZ " + (int) Math.floor(mc.thePlayer.posX) + " " + (int) Math.floor(mc.thePlayer.posY)
                + " " + (int) Math.floor(mc.thePlayer.posZ));
    }
}
