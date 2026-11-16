package dev.yaclient;

/**
 * Cosmetics catalog and the player's selection. Items are unlocked on the website (the Beta Tester
 * bundle is free for beta testers); the selection is saved locally and synced to the store API
 * (see CosmeticsApi). Rendering is done by cosmetics/CosmeticLayer.
 */
public final class Cosmetics {
    /** One item per slot for now. Order = order in the locker. */
    public static final String[] SLOTS = {"cape", "wings", "bandana", "pet"};
    public static final String[] ITEMS = {"ya_cape", "ya_wings", "ya_bandana", "ya_pet"};
    /** Store id of the bundle (it unlocks every item above). */
    public static final String BUNDLE = "beta";

    private Cosmetics() {}

    public static String item(String slot) {
        for (int i = 0; i < SLOTS.length; i++) if (SLOTS[i].equals(slot)) return ITEMS[i];
        return "none";
    }

    public static String slotOf(String item) {
        for (int i = 0; i < ITEMS.length; i++) if (ITEMS[i].equals(item)) return SLOTS[i];
        return null;
    }

    public static boolean ownsBundle() {
        for (String it : ITEMS) if (!CosmeticsApi.owned(it)) return false;
        return true;
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

    /** Equips (or removes when it is already worn) one item. */
    public static void toggle(String slot) {
        String item = item(slot);
        equip(slot, selected(slot).equals(item) ? "none" : item);
    }

    /** Equips the whole bundle, or removes everything when all of it is already worn. */
    public static void toggleBundle() {
        boolean all = true;
        for (String s : SLOTS) if (!selected(s).equals(item(s))) all = false;
        for (String s : SLOTS) equip(s, all ? "none" : item(s));
    }
}
