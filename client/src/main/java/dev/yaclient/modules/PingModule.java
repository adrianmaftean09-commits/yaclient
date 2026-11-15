package dev.yaclient.modules;

import net.minecraft.client.network.NetworkPlayerInfo;

public class PingModule extends Module {
    public PingModule() { super("Ping", false); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null || mc.getNetHandler() == null) return;
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        boxRight(4, info == null ? "Ping --" : info.getResponseTime() + " ms");
    }
}
