package dev.yaclient;

/**
 * Cosmetics catalog and the player's selection. Items are unlocked on the website; the selection is
 * saved locally and synced to the store API (see CosmeticsApi). Rendering is done by cosmetics/CosmeticLayer.
 * Item ids are "<bundle prefix>_<slot>", e.g. ya_cape or mono_wings.
 */
public final class Cosmetics {
    /** One item per slot in every bundle. Order = order in the locker. */
    public static final String[] SLOTS = {"cape", "wings", "bandana", "pet"};
    /** Store ids of the bundles and the prefix of their items. */
    public static final String[] BUNDLES = {"beta", "mono", "qlux", "sakura", "shinobi", "kitsune", "santa", "frost"};
    public static final String[] PREFIX = {"ya", "mono", "qlux", "sakura", "shinobi", "kitsune", "santa", "frost"};
    /** Partner (streamer) bundles are listed in their own "Partner" section. */
    public static final boolean[] PARTNER = {false, false, true, false, false, false, false, false};
    /** Streamer name shown on a partner bundle. */
    public static final String[] PARTNER_NAME = {"", "", "Qlux", "", "", "", "", ""};

    public static boolean isPartner(int bundle) { return PARTNER[bundle]; }

    private Cosmetics() {}

    public static String item(int bundle, String slot) { return PREFIX[bundle] + "_" + slot; }

    public static String slotOf(String item) {
        int i = item == null ? -1 : item.indexOf('_');
        return i < 0 ? null : item.substring(i + 1);
    }

    public static boolean ownsBundle(int bundle) {
        for (String s : SLOTS) if (!CosmeticsApi.owned(item(bundle, s))) return false;
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

    /** Equips (or removes when it is already worn) one item of a bundle. */
    public static void toggle(int bundle, String slot) {
        String item = item(bundle, slot);
        equip(slot, selected(slot).equals(item) ? "none" : item);
    }

    public static boolean wearsBundle(int bundle) {
        for (String s : SLOTS) if (!selected(s).equals(item(bundle, s))) return false;
        return true;
    }

    /** Equips the whole bundle, or removes everything when all of it is already worn. */
    public static void toggleBundle(int bundle) {
        boolean all = wearsBundle(bundle);
        java.util.Map<String, String> m = new java.util.LinkedHashMap<String, String>();
        for (String s : SLOTS) { String v = all ? "none" : item(bundle, s); Config.set("cos." + s, v); m.put(s, v); }
        Config.save();
        CosmeticsApi.pushEquipAll(m);
    }
}
