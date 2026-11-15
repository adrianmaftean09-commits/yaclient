package dev.yaclient.modules;

import java.util.Locale;

public class SpeedModule extends Module {
    private double lastX, lastZ, speed;
    private boolean has;

    public SpeedModule() { super("Velocit\u00e0", false); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) { has = false; return; }
        double x = mc.thePlayer.posX, z = mc.thePlayer.posZ;
        if (has) {
            double d = Math.sqrt((x - lastX) * (x - lastX) + (z - lastZ) * (z - lastZ)) * 20.0;
            speed += (d - speed) * 0.3;
        }
        lastX = x; lastZ = z; has = true;
    }

    @Override
    public void onRender() {
        boxRight(52, String.format(Locale.US, "%.1f b/s", speed));
    }
}
