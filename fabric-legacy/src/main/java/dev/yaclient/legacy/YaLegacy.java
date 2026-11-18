package dev.yaclient.legacy;

import net.fabricmc.api.ClientModInitializer;

public class YaLegacy implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[YA CLIENT] loaded");
    }
}
