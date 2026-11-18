package dev.yaclient.fabric;

/**
 * Cosmetics catalog and the player's selection (same ids as the 1.8.9 client). Items are unlocked on the
 * website, the selection is synced to the store API (see CosmeticsApi).
 */
public final class Cosmetics {
    public static final String[] SLOTS = {"cape", "wings", "bandana", "pet"};
    public static final String[] BUNDLES = {"beta", "mono", "qlux", "sakura", "shinobi", "kitsune", "santa", "frost", "clown", "luck"};
    public static final String[] PREFIX = {"ya", "mono", "qlux", "sakura", "shinobi", "kitsune", "santa", "frost", "clown", "luck"};
    public static final String[] NAMES = {"Beta Tester", "Monochrome", "Qlux", "Sakura", "Shinobi", "Kitsune", "Santa", "Frost", "Clown", "Luck"};
    public static final boolean[] PARTNER = {false, false, true, false, false, false, false, false, false, false};

    private Cosmetics() {}

    public static String item(int bundle, String slot) { return PREFIX[bundle] + "_" + slot; }

    public static boolean ownsBundle(int bundle) {
        for (String s : SLOTS) if (!CosmeticsApi.owned(item(bundle, s))) return false;
        return true;
    }

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

    public static void toggle(int bundle, String slot) {
        String item = item(bundle, slot);
        equip(slot, selected(slot).equals(item) ? "none" : item);
    }

    public static boolean wearsBundle(int bundle) {
        for (String s : SLOTS) if (!selected(s).equals(item(bundle, s))) return false;
        return true;
    }

    public static void toggleBundle(int bundle) {
        boolean all = wearsBundle(bundle);
        java.util.Map<String, String> m = new java.util.LinkedHashMap<String, String>();
        for (String s : SLOTS) { String v = all ? "none" : item(bundle, s); Config.set("cos." + s, v); m.put(s, v); }
        Config.save();
        CosmeticsApi.pushEquipAll(m);
    }
}
