package dev.yaclient.fabric;

import dev.yaclient.fabric.mods.CoordsModule;
import dev.yaclient.fabric.mods.CpsModule;
import dev.yaclient.fabric.mods.CrystalCounterModule;
import dev.yaclient.fabric.mods.FpsModule;
import dev.yaclient.fabric.mods.KeystrokesModule;

import java.util.ArrayList;
import java.util.List;

/** All modules of the Fabric client. New installs start with FPS, CPS and Keystrokes on, everything else off. */
public final class Modules {
    public static final List<Module> ALL = new ArrayList<Module>();

    private Modules() {}

    public static void init() {
        ALL.add(new FpsModule());
        ALL.add(new CpsModule());
        ALL.add(new KeystrokesModule());
        ALL.add(new CoordsModule());
        ALL.add(new CrystalCounterModule());
        for (Module m : ALL) m.load();
    }

    public static void tick() {
        for (Module m : ALL) if (m.enabled) m.onTick();
    }
}
