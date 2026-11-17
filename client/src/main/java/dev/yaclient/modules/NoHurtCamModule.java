package dev.yaclient.modules;

/** Removes the camera shake when you take damage. */
public class NoHurtCamModule extends Module {
    public NoHurtCamModule() { super("no_hurt_cam", Category.PVP, false); }

    @Override
    public void onTick() {
        if (mc.thePlayer != null) mc.thePlayer.hurtTime = 0;
    }
}
