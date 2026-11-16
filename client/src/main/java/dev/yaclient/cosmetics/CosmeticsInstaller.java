package dev.yaclient.cosmetics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Adds CosmeticLayer to the player renderers without Mixin: the renderer keeps its layers in a
 * List field, which is found by type (so it works whatever the runtime field names are).
 */
public final class CosmeticsInstaller {
    private static boolean done, failed;

    private CosmeticsInstaller() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void tryInstall() {
        if (done || failed) return;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getRenderManager() == null) return;
            Map<String, RenderPlayer> skins = mc.getRenderManager().getSkinMap();
            if (skins == null || skins.isEmpty()) return;

            Field layers = null;
            for (Field f : RendererLivingEntity.class.getDeclaredFields()) {
                if (List.class.isAssignableFrom(f.getType())) { layers = f; break; }
            }
            if (layers == null) { failed = true; return; }
            layers.setAccessible(true);
            for (RenderPlayer rp : skins.values()) {
                ((List) layers.get(rp)).add(new CosmeticLayer(rp));
            }
            done = true;
        } catch (Throwable t) {
            failed = true;
            System.out.println("[YA CLIENT] cosmetics layer not installed: " + t);
        }
    }
}
