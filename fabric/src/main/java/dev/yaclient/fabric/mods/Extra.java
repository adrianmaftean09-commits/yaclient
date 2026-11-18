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

    public static void tick(Minecraft mc) {
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
