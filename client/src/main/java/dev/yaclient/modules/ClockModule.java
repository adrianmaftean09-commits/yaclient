package dev.yaclient.modules;

import dev.yaclient.Setting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClockModule extends PillModule {
    private final Setting.Bool h24 = add(new Setting.Bool("h24", true));
    private final SimpleDateFormat f24 = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat f12 = new SimpleDateFormat("hh:mm a");
    private long lastMin = -1;
    private boolean lastMode;
    private String text = "";

    public ClockModule() { super("clock", Category.INFO, false, 0.99f, 0.116f); }

    @Override
    protected String text() {
        long min = System.currentTimeMillis() / 60000L;
        if (min != lastMin || lastMode != h24.value) {
            lastMin = min;
            lastMode = h24.value;
            text = (h24.value ? f24 : f12).format(new Date());
        }
        return text;
    }
}
