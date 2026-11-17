package dev.yaclient.fabric;

import net.minecraft.client.Minecraft;

/** Base of every client feature. Ids are stable (used for the config). */
public abstract class Module {
    public final String id;
    public final String name;
    public final String description;
    public final Category category;
    public boolean enabled;

    public enum Category { INFO, PVP, CRYSTAL, UTILITY }

    protected Module(String id, String name, String description, Category category, boolean enabledByDefault) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = enabledByDefault;
    }

    protected static Minecraft mc() { return Minecraft.getInstance(); }

    public void load() { enabled = Config.getBool("mod." + id + ".enabled", enabled); }

    public void save() { Config.setBool("mod." + id + ".enabled", enabled); }

    public void toggle() {
        enabled = !enabled;
        save();
        Config.save();
        onToggle();
    }

    /** Called every client tick while the module is on. */
    public void onTick() {}

    public void onToggle() {}
}
