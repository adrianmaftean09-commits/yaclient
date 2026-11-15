package dev.yaclient.modules;

import dev.yaclient.Hud;

import java.util.Locale;

public class HealthModule extends Module {
    public HealthModule() { super("Salute", false); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null) return;
        Hud.right(String.format(Locale.US, "%.1f HP", mc.thePlayer.getHealth()));
    }
}
