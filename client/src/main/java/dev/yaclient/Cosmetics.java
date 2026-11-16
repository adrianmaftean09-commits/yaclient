package dev.yaclient;

/**
 * Cosmetics catalog and the player's selection. The selection is saved in the config; rendering
 * on the player model is done by the render layer of phase 2 (see docs/PHASE2.md), which reads
 * equipped(slot).
 */
public final class Cosmetics {
    public static final String[] SLOTS = {"wings", "cape", "hat", "bandana"};
    private static final String[][] ITEMS = {
            {"angel", "dragon"},
            {"classic", "crimson", "midnight"},
            {"tophat", "crown"},
            {"red", "black"}
    };

    private Cosmetics() {}

    public static String[] items(String slot) {
        for (int i = 0; i < SLOTS.length; i++) if (SLOTS[i].equals(slot)) return ITEMS[i];
        return new String[0];
    }

    /** Equipped item id for the slot, or "none". */
    public static String equipped(String slot) { return Config.get("cos." + slot, "none"); }

    public static void equip(String slot, String item) {
        Config.set("cos." + slot, item);
        Config.save();
    }
}
