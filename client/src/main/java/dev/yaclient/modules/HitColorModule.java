package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import net.minecraft.client.gui.ScaledResolution;

/** A colored ring that expands from your crosshair each time you hit someone. */
public class HitColorModule extends Module {
    private final Setting.Choice color = add(new Setting.Choice("color", 0, "red", "white", "green", "yellow", "accent"));
    private final Setting.Num size = add(new Setting.Num("size", 10f, 40f, 2f, 24f, ""));

    public HitColorModule() { super("hit_color", Category.PVP, false); }

    private int rgb() {
        switch (color.current()) {
            case "white": return 0xFFFFFF;
            case "green": return 0x34D27B;
            case "yellow": return 0xFFD60A;
            case "accent": return Theme.accent() & 0xFFFFFF;
            default: return 0xFF3B30;
        }
    }

    @Override
    public void onRender() {
        long age = System.currentTimeMillis() - HitTracker.lastHitTime();
        if (age < 0 || age > 380 || mc.gameSettings.thirdPersonView != 0) return;
        float p = age / 380f;
        ScaledResolution sr = new ScaledResolution(mc);
        Gfx.setScale(sr.getScaleFactor());
        Gfx.fade = 1f;
        int d = Math.round(6f + (size.value - 6f) * (float) Math.sqrt(p));
        int a = Math.round(255f * (1f - p));
        int cx = sr.getScaledWidth() / 2, cy = sr.getScaledHeight() / 2;
        Gfx.rrOutline(cx - d / 2, cy - d / 2, d, d, d / 2, 2, (a << 24) | rgb());
    }
}
