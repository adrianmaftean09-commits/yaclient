package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.weavemc.api.event.EventBus;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.util.function.Consumer;

/**
 * Free Look (the FreeLookModule switches it on and off). Module "Free Look". Hold the key (set it in Options > Controls > "YA Client") and move the mouse: the
 * camera turns around you while the direction your player faces (and aims) stays frozen. It switches to third person
 * while held and goes back when released.
 *
 * Just before the world is drawn the mouse movement of the frame is moved from the player to the camera angles, the
 * camera angles are put on the player for the draw, and right after the draw the frozen angles are put back.
 */
public final class FreeLook {
    private static KeyBinding key;
    private static int savedCode = Keyboard.KEY_V;
    private static boolean hooked;
    private static boolean active;
    private static float camYaw, camPitch, fYaw, fPitch;
    private static int prevView;

    private FreeLook() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init() {
        Minecraft mc = Minecraft.getMinecraft();
        try {
            savedCode = Config.getInt("freelook.key", Keyboard.KEY_V);
            key = new KeyBinding("Free Look", savedCode, "YA Client");
            GameSettings gs = mc.gameSettings;
            // the array that holds every control (not the 9-slot hotbar array, which is also a KeyBinding[]): it contains the sneak key
            Field f = null;
            for (Field c : GameSettings.class.getDeclaredFields()) {
                if (c.getType() != KeyBinding[].class || java.lang.reflect.Modifier.isStatic(c.getModifiers())) continue;
                c.setAccessible(true);
                KeyBinding[] arr = (KeyBinding[]) c.get(gs);
                if (arr != null && arr.length > 20 && java.util.Arrays.asList(arr).contains(gs.keyBindSneak)) { f = c; break; }
            }
            if (f == null) throw new IllegalStateException("controls array not found");
            f.setAccessible(true);
            KeyBinding[] old = (KeyBinding[]) f.get(gs);
            KeyBinding[] n = java.util.Arrays.copyOf(old, old.length + 1);
            n[old.length] = key;
            f.set(gs, n);
            KeyBinding.resetKeyBindingArrayAndHash();
        } catch (Throwable t) {
            System.out.println("[YA CLIENT] free look key: " + t);
            key = null;
        }
        String[] pre = {"net.weavemc.api.event.RenderWorldEvent$Pre"};
        String[] post = {"net.weavemc.api.event.RenderWorldEvent$Post"};
        try {
            Class c = Class.forName(pre[0]);
            EventBus.subscribe(c, (Consumer) new Consumer<Object>() { @Override public void accept(Object e) { before(); } });
            Class d = Class.forName(post[0]);
            EventBus.subscribe(d, (Consumer) new Consumer<Object>() { @Override public void accept(Object e) { after(); } });
            hooked = true;
        } catch (Throwable t) {
            System.out.println("[YA CLIENT] free look: world render events not found");
        }
    }

    private static boolean held() {
        if (key == null) return false;
        int c = key.getKeyCode();
        try { return c < 0 ? Mouse.isButtonDown(c + 100) : Keyboard.isKeyDown(c); } catch (Throwable t) { return false; }
    }

    /** Every tick: remember the key the player picked, start / stop free look. */
    private static boolean tried;

    public static void tick(Minecraft mc) {
        if (!tried && mc.gameSettings != null) { tried = true; init(); }
        if (key == null) return;
        if (key.getKeyCode() != savedCode) {
            savedCode = key.getKeyCode();
            Config.setInt("freelook.key", savedCode);
            Config.save();
        }
        EntityPlayer p = mc.thePlayer;
        boolean want = hooked && dev.yaclient.modules.FreeLookModule.on() && p != null && !p.isDead && mc.currentScreen == null && held();
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
