package dev.yaclient.modules;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClockModule extends Module {
    private final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
    private long lastSec = -1;
    private String text = "";

    public ClockModule() { super("Orologio", false); }

    @Override
    public void onRender() {
        long sec = System.currentTimeMillis() / 1000;
        if (sec != lastSec) { lastSec = sec; text = fmt.format(new Date()); }
        boxRight(36, text);
    }
}
