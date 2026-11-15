package dev.yaclient;

import dev.yaclient.modules.*;
import net.minecraft.client.Minecraft;
import net.weavemc.loader.api.ModInitializer;
import net.weavemc.loader.api.event.EventBus;
import net.weavemc.loader.api.event.KeyboardEvent;
import net.weavemc.loader.api.event.RenderGameOverlayEvent;
import net.weavemc.loader.api.event.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/** YA CLIENT - entrypoint (Weave, senza Forge). Solo funzioni lecite: nessun cheat. */
public class YAClient implements ModInitializer {
    public static final String NAME = "YA CLIENT";
    public static final int RED = 0xFFE10600;
    public static final int WHITE = 0xFFF5F5F5;
    public static final int BLACK_BG = 0x99000000;
    public static final int MENU_KEY = Keyboard.KEY_RSHIFT;

    public static final List<Module> MODULES = new ArrayList<>();

    @Override
    public void preInit() {
        MODULES.add(new FpsModule());
        MODULES.add(new CpsModule());
        MODULES.add(new CoordsModule());
        MODULES.add(new KeystrokesModule());
        MODULES.add(new ArmorModule());
        MODULES.add(new ToggleSprintModule());
        MODULES.add(new ZoomModule());

        EventBus.subscribe(TickEvent.Post.class, e -> {
            for (Module m : MODULES) if (m.enabled) m.onTick();
        });
        EventBus.subscribe(RenderGameOverlayEvent.Post.class, e -> {
            for (Module m : MODULES) if (m.enabled) m.onRender();
        });
        EventBus.subscribe(KeyboardEvent.class, e -> {
            Minecraft mc = Minecraft.getMinecraft();
            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == MENU_KEY && mc.currentScreen == null) {
                mc.displayGuiScreen(new MenuScreen());
            }
        });
        System.out.println("[" + NAME + "] caricato. Premi RShift per il menu.");
    }
}
