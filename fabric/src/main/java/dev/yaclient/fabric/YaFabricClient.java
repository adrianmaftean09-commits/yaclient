package dev.yaclient.fabric;

import net.fabricmc.api.ClientModInitializer;

/** YA CLIENT for the latest Minecraft (Fabric). Entry point: the module system is built on top of this. */
public class YaFabricClient implements ClientModInitializer {
    public static final String NAME = "YA CLIENT";
    public static final String VERSION = "1.0.0";

    @Override
    public void onInitializeClient() {
        System.out.println("[" + NAME + "] " + VERSION + " loaded (Fabric).");
    }
}
