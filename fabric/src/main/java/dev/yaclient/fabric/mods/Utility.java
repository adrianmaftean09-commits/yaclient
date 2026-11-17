package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Module;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Modules without a HUD box. */
public final class Utility {
    private Utility() {}

    public static class Fullbright extends Module {
        public Fullbright() { super("fullbright", "Fullbright", "See in the dark (client side night vision)", Category.UTILITY, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            if (mc.player != null) mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
        }

        @Override
        public void onToggle() {
            Minecraft mc = mc();
            if (!enabled && mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    public static class NoClouds extends Module {
        public NoClouds() { super("noclouds", "No Clouds", "Removes the clouds from the sky", Category.UTILITY, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            if (mc.options.cloudStatus().get() != CloudStatus.OFF) mc.options.cloudStatus().set(CloudStatus.OFF);
        }
    }

    public static class ToggleSprint extends Module {
        public ToggleSprint() { super("togglesprint", "Toggle Sprint", "Sprints automatically while you walk forward", Category.UTILITY, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            if (mc.player == null || mc.screen != null) return;
            if (mc.options.keyUp.isDown() && !mc.player.isShiftKeyDown() && !mc.player.isUsingItem()) mc.player.setSprinting(true);
        }
    }

    /** Hold the key to zoom in (FOV 30), release to restore. */
    public static class Zoom extends Module {
        private int saved = -1;

        public Zoom() { super("zoom", "Zoom", "Hold C to zoom in", Category.PVP, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            boolean down = mc.screen == null && org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().handle(), org.lwjgl.glfw.GLFW.GLFW_KEY_C) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            if (down && saved < 0) { saved = mc.options.fov().get(); mc.options.fov().set(30); }
            else if (!down && saved >= 0) { mc.options.fov().set(saved); saved = -1; }
        }

        @Override
        public void onToggle() {
            Minecraft mc = mc();
            if (!enabled && saved >= 0) { mc.options.fov().set(saved); saved = -1; }
        }
    }
}
