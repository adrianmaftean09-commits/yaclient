package dev.yaclient.modules;

import java.util.Locale;

public class SpeedModule extends PillModule {
    private double lastX, lastZ, speed;
    private boolean has;

    public SpeedModule() { super("speed", Category.INFO, false, 0.008f, 0.168f); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) { has = false; return; }
        double x = mc.thePlayer.posX, z = mc.thePlayer.posZ;
        if (has) {
            double d = Math.sqrt((x - lastX) * (x - lastX) + (z - lastZ) * (z - lastZ)) * 20.0;
            speed += (d - speed) * 0.3;
        }
        lastX = x;
        lastZ = z;
        has = true;
    }

    @Override
    protected String text() { return String.format(Locale.ROOT, "%.1f b/s", speed); }
}
