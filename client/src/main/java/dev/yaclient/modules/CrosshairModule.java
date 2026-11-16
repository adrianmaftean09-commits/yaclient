package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import net.minecraft.client.gui.ScaledResolution;

/** Customizable crosshair drawn around the screen center (ring, dot, square). */
public class CrosshairModule extends Module {
    private final Setting.Choice style = add(new Setting.Choice("style", 0, "ring", "ring_dot", "dot", "square"));
    private final Setting.Num size = add(new Setting.Num("size", 6f, 30f, 1f, 12f, ""));
    private final Setting.Num thickness = add(new Setting.Num("thickness", 1f, 4f, 1f, 1f, ""));
    private final Setting.Choice color = add(new Setting.Choice("color", 0, "accent", "white", "red", "green"));

    public CrosshairModule() { super("crosshair", Category.PVP, false); }

    private int argb() {
        switch (color.current()) {
            case "white": return 0xF2FFFFFF;
            case "red": return 0xF2FF3B30;
            case "green": return 0xF234D27B;
            default: return Theme.accent();
        }
    }

    @Override
    public void onRender() {
        if (mc.gameSettings.thirdPersonView != 0) return;
        ScaledResolution sr = new ScaledResolution(mc);
        Gfx.setScale(sr.getScaleFactor());
        Gfx.fade = 1f;
        int cx = sr.getScaledWidth() / 2, cy = sr.getScaledHeight() / 2;
        int d = Math.round(size.value);
        int t = Math.round(thickness.value);
        int c = argb();
        String st = style.current();
        if (st.equals("ring") || st.equals("ring_dot")) {
            Gfx.rrOutline(cx - d / 2, cy - d / 2, d, d, d / 2, t, c);
        }
        if (st.equals("dot") || st.equals("ring_dot")) {
            int dot = Math.max(2, t + 1);
            Gfx.rr(cx - dot / 2, cy - dot / 2, dot, dot, dot / 2, c);
        }
        if (st.equals("square")) {
            Gfx.rrOutline(cx - d / 2, cy - d / 2, d, d, 1, t, c);
        }
    }
}
