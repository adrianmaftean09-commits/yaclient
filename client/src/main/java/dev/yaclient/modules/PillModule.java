package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;

/** HUD element that shows one line of text in a rounded pill. */
public abstract class PillModule extends HudModule {
    protected static final int H = 16;
    protected final Setting.Bool background = add(new Setting.Bool("bg", true));

    protected PillModule(String id, Category category, boolean enabledByDefault, float defX, float defY) {
        super(id, category, enabledByDefault, defX, defY);
    }

    /** Current text. Return an empty string to hide the element. */
    protected abstract String text();

    /** Text shown in the HUD editor when text() is empty. */
    protected String sample() { return name(); }

    private String shown() {
        String t = text();
        if ((t == null || t.isEmpty()) && preview) t = sample();
        return t == null ? "" : t;
    }

    @Override
    public int width() {
        String t = shown();
        return t.isEmpty() ? 0 : Gfx.width(t, 10, false) + 16;
    }

    @Override
    public int height() { return H; }

    @Override
    protected void draw() {
        String t = shown();
        if (t.isEmpty()) return;
        int w = Gfx.width(t, 10, false) + 16;
        if (background.value) {
            Gfx.rr(0, 0, w, H, 6, Theme.HUD_BG);
            Gfx.rrOutline(0, 0, w, H, 6, 1, 0x1AFFFFFF);
        }
        Gfx.text(t, 8, (H - Gfx.lineHeight(10, false)) / 2, 10, false, Theme.TEXT);
    }
}
