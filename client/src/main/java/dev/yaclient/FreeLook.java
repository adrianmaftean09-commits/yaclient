package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.weavemc.api.event.EventBus;

import java.util.function.Consumer;

/**
 * Free Look (the FreeLookModule switches it on and off; the key is set in the options of that mod). Hold the key and move the mouse: the
 * camera turns around you while the direction your player faces (and aims) stays frozen. It switches to third person
 * while held and goes back when released.
 *
 * Just before the world is drawn the mouse movement of the frame is moved from the player to the camera angles, the
 * camera angles are put on the player for the draw, and right after the draw the frozen angles are put back.
 */
public final class FreeLook {
    private static boolean hooked;
    private static boolean active;
    private static boolean subscribed;
    private static float camYaw, camPitch, fYaw, fPitch;
    private static int prevView;

    private FreeLook() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void init() {
        try {
            Class c = Class.forName("net.weavemc.api.event.RenderWorldEvent$Pre");
            EventBus.subscribe(c, (Consumer) new Consumer<Object>() { @Override public void accept(Object e) { before(); } });
            Class d = Class.forName("net.weavemc.api.event.RenderWorldEvent$Post");
            EventBus.subscribe(d, (Consumer) new Consumer<Object>() { @Override public void accept(Object e) { after(); } });
            hooked = true;
        } catch (Throwable t) {
            System.out.println("[YA CLIENT] free look: world render events not found");
        }
    }

    /** Every tick: remember the key the player picked, start / stop free look. */
    public static void tick(Minecraft mc) {
        if (!subscribed) { subscribed = true; init(); }
        EntityPlayer p = mc.thePlayer;
        boolean want = hooked && dev.yaclient.modules.FreeLookModule.on() && p != null && !p.isDead && mc.currentScreen == null && dev.yaclient.modules.FreeLookModule.held();
        if (want && !active) {
            active = true;
            camYaw = fYaw = p.rotationYaw;
            camPitch = fPitch = p.rotationPitch;
            prevView = mc.gameSettings.thirdPersonView;
            if (prevView == 0 && dev.yaclient.modules.FreeLookModule.thirdPerson()) mc.gameSettings.thirdPersonView = 1;
        } else if (!want && active) {
            stop(mc);
        }
    }

    private static void stop(Minecraft mc) {
        active = false;
        if (mc.thePlayer != null) { mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = fYaw; mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = fPitch; }
        mc.gameSettings.thirdPersonView = prevView;
    }

    private static void before() {
        if (!active) return;
        EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        if (p == null) return;
        camYaw += p.rotationYaw - fYaw;
        camPitch += p.rotationPitch - fPitch;
        if (camPitch > 90f) camPitch = 90f;
        if (camPitch < -90f) camPitch = -90f;
        p.rotationYaw = p.prevRotationYaw = camYaw;
        p.rotationPitch = p.prevRotationPitch = camPitch;
    }

    private static void after() {
        if (!active) return;
        EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        if (p == null) return;
        p.rotationYaw = p.prevRotationYaw = fYaw;
        p.rotationPitch = p.prevRotationPitch = fPitch;
    }
}
