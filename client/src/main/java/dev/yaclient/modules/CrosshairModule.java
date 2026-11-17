package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import net.minecraft.client.gui.ScaledResolution;

/**
 * Custom crosshair. The player types the shape or the text: a known word (cross, plus, dot, ring,
 * square, diamond) draws that shape, anything else is drawn as text. While the module is enabled the
 * vanilla crosshair is not drawn (see YaIngame).
 */
public class CrosshairModule extends Module {
    public static CrosshairModule instance;

    private final Setting.Text shape = add("shape", new Setting.Text("shape", "cross", 12));
    private final Setting.Palette color = add("shape", new Setting.Palette("color", 0));
    private final Setting.Num size = add("shape", new Setting.Num("size", 4f, 40f, 1f, 10f, ""));
    private final Setting.Num thickness = add("shape", new Setting.Num("thickness", 1f, 6f, 1f, 2f, ""));
    private final Setting.Num gap = add("shape", new Setting.Num("gap", 0f, 20f, 1f, 3f, ""));
    private final Setting.Bool outline = add("style", new Setting.Bool("outline", true));
    private final Setting.Bool dynamic = add("style", new Setting.Bool("dynamic", false));
    private final Setting.Num opacity = add("style", new Setting.Num("opacity", 20f, 100f, 5f, 100f, "%"));

    public CrosshairModule() { super("crosshair", Category.PVP, false); instance = this; }

    /** True while the vanilla crosshair must be hidden. */
    public static boolean hidesVanilla() { return instance != null && instance.enabled; }

    /** Text shown in the options preview. */
    public String shapeText() { return shape.value; }

    private static String key(String s) {
        s = s.trim().toLowerCase(java.util.Locale.ROOT);
        switch (s) {
            case "croce": case "cross": case "mirino": return "cross";
            case "plus": case "piu": case "più": return "plus";
            case "dot": case "punto": case "point": return "dot";
            case "ring": case "cerchio": case "circle": case "anello": return "ring";
            case "square": case "quadrato": case "box": return "square";
            case "diamond": case "rombo": return "diamond";
            default: return null;
        }
    }

    private int argb() {
        int a = Math.round(2.55f * opacity.value);
        return (a << 24) | (Setting.Palette.color(color.index) & 0xFFFFFF);
    }

    @Override
    public void onRender() {
        if (mc.gameSettings.thirdPersonView != 0 || mc.currentScreen != null) return;
        ScaledResolution sr = new ScaledResolution(mc);
        Gfx.setScale(sr.getScaleFactor());
        Gfx.fade = 1f;
        drawAt(sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, 1f, mc.thePlayer != null && dynamic.value && (mc.thePlayer.isSprinting() || mc.thePlayer.motionY != 0 && !mc.thePlayer.onGround));
    }

    /** Draws the crosshair centered on (cx, cy); used by the game and by the options preview. */
    public void drawAt(int cx, int cy, float zoom, boolean spread) {
        int d = Math.round(size.value * zoom), t = Math.max(1, Math.round(thickness.value * zoom));
        int g = Math.round((gap.value + (spread ? 3 : 0)) * zoom);
        int c = argb();
        String k = key(shape.value);
        if (k == null) {
            String s = shape.value.isEmpty() ? "+" : shape.value;
            int px = Math.max(8, Math.round(size.value * 1.2f * zoom));
            int w = Gfx.width(s, px, true), h = Gfx.lineHeight(px, true);
            Gfx.textCentered(s, cx, cy - h / 2, px, true, c);
            return;
        }
        boolean ol = outline.value;
        int o = 0xE0000000 & (c & 0xFF000000 | 0xFF000000);
        o = (c & 0xFF000000) | 0x000000;
        switch (k) {
            case "cross": {
                bar(cx - g - d, cy - t / 2, d, t, c, o, ol);
                bar(cx + g, cy - t / 2, d, t, c, o, ol);
                bar(cx - t / 2, cy - g - d, t, d, c, o, ol);
                bar(cx - t / 2, cy + g, t, d, c, o, ol);
                break;
            }
            case "plus": {
                bar(cx - d, cy - t / 2, 2 * d + 1, t, c, o, ol);
                bar(cx - t / 2, cy - d, t, 2 * d + 1, c, o, ol);
                break;
            }
            case "dot": {
                int r = Math.max(2, t + 1);
                if (ol) Gfx.rr(cx - r / 2 - 1, cy - r / 2 - 1, r + 2, r + 2, (r + 2) / 2, o);
                Gfx.rr(cx - r / 2, cy - r / 2, r, r, r / 2, c);
                break;
            }
            case "ring": {
                int dd = d * 2;
                if (ol) Gfx.rrOutline(cx - dd / 2 - 1, cy - dd / 2 - 1, dd + 2, dd + 2, dd / 2 + 1, t + 2, o);
                Gfx.rrOutline(cx - dd / 2, cy - dd / 2, dd, dd, dd / 2, t, c);
                break;
            }
            case "square": {
                int dd = d * 2;
                if (ol) Gfx.rrOutline(cx - dd / 2 - 1, cy - dd / 2 - 1, dd + 2, dd + 2, 1, t + 2, o);
                Gfx.rrOutline(cx - dd / 2, cy - dd / 2, dd, dd, 1, t, c);
                break;
            }
            default: { // diamond: stacked bars
                for (int i = -d; i <= d; i++) {
                    int w = (d - Math.abs(i)) * 2 + 1;
                    int tw = Math.max(1, t);
                    Gfx.rr(cx - w / 2, cy + i, Math.min(w, tw), 1, 0, c);
                    if (w > tw) {
                        Gfx.rr(cx - w / 2, cy + i, tw, 1, 0, c);
                        Gfx.rr(cx + w / 2 - tw + 1, cy + i, tw, 1, 0, c);
                    }
                }
            }
        }
    }

    private static void bar(int x, int y, int w, int h, int c, int o, boolean ol) {
        if (ol) Gfx.rr(x - 1, y - 1, w + 2, h + 2, 0, o);
        Gfx.rr(x, y, w, h, 0, c);
    }
}
