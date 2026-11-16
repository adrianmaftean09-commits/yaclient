package dev.yaclient.modules;

/** Shows entity hitboxes (same as F3 + B). */
public class HitboxesModule extends Module {
    private boolean active;

    public HitboxesModule() { super("hitboxes", Category.PVP, false); }

    @Override
    public void onTick() {
        mc.getRenderManager().setDebugBoundingBox(true);
        active = true;
    }

    @Override
    public void onAlwaysTick() {
        if (!enabled && active && mc != null && mc.getRenderManager() != null) {
            mc.getRenderManager().setDebugBoundingBox(false);
            active = false;
        }
    }
}
