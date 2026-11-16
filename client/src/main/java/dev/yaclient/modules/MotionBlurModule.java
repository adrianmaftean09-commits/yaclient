package dev.yaclient.modules;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Method;

/** Motion blur using Minecraft's built-in "phosphor" post-processing shader. */
public class MotionBlurModule extends Module {
    private static Method loader;
    private static boolean failed;
    private boolean applied;

    public MotionBlurModule() { super("motion_blur", Category.UTILITY, false); }

    /**
     * EntityRenderer.loadShader(ResourceLocation) is private. The method is found by its signature
     * (void, one ResourceLocation parameter) so this works whatever the runtime method names are.
     */
    private static void loadShader(EntityRenderer renderer, ResourceLocation location) {
        if (failed) return;
        try {
            if (loader == null) {
                for (Method m : EntityRenderer.class.getDeclaredMethods()) {
                    Class<?>[] p = m.getParameterTypes();
                    if (p.length == 1 && p[0] == ResourceLocation.class && m.getReturnType() == void.class) {
                        m.setAccessible(true);
                        loader = m;
                        break;
                    }
                }
            }
            if (loader == null) { failed = true; return; }
            loader.invoke(renderer, location);
        } catch (Exception e) {
            failed = true;
        }
    }

    @Override
    public void onTick() {
        if (mc.theWorld == null || mc.entityRenderer == null) return;
        if (!mc.entityRenderer.isShaderActive()) {
            loadShader(mc.entityRenderer, new ResourceLocation("shaders/post/phosphor.json"));
            applied = true;
        }
    }

    @Override
    public void onToggle() {
        if (!enabled && applied && mc != null && mc.entityRenderer != null) {
            mc.entityRenderer.stopUseShader();
            applied = false;
        }
    }
}
