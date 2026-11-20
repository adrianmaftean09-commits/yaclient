package dev.yaclient.modules;

import net.minecraft.entity.Entity;

/**
 * Removes the camera shake when you take damage. The shake is proportional to hurtTime / maxHurtTime,
 * so a huge maxHurtTime makes it vanish while hurtTime keeps counting normally (no lag, no side effects).
 */
public class NoHurtCamModule extends Module {
    private static NoHurtCamModule inst;
    private static int real = 10;

    public NoHurtCamModule() { super("no_hurt_cam", Category.PVP, false); inst = this; }

    /** The real max hurt time of an entity (what the game set before this module raised it). */
    public static int realMax(Entity e) {
        if (inst != null && inst.enabled && mcPlayer(e)) return real;
        return e instanceof net.minecraft.entity.EntityLivingBase ? ((net.minecraft.entity.EntityLivingBase) e).maxHurtTime : 10;
    }

    private static boolean mcPlayer(Entity e) {
        net.minecraft.client.Minecraft m = net.minecraft.client.Minecraft.getMinecraft();
        return m != null && e == m.thePlayer;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        int m = mc.thePlayer.maxHurtTime;
        if (m > 0 && m < 100) real = m;
        mc.thePlayer.maxHurtTime = 10000;
    }

    @Override
    public void onToggle() {
        if (!enabled && mc != null && mc.thePlayer != null && mc.thePlayer.maxHurtTime > 100) mc.thePlayer.maxHurtTime = real;
    }
}
