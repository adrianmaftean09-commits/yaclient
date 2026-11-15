package dev.yaclient.modules;

import dev.yaclient.Hud;

public class DirectionModule extends Module {
    private static final String[] DIRS = {"S", "SO", "O", "NO", "N", "NE", "E", "SE"};

    public DirectionModule() { super("Direzione", false); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null) return;
        float yaw = ((mc.thePlayer.rotationYaw % 360f) + 360f) % 360f;
        int idx = ((int) ((yaw + 22.5f) / 45f)) % 8;
        Hud.right(DIRS[idx] + " " + (int) yaw + "\u00b0");
    }
}
