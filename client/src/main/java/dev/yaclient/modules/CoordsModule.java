package dev.yaclient.modules;

import dev.yaclient.Setting;

import java.util.Locale;

public class CoordsModule extends PillModule {
    private final Setting.Bool decimals = add(new Setting.Bool("decimals", false));

    public CoordsModule() { super("coords", Category.INFO, false, 0.008f, 0.116f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null) return "";
        if (decimals.value) {
            return String.format(Locale.ROOT, "XYZ %.1f %.1f %.1f", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        }
        return "XYZ " + (int) Math.floor(mc.thePlayer.posX) + " " + (int) Math.floor(mc.thePlayer.posY)
                + " " + (int) Math.floor(mc.thePlayer.posZ);
    }

    @Override
    protected String sample() { return "XYZ 128 64 -256"; }
}
