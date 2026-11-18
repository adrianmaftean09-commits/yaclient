package dev.yaclient.modules;

public class ServerAddressModule extends PillModule {
    public ServerAddressModule() { super("server_ip", Category.INFO, false, 0.008f, 0.28f); }

    @Override
    protected String text() {
        if (mc == null || mc.theWorld == null) return "";
        if (mc.getCurrentServerData() == null) return "Singleplayer";
        if (StreamerModeModule.hideIp()) return "hidden";
        return mc.getCurrentServerData().serverIP;
    }

    @Override
    protected String sample() { return "play.example.net"; }
}
