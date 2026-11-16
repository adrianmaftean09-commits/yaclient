package dev.yaclient.modules;

import dev.yaclient.Config;
import dev.yaclient.Gfx;
import dev.yaclient.Hud;
import dev.yaclient.Ui;
import net.minecraft.client.renderer.GlStateManager;

/**
 * A module with a movable, resizable HUD element. Position is stored as a fraction of the
 * screen (so it survives resolution changes); draw() always paints at the origin (0, 0).
 */
public abstract class HudModule extends Module {
    public static final float MIN_SCALE = 0.4f, MAX_SCALE = 3f;

    public float px, py;
    public float scale = 0.8f;
    private final float defX, defY;
    /** True while the HUD editor is open (modules show sample content when they have none). */
    protected boolean preview;

    protected HudModule(String id, Category category, boolean enabledByDefault, float defX, float defY) {
        super(id, category, enabledByDefault);
        this.defX = defX;
        this.defY = defY;
        this.px = defX;
        this.py = defY;
    }

    /** Width of the element in element units (scale 1). */
    public abstract int width();

    /** Height of the element in element units (scale 1). */
    public abstract int height();

    /** Paints the element with its top-left corner at (0, 0). */
    protected abstract void draw();

    @Override
    public void loadConfig() {
        super.loadConfig();
        px = Config.getFloat("hud." + id + ".x", defX);
        py = Config.getFloat("hud." + id + ".y", defY);
        scale = Ui.clamp(Config.getFloat("hud." + id + ".s", 0.8f), MIN_SCALE, MAX_SCALE);
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
        Config.setFloat("hud." + id + ".x", px);
        Config.setFloat("hud." + id + ".y", py);
        Config.setFloat("hud." + id + ".s", scale);
    }

    public void resetLayout() {
        px = defX;
        py = defY;
        scale = 0.8f;
    }

    /** Effective scale including the global HUD scale. */
    public float screenScale() { return scale * Hud.globalScale(); }

    /** Top-left corner on screen in GUI units, kept fully inside the screen. */
    public float screenX(int sw) { return Ui.clamp(px * sw, 0f, Math.max(0f, sw - width() * screenScale())); }

    public float screenY(int sh) { return Ui.clamp(py * sh, 0f, Math.max(0f, sh - height() * screenScale())); }

    public final void renderHud(boolean editing, int sw, int sh, int guiScale) {
        preview = editing;
        float s = screenScale();
        Gfx.setScale(Math.max(1, Math.round(guiScale * s)));
        Gfx.fade = 1f;
        if (width() <= 0) { preview = false; return; }
        float x = Math.round(screenX(sw) * guiScale) / (float) guiScale;
        float y = Math.round(screenY(sh) * guiScale) / (float) guiScale;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0f);
        GlStateManager.scale(s, s, 1f);
        draw();
        GlStateManager.popMatrix();
        preview = false;
    }
}
