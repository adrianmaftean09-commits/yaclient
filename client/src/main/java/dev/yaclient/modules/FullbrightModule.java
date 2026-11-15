package dev.yaclient.modules;

/** Luce sempre: luminosita' al massimo (solo lato client). Alcuni server possono vietarla. */
public class FullbrightModule extends Module {
    private float saved = 1f;
    private boolean active;

    public FullbrightModule() { super("Luce Sempre", false); }

    @Override
    public void onTick() {
        if (mc.gameSettings == null) return;
        if (!active) {
            saved = mc.gameSettings.gammaSetting;
            active = true;
        }
        mc.gameSettings.gammaSetting = 100f;
    }

    @Override
    public void onAlwaysTick() {
        // se il gioco e' stato chiuso con la luce attiva, il valore resterebbe salvato nelle opzioni
        if (!enabled && !active && mc.gameSettings != null && mc.gameSettings.gammaSetting > 1f) {
            mc.gameSettings.gammaSetting = 1f;
        }
    }

    @Override
    public void onToggle() {
        if (!enabled && active && mc != null && mc.gameSettings != null) {
            mc.gameSettings.gammaSetting = saved > 1f ? 1f : saved;
            active = false;
        }
    }
}
