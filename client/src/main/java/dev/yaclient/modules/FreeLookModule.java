package dev.yaclient.modules;

import dev.yaclient.Setting;

/** Hold the Free Look key (Options > Controls > "YA Client") to look around without turning your player. */
public class FreeLookModule extends Module {
    private final Setting.Bool third = add(new Setting.Bool("third_person", true));
    private static FreeLookModule inst;

    public FreeLookModule() { super("freelook", Category.UTILITY, true); inst = this; }

    public static boolean on() { return inst != null && inst.enabled; }

    public static boolean thirdPerson() { return inst == null || inst.third.value; }
}
