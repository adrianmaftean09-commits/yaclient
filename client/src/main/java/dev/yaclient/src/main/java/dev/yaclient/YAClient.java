package dev.yaclient;

import dev.yaclient.modules.*;
import net.minecraft.client.Minecraft;
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
    public static final int WHITE = 0xFFF5F5F5;
    public static final int BLACK_BG = 0x99000000;
    /** Tasto per aprire il menu (si chiude solo con ESC). */
    public static final int MENU_KEY = Keyboard.KEY_RSHIFT;

    public static final List<Module> MODULES = new ArrayList<>();
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
        MODULES.add(new PotionModule());

        EventBus.subscribe(TickEvent.Post.class, e -> {
            for (Module m : MODULES) {
                m.bind();
                if (m.enabled) m.onTick();
            }

            Minecraft mc = Minecraft.getMinecraft();
            boolean down = Keyboard.isKeyDown(MENU_KEY);
            if (down && !menuKeyWasDown && mc.currentScreen == null) {
                mc.displayGuiScreen(new MenuScreen());
            }
            menuKeyWasDown = down;
        });

        EventBus.subscribe(RenderGameOverlayEvent.Post.class, e -> {
            for (Module m : MODULES) {
                m.bind();
                if (m.enabled) m.onRender();
            }
        });

        System.out.println("[" + NAME + "] caricato. Premi RShift per il menu.");
    }

    @Override
    @SuppressWarnings("deprecation")
    public void preInit(Instrumentation instrumentation) {}
}
