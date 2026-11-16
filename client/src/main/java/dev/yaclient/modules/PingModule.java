package dev.yaclient.modules;

import net.minecraft.client.network.NetworkPlayerInfo;

public class PingModule extends PillModule {
    public PingModule() { super("ping", Category.INFO, false, 0.99f, 0.012f); }

    @Override
    protected String text() {
        if (mc == null || mc.thePlayer == null || mc.getNetHandler() == null) return "";
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        return info == null ? "-- ms" : info.getResponseTime() + " ms";
    }

    @Override
    protected String sample() { return "42 ms"; }
}
