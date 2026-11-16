package dev.yaclient.modules;

import net.minecraft.util.ResourceLocation;

/** Motion blur using Minecraft's built-in "phosphor" post-processing shader. */
public class MotionBlurModule extends Module {
    private boolean applied;

    public MotionBlurModule() { super("motion_blur", Category.UTILITY, false); }

    @Override
    public void onTick() {
        if (mc.theWorld == null || mc.entityRenderer == null) return;
        if (!mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/phosphor.json"));
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
