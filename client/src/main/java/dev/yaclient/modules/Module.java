package dev.yaclient.modules;

import net.minecraft.client.Minecraft;

public abstract class Module {
    public final String name;
    public boolean enabled;
    protected final Minecraft mc = Minecraft.getMinecraft();

    protected Module(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    public void toggle() { enabled = !enabled; onToggle(); }
    public void onToggle() {}
    public void onTick() {}
    public void onRender() {}
}
