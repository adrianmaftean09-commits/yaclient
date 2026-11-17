package dev.yaclient.modules;

import dev.yaclient.Setting;
import dev.yaclient.YaChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** Chat options: remove the dark background behind the lines and make the whole chat bigger or smaller. */
public class ChatModule extends Module {
    private final Setting.Bool hideBg = add(new Setting.Bool("hide_bg", true));
    private final Setting.Num scale = add(new Setting.Num("scale", 30f, 250f, 5f, 100f, "%"));

    private static ChatModule inst;
    private boolean failed;

    public ChatModule() { super("chat", Category.UTILITY, false); inst = this; }

    public static boolean bgHidden() { return inst != null && inst.enabled && inst.hideBg.value; }

    public static float userScale() { return inst != null && inst.enabled ? inst.scale.value / 100f : 1f; }

    /** Swaps the game's chat for the YA chat (same history), once. Runs every tick so it survives a GUI swap. */
    @Override
    public void onAlwaysTick() {
        if (failed || mc == null || mc.ingameGUI == null) return;
        GuiIngame gui = mc.ingameGUI;
        try {
            if (gui.getChatGUI() instanceof YaChat) return;
            Field slot = null;
            for (Field f : GuiIngame.class.getDeclaredFields()) {
                if (f.getType() == GuiNewChat.class && !Modifier.isStatic(f.getModifiers())) { slot = f; break; }
            }
            if (slot == null) { failed = true; return; }
            slot.setAccessible(true);
            GuiNewChat old = (GuiNewChat) slot.get(gui);
            YaChat neu = new YaChat(mc);
            for (Field f : GuiNewChat.class.getDeclaredFields()) {      // keep the lines, the history and the scroll position
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                f.set(neu, f.get(old));
            }
            slot.set(gui, neu);
        } catch (Throwable t) {
            failed = true;     // never break the game because of the chat
        }
    }
}
