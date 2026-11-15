package dev.yaclient.modules;

import dev.yaclient.Hud;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClockModule extends Module {
    private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
    private long lastMin = -1;
    private String text = "";

    public ClockModule() { super("Orologio", false); }

    @Override
    public void onRender() {
        long min = System.currentTimeMillis() / 60000L;
        if (min != lastMin) { lastMin = min; text = fmt.format(new Date()); }
        Hud.right(text);
    }
}
