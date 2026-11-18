package dev.yaclient.modules;

import dev.yaclient.Setting;

/**
 * Free Look: hold the key and the camera turns around you while your player keeps facing (and aiming) the same way.
 * The key is chosen in Options > Controls > "YA Client". The work is done by {@link dev.yaclient.FreeLook}.
 */
public class FreeLookModule extends Module {
    private final Setting.Bool third = add(new Setting.Bool("third_person", true));
    private static FreeLookModule inst;

    public FreeLookModule() { super("freelook", Category.UTILITY, true); inst = this; }

    public static boolean on() { return inst != null && inst.enabled && !inst.locked(); }

    public static boolean thirdPerson() { return inst == null || inst.third.value; }
}
