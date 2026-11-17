package dev.yaclient.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

/** Detects the hits you land (for Combo, Hit Color and Target HUD) and the damage you take. Updated every tick. */
public final class HitTracker {
    private HitTracker() {}

    private static boolean wasDown;
    private static EntityLivingBase clickTarget;
    private static int tick, clickTick;
    private static long lastHit;
    private static int combo;
    private static EntityLivingBase target;
    private static boolean wasHurt;

    public static void tick(Minecraft mc) {
        tick++;
        if (mc.thePlayer == null || mc.theWorld == null) { combo = 0; target = null; clickTarget = null; return; }
        boolean down = Mouse.isButtonDown(0) && mc.currentScreen == null;
        if (down && !wasDown) {
            MovingObjectPosition m = mc.objectMouseOver;
            if (m != null && m.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && m.entityHit instanceof EntityLivingBase) {
                clickTarget = (EntityLivingBase) m.entityHit;
                clickTick = tick;
                target = clickTarget;
                lastTargetTime = System.currentTimeMillis();
            } else {
                clickTarget = null;
            }
        }
        wasDown = down;
        if (clickTarget != null) {
            if (tick - clickTick > 12 || clickTarget.isDead) {
                clickTarget = null;
            } else if (clickTarget.hurtTime > 0 && clickTarget.hurtTime >= clickTarget.maxHurtTime - 1) {
                combo++;
                lastHit = System.currentTimeMillis();
                clickTarget = null;
            }
        }
        boolean hurt = mc.thePlayer.hurtTime > 0 && mc.thePlayer.hurtTime >= mc.thePlayer.maxHurtTime - 1;
        if (hurt && !wasHurt) combo = 0;
        wasHurt = hurt;
        if (System.currentTimeMillis() - lastHit > 3000) combo = 0;
    }

    private static long lastTargetTime;

    public static int combo() { return combo; }

    public static long lastHitTime() { return lastHit; }

    /** The enemy you attacked or aimed at most recently (null after a few seconds). */
    public static EntityLivingBase target() {
        if (target != null && (target.isDead || System.currentTimeMillis() - lastTargetTime > 4000)) target = null;
        return target;
    }

    /** Refresh the "recently aimed at" timer while the crosshair is on a living entity. */
    public static void aimed(EntityLivingBase e) { target = e; lastTargetTime = System.currentTimeMillis(); }
}
