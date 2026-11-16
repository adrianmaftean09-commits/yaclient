package dev.yaclient;

import dev.yaclient.modules.*;
import dev.yaclient.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.weavemc.api.ModInitializer;
import net.weavemc.api.event.EventBus;
import net.weavemc.api.event.RenderGameOverlayEvent;
import net.weavemc.api.event.TickEvent;
import org.lwjgl.input.Keyboard;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

/** YA CLIENT entry point (Weave loader, no Forge). Only legitimate features: no cheats. */
public class YAClient implements ModInitializer {
    public static final String NAME = "YA CLIENT";
    public static final String VERSION = "1.1.0";
    /** Key that opens the client menu (close it with ESC). */
    public static final int MENU_KEY = Keyboard.KEY_RSHIFT;

    public static final List<Module> MODULES = new ArrayList<Module>();
    private boolean menuKeyWasDown;

    @Override
    public void init() {
        Config.load();
        Lang.load(Config.get("lang", "en"));
        Theme.load();
        Hud.loadConfig();

        // HUD elements
        MODULES.add(new FpsModule());
        MODULES.add(new CpsModule());
        MODULES.add(new CoordsModule());
        MODULES.add(new KeystrokesModule());
        MODULES.add(new ArmorModule());
        MODULES.add(new PingModule());
        MODULES.add(new DirectionModule());
        MODULES.add(new ClockModule());
        MODULES.add(new SpeedModule());
        MODULES.add(new MemoryModule());
        MODULES.add(new HealthModule());
        MODULES.add(new PotionModule());
        MODULES.add(new ReachModule());
        MODULES.add(new SpotifyModule());
        MODULES.add(new WaypointsModule());
        // utilities
        MODULES.add(new ToggleSprintModule());
        MODULES.add(new ToggleSneakModule());
        MODULES.add(new ZoomModule());
        MODULES.add(new FullbrightModule());
        MODULES.add(new MotionBlurModule());
        MODULES.add(new CrosshairModule());
        MODULES.add(new DiscordRpcModule());
        // planned (need the Mixin phase, see docs/PHASE2.md)
        MODULES.add(new SoonModule("hit_color", Category.PVP));
        MODULES.add(new SoonModule("old_animations", Category.PVP));
        MODULES.add(new SoonModule("autogg", Category.UTILITY));
        MODULES.add(new SoonModule("levelhead", Category.INFO));
        MODULES.add(new SoonModule("minimap", Category.INFO));

        for (Module m : MODULES) m.loadConfig();

        EventBus.subscribe(TickEvent.Post.class, e -> {
            Minecraft mc = Minecraft.getMinecraft();

            // our title screen replaces the vanilla one
            if (mc.currentScreen != null && mc.currentScreen.getClass() == GuiMainMenu.class) {
                mc.displayGuiScreen(new MainMenuScreen());
            }

            for (Module m : MODULES) {
                m.bind();
                m.onAlwaysTick();
                if (m.enabled) m.onTick();
            }

            boolean down = Keyboard.isKeyDown(MENU_KEY);
            if (down && !menuKeyWasDown && mc.currentScreen == null) {
                mc.displayGuiScreen(new MenuScreen());
            }
            menuKeyWasDown = down;
        });

        EventBus.subscribe(RenderGameOverlayEvent.Post.class, e -> Hud.render());

        System.out.println("[" + NAME + "] " + VERSION + " loaded. Press Right Shift for the menu.");
    }

    @Override
    @SuppressWarnings("deprecation")
    public void preInit(Instrumentation instrumentation) {}
}
