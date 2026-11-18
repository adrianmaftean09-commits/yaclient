package dev.yaclient.fabric.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Small helpers behind the SMP / BoxPvP / SkyBlock style HUD modules. */
public final class Extra {
    private Extra() {}

    private static int[] last;      // last position while alive
    private static String death = "";

    public static boolean rainbow;

    public static int rainbowRgb(int line) {
        float h = ((System.currentTimeMillis() % 4000L) / 4000f + line * 0.07f) % 1f;
        return 0xFF000000 | (java.awt.Color.HSBtoRGB(h, 0.55f, 1f) & 0xFFFFFF);
    }

    public static int bestCombo;

    public static String stats() {
        if (dev.yaclient.fabric.Hits.combo > bestCombo) bestCombo = dev.yaclient.fabric.Hits.combo;
        return "Combo " + dev.yaclient.fabric.Hits.combo + "\nBest " + bestCombo + "\nTotem pops " + dev.yaclient.fabric.Hits.pops(Minecraft.getInstance());
    }

    public static String targetDistance(Minecraft mc) {
        if (mc.player == null || mc.hitResult == null || mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.MISS) return "";
        double d = mc.hitResult.getLocation().distanceTo(mc.player.getEyePosition());
        return String.format("Target %.1f m", d);
    }

    public static String mobCount(Minecraft mc) {
        if (mc.level == null || mc.player == null) return "";
        int n = 0;
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering())
            if (e instanceof net.minecraft.world.entity.monster.Enemy && e.distanceToSqr(mc.player) < 1024.0) n++;
        return "Mobs " + n;
    }

    public static String dropCount(Minecraft mc) {
        if (mc.level == null || mc.player == null) return "";
        int n = 0;
        for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering())
            if (e instanceof net.minecraft.world.entity.item.ItemEntity && e.distanceToSqr(mc.player) < 1024.0) n += ((net.minecraft.world.entity.item.ItemEntity) e).getItem().getCount();
        return "Drops " + n;
    }

    public static void tick(Minecraft mc) {
        dev.yaclient.fabric.Module rb = dev.yaclient.fabric.Modules.byId("rainbowhud");
        rainbow = rb != null && rb.active();
        Player p = mc.player;
        if (p == null) { last = null; return; }
        if (p.getHealth() <= 0f || p.isDeadOrDying()) {
            if (last != null) death = last[0] + " " + last[1] + " " + last[2] + " (" + last[3] + ")";
            last = null;
            return;
        }
        last = new int[] {(int) Math.floor(p.getX()), (int) Math.floor(p.getY()), (int) Math.floor(p.getZ()), 0};
    }

    public static String deathText() { return death.isEmpty() ? "No death yet" : "Died at " + death; }

    public static int count(Minecraft mc, Item... items) {
        Player p = mc.player;
        if (p == null) return 0;
        int n = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            var s = p.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            for (Item it : items) if (s.is(it)) { n += s.getCount(); break; }
        }
        return n;
    }

    public static int freeSlots(Minecraft mc) {
        Player p = mc.player;
        if (p == null) return 0;
        int n = 0;
        for (int i = 0; i < 36 && i < p.getInventory().getContainerSize(); i++) if (p.getInventory().getItem(i).isEmpty()) n++;
        return n;
    }

    public static String netherCoords(Minecraft mc) {
        if (mc.player == null || mc.level == null) return "";
        double scale = mc.level.dimensionType().coordinateScale();
        boolean nether = scale > 1.0;
        double k = nether ? 8.0 : 0.125;
        return (nether ? "Overworld " : "Nether ") + (int) Math.floor(mc.player.getX() * k) + " " + (int) Math.floor(mc.player.getZ() * k);
    }

    public static String light(Minecraft mc) {
        if (mc.player == null || mc.level == null) return "";
        BlockPos pos = BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        int l = mc.level.getMaxLocalRawBrightness(pos);
        return "Light " + l + (l < 8 ? " (mobs)" : "");
    }

    private static java.lang.reflect.Method dayM;
    private static boolean dayTried;

    /** getDayTime() was renamed / moved in newer versions: look it up at runtime, fall back to the game time. */
    private static long dayTime(net.minecraft.world.level.Level lv) {
        try {
            if (!dayTried) { dayTried = true; try { dayM = lv.getClass().getMethod("getDayTime"); } catch (Exception ignored) { } }
            if (dayM != null) return ((Number) dayM.invoke(lv)).longValue();
        } catch (Exception ignored) { }
        return lv.getGameTime();
    }

    public static String day(Minecraft mc) {
        if (mc.level == null) return "";
        long t = dayTime(mc.level);
        long tod = t % 24000L;
        return "Day " + (t / 24000L + 1) + String.format("  %02d:%02d", (tod / 1000 + 6) % 24, (tod % 1000) * 60 / 1000);
    }

    public static final Item[] PEARL = {Items.ENDER_PEARL};
}
