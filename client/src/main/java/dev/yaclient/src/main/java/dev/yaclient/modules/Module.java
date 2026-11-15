package dev.yaclient.modules;

import dev.yaclient.YAClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public abstract class Module {
    public final String name;
    public boolean enabled;
    protected Minecraft mc;

    protected Module(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    /** Aggiorna il riferimento a Minecraft (non esiste ancora quando i moduli vengono creati). */
    public final void bind() { mc = Minecraft.getMinecraft(); }

    public void toggle() { enabled = !enabled; onToggle(); }
    public void onToggle() {}
    public void onTick() {}
    public void onRender() {}

    /** Riquadro HUD con barretta a sinistra. */
    protected void box(int x, int y, String t) {
        int w = mc.fontRendererObj.getStringWidth(t);
        Gui.drawRect(x, y, x + w + 10, y + 12, YAClient.BLACK_BG);
        Gui.drawRect(x, y, x + 2, y + 12, YAClient.RED);
        mc.fontRendererObj.drawStringWithShadow(t, x + 6, y + 2, YAClient.WHITE);
    }

    /** Riquadro HUD allineato a destra. */
    protected void boxRight(int y, String t) {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = mc.fontRendererObj.getStringWidth(t);
        box(sr.getScaledWidth() - w - 14, y, t);
    }
}
