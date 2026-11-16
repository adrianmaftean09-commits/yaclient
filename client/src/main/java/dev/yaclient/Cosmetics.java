package dev.yaclient;

/**
 * Cosmetics catalog and the player's selection. Items are bought on the website; the selection is
 * saved locally and synced to the store backend (see CosmeticsApi). Rendering is done by
 * cosmetics/CosmeticLayer.
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

    /** What the player chose for the slot ("none" if nothing). */
    public static String selected(String slot) { return Config.get("cos." + slot, "none"); }

    /** What is actually worn: the selection, but only if it is owned. */
    public static String equipped(String slot) {
        String s = selected(slot);
        return (s.equals("none") || !CosmeticsApi.owned(s)) ? "none" : s;
    }

    public static void equip(String slot, String item) {
        Config.set("cos." + slot, item);
        Config.save();
        CosmeticsApi.pushEquip(slot, item);
    }
}
