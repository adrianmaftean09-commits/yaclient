package dev.yaclient.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

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
        Hits.init();
        dev.yaclient.fabric.cos.CosmeticsLayer.register();
        KeyMapping menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.yaclient.menu", GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("yaclient", "main"))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) CosmeticsApi.refreshAsync(false);
            Modules.tick();
            TabBadge.tick(client);
            while (menuKey.consumeClick()) client.setScreenAndShow(new MenuScreen());
        });
        System.out.println("[" + NAME + "] " + VERSION + " loaded (Fabric), " + Modules.ALL.size() + " modules.");
    }
}
