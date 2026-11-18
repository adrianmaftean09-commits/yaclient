package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.Module;
import dev.yaclient.fabric.Setting;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.server.level.ParticleStatus;
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
            if (mc.player == null || !mc.mouseHandler.isMouseGrabbed()) return;
            if (mc.options.keyUp.isDown() && !mc.player.isShiftKeyDown() && !mc.player.isUsingItem()) mc.player.setSprinting(true);
        }
    }

    /** Hold the key to zoom in (FOV 30), release to restore. */
    public static class Zoom extends Module {
        private int saved = -1;
        private final Setting.Num fov = add("general", new Setting.Num("zoom_fov", 10f, 60f, 1f, 30f, ""));

        public Zoom() { super("zoom", "Zoom", "Hold C to zoom in", Category.PVP, false); }

        @Override
        public void onTick() {
            Minecraft mc = mc();
            boolean down = mc.mouseHandler.isMouseGrabbed() && org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().handle(), org.lwjgl.glfw.GLFW.GLFW_KEY_C) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            if (down && saved < 0) { saved = mc.options.fov().get(); mc.options.fov().set(Math.round(fov.value)); }
            else if (!down && saved >= 0) { mc.options.fov().set(saved); saved = -1; }
        }

        @Override
        public void onToggle() {
            Minecraft mc = mc();
            if (!enabled && saved >= 0) { mc.options.fov().set(saved); saved = -1; }
        }
    }

    /** Lowers the heaviest video options while on and restores them when switched off. */
    public static class FpsBoost extends Module {
        private final java.util.Map<String, Object> saved = new java.util.HashMap<String, Object>();
        private int t;
        public FpsBoost() { super("fpsboost", "FPS Boost", "Fewer particles, no clouds, no shadows and less entity detail for more FPS", Category.UTILITY, false); }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void put(String key, OptionInstance opt, Object value) {
            if (!saved.containsKey(key)) saved.put(key, opt.get());
            if (!opt.get().equals(value)) opt.set(value);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void restore(String key, OptionInstance opt) {
            if (saved.containsKey(key)) opt.set(saved.get(key));
        }

        private void apply() {
            Minecraft mc = mc();
            put("particles", mc.options.particles(), ParticleStatus.MINIMAL);
            put("clouds", mc.options.cloudStatus(), CloudStatus.OFF);
            put("shadows", mc.options.entityShadows(), Boolean.FALSE);
            put("entdist", mc.options.entityDistanceScaling(), Double.valueOf(0.5));
            put("biome", mc.options.biomeBlendRadius(), Integer.valueOf(0));
            put("bob", mc.options.bobView(), Boolean.FALSE);
        }

        @Override public void onTick() { if (t++ % 100 == 0) apply(); }

        @Override
        public void onToggle() {
            Minecraft mc = mc();
            if (enabled) { t = 0; apply(); return; }
            restore("particles", mc.options.particles());
            restore("clouds", mc.options.cloudStatus());
            restore("shadows", mc.options.entityShadows());
            restore("entdist", mc.options.entityDistanceScaling());
            restore("biome", mc.options.biomeBlendRadius());
            restore("bob", mc.options.bobView());
            saved.clear();
        }
    }
}
