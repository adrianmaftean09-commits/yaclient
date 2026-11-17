package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A module that draws a box on the HUD (position and scale are stored in the config, relative to the screen). */
public abstract class HudModule extends Module {
    public float px, py, scale;

    protected HudModule(String id, String name, String description, Category category, boolean enabledByDefault, float defX, float defY) {
        super(id, name, description, category, enabledByDefault);
        this.px = defX;
        this.py = defY;
        this.scale = 1f;
    }

    @Override
    public void load() {
        super.load();
        px = Config.getFloat("hud." + id + ".x", px);
        py = Config.getFloat("hud." + id + ".y", py);
        scale = Math.max(0.5f, Math.min(2.5f, Config.getFloat("hud." + id + ".s", scale)));
    }

    @Override
    public void save() {
        super.save();
        Config.setFloat("hud." + id + ".x", px);
        Config.setFloat("hud." + id + ".y", py);
        Config.setFloat("hud." + id + ".s", scale);
    }

    /** Width and height of the content in GUI pixels (before the scale). */
    public abstract int width(Minecraft mc);

    public abstract int height(Minecraft mc);

    /** Draws the content with its top left corner at (0, 0). */
    public abstract void draw(GuiGraphicsExtractor g, Minecraft mc);

    public final void render(GuiGraphicsExtractor g, Minecraft mc) {
        int sw = g.guiWidth(), sh = g.guiHeight();
        int x = Math.round(px * sw), y = Math.round(py * sh);
        g.pose().pushMatrix();
        g.pose().translate((float) x, (float) y);
        g.pose().scale(scale, scale);
        draw(g, mc);
        g.pose().popMatrix();
    }
}
