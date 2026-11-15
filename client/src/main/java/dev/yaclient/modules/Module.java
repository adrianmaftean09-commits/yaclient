package dev.yaclient.modules;

import net.minecraft.client.Minecraft;

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
    /** Chiamato ogni tick solo se il modulo e' attivo. */
    public void onTick() {}
    /** Chiamato ogni tick anche se il modulo e' spento (pulizia). */
    public void onAlwaysTick() {}
    public void onRender() {}
}
