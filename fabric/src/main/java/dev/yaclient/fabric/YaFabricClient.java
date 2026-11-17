package dev.yaclient.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/** YA CLIENT for the latest Minecraft (Fabric). */
public class YaFabricClient implements ClientModInitializer {
    public static final String NAME = "YA CLIENT";
    public static final String VERSION = "1.0.0";

    @Override
    public void onInitializeClient() {
        Config.load();
        // new installs: only FPS, CPS and Keystrokes are on, everything else starts off
        if (!Config.hasPrefix("mod.")) {
            for (String id : new String[]{"fps", "cps", "keystrokes"}) Config.setBool("mod." + id + ".enabled", true);
            Config.save();
        }
        Modules.init();
        Hud.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> Modules.tick());
        System.out.println("[" + NAME + "] " + VERSION + " loaded (Fabric), " + Modules.ALL.size() + " modules.");
    }
}
