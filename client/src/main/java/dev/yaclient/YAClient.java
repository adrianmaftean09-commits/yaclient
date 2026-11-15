package dev.yaclient;

import dev.yaclient.modules.*;
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

/** YA CLIENT - entrypoint (Weave, senza Forge). Solo funzioni lecite: nessun cheat. */
public class YAClient implements ModInitializer {
    public static final String NAME = "YA CLIENT";
    public static final int RED = 0xFFE10600;
    /** Tasto per aprire il menu dei moduli (si chiude solo con ESC). */
    public static final int MENU_KEY = Keyboard.KEY_RSHIFT;

    public static final List<Module> MODULES = new ArrayList<Module>();
    private boolean menuKeyWasDown;

    @Override
    public void init() {
        MODULES.add(new FpsModule());
        MODULES.add(new CpsModule());
        MODULES.add(new CoordsModule());
        MODULES.add(new KeystrokesModule());
        MODULES.add(new ArmorModule());
        MODULES.add(new ToggleSprintModule());
        MODULES.add(new ZoomModule());
        MODULES.add(new PingModule());
        MODULES.add(new DirectionModule());
        MODULES.add(new ClockModule());
        MODULES.add(new SpeedModule());
        MODULES.add(new MemoryModule());
        MODULES.add(new HealthModule());
        MODULES.add(new PotionModule());
        MODULES.add(new ReachModule());
        MODULES.add(new ToggleSneakModule());
        MODULES.add(new FullbrightModule());
        Config.load();

        EventBus.subscribe(TickEvent.Post.class, e -> {
            Minecraft mc = Minecraft.getMinecraft();

            // menu principale di YA CLIENT al posto di quello di Minecraft
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

        EventBus.subscribe(RenderGameOverlayEvent.Post.class, e -> {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.gameSettings.hideGUI || mc.gameSettings.showDebugInfo) return;
            Hud.begin();
            for (Module m : MODULES) {
                m.bind();
                if (m.enabled) m.onRender();
            }
            Hud.end();
        });

        System.out.println("[" + NAME + "] caricato. Premi RShift per il menu.");
    }

    @Override
    @SuppressWarnings("deprecation")
    public void preInit(Instrumentation instrumentation) {}
}
