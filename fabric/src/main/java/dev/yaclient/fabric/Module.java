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

    /** Icon name for the vector icon set (falls back to a generic one). */
    public String icon() {
        String n;
        switch (id) {
            case "bowcharge": n = "bow_charge"; break;
            case "blockinfo": n = "block_info"; break;
            case "lowhealth": n = "low_health"; break;
            case "targethud": n = "target_hud"; break;
            case "crystalcount": n = "item_counter"; break;
            case "inventory": n = "inventory_hud"; break;
            case "nearby": n = "nearby_players"; break;
            case "server": n = "server_ip"; break;
            case "playercount": n = "players"; break;
            case "falldistance": n = "fall"; break;
            case "iteminfo": n = "item_info"; break;
            case "togglesprint": n = "toggle_sprint"; break;
            case "noclouds": n = "no_clouds"; break;
            case "totempops": n = "combo"; break;
            default: n = id;
        }
        return Gfx.hasIcon(n) ? n : "grid";
    }

    public void load() { enabled = Config.getBool("mod." + id + ".enabled", enabled); }

    public void save() { Config.setBool("mod." + id + ".enabled", enabled); }

    public void toggle() {
        enabled = !enabled;
        save();
        Config.save();
        onToggle();
    }

    public void onTick() {}

    public void onToggle() {}
}
