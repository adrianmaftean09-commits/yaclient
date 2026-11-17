package dev.yaclient.modules;

import dev.yaclient.Config;
import dev.yaclient.Lang;
import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class of every client feature. Ids are stable (used for config and translations):
 * display name = lang key "mod.&lt;id&gt;.name", description = "mod.&lt;id&gt;.desc".
 */
public abstract class Module {
    public final String id;
    public final Category category;
    /** False for planned features that are listed in the menu but cannot be enabled yet. */
    public final boolean available;
    public boolean enabled;
    public final List<Setting> settings = new ArrayList<Setting>();
    protected Minecraft mc;

    protected Module(String id, Category category, boolean enabledByDefault) {
        this(id, category, enabledByDefault, true);
    }

    protected Module(String id, Category category, boolean enabledByDefault, boolean available) {
        this.id = id;
        this.category = category;
        this.available = available;
        this.enabled = available && enabledByDefault;
    }

    /** Icon name: the module's own icon when it has one, otherwise its category icon. */
    public String icon() { return dev.yaclient.Gfx.hasIcon(id) ? id : category.icon; }

    public String name() { return Lang.t("mod." + id + ".name"); }

    public String desc() { return Lang.t("mod." + id + ".desc"); }

    protected <T extends Setting> T add(T s) {
        s.owner = id;
        settings.add(s);
        return s;
    }

    protected <T extends Setting> T add(String group, T s) {
        s.group = group;
        return add(s);
    }

    /** Every option back to its default value. */
    public void resetSettings() {
        for (Setting s : settings) s.reset();
        saveConfig();
    }

    public void loadConfig() {
        enabled = available && Config.getBool("mod." + id + ".enabled", enabled);
        for (Setting s : settings) s.load();
    }

    public void saveConfig() {
        Config.setBool("mod." + id + ".enabled", enabled);
        for (Setting s : settings) s.save();
    }

    /** Refreshes the Minecraft reference (it does not exist yet when modules are created). */
    public final void bind() { mc = Minecraft.getMinecraft(); }

    public void toggle() {
        if (!available) return;
        enabled = !enabled;
        onToggle();
        saveConfig();
        Config.save();
    }

    public void onToggle() {}

    /** Called every tick while the module is enabled. */
    public void onTick() {}

    /** Called every tick even when the module is disabled (cleanup). */
    public void onAlwaysTick() {}

    /** Called every frame while enabled, before the HUD elements are drawn. */
    public void onRender() {}
}
