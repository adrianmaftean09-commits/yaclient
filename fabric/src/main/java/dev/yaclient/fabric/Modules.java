package dev.yaclient.fabric;

import dev.yaclient.fabric.mods.*;
import dev.yaclient.fabric.Module.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** All modules of the Fabric client. New installs start with FPS, CPS and Keystrokes on, everything else off. */
public final class Modules {
    public static final List<Module> ALL = new ArrayList<Module>();
    private static final long START = System.currentTimeMillis();

    private Modules() {}

    private static void text(String id, String name, String desc, Category c, float x, float y, java.util.function.Function<Minecraft, String> f) {
        ALL.add(new TextHud(id, name, desc, c, false, x, y, f));
    }

    private static void textOn(String id, String name, String desc, Category c, float x, float y, java.util.function.Function<Minecraft, String> f) {
        ALL.add(new TextHud(id, name, desc, c, true, x, y, f));
    }

    public static void init() {
        textOn("fps", "FPS", "Shows your frames per second", Category.INFO, 0.008f, 0.012f, mc -> mc.getFps() + " FPS");
        textOn("cps", "CPS", "Shows your clicks per second", Category.PVP, 0.008f, 0.064f, mc -> Hits.cpsText(mc));
        ALL.add(new KeystrokesModule());
        text("coords", "Coordinates", "Shows your XYZ position", Category.INFO, 0.008f, 0.116f, mc -> mc.player == null ? "" :
            "XYZ " + (int) Math.floor(mc.player.getX()) + " " + (int) Math.floor(mc.player.getY()) + " " + (int) Math.floor(mc.player.getZ()));
        text("ping", "Ping", "Your latency to the server", Category.INFO, 0.008f, 0.17f, mc -> {
            if (mc.player == null || mc.getConnection() == null) return "Ping -";
            PlayerInfo pi = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            return "Ping " + (pi == null ? 0 : pi.getLatency()) + " ms";
        });
        text("speed", "Speed", "Your speed in blocks per second", Category.INFO, 0.008f, 0.22f, mc -> {
            if (mc.player == null) return "";
            double dx = mc.player.getX() - mc.player.xo, dz = mc.player.getZ() - mc.player.zo;
            return String.format("%.2f b/s", Math.sqrt(dx * dx + dz * dz) * 20);
        });
        text("direction", "Direction", "Compass direction you face", Category.INFO, 0.008f, 0.27f, mc -> {
            if (mc.player == null) return "";
            float yaw = ((mc.player.getYRot(1f) % 360f) + 360f) % 360f;
            String[] d = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
            return d[Math.round(yaw / 45f) % 8] + " (" + Math.round(yaw) + ")";
        });
        text("clock", "Clock", "Real time", Category.INFO, 0.9f, 0.012f, mc -> String.format("%tR", Calendar.getInstance()));
        text("session", "Session Time", "How long you have been playing", Category.INFO, 0.9f, 0.06f, mc -> {
            long s = (System.currentTimeMillis() - START) / 1000;
            return String.format("%d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
        });
        text("memory", "Memory", "Java memory in use", Category.INFO, 0.9f, 0.11f, mc -> {
            Runtime r = Runtime.getRuntime();
            return ((r.totalMemory() - r.freeMemory()) >> 20) + " / " + (r.maxMemory() >> 20) + " MB";
        });
        text("playercount", "Player Count", "Players online on the server", Category.INFO, 0.9f, 0.16f, mc ->
            mc.getConnection() == null ? "" : mc.getConnection().getOnlinePlayers().size() + " online");
        text("server", "Server Address", "The server you are on", Category.INFO, 0.9f, 0.21f, mc ->
            mc.getCurrentServer() == null ? "Singleplayer" : mc.getCurrentServer().ip);
        text("entities", "Entity Count", "Entities around you", Category.INFO, 0.9f, 0.26f, mc -> {
            if (mc.level == null) return "";
            int n = 0;
            for (Entity e : mc.level.entitiesForRendering()) n++;
            return n + " entities";
        });
        text("falldistance", "Fall Distance", "Blocks you have fallen", Category.INFO, 0.9f, 0.31f, mc ->
            mc.player == null ? "" : String.format("Fall %.1f", mc.player.fallDistance));
        text("health", "Health", "Your health in hearts", Category.PVP, 0.008f, 0.8f, mc -> {
            if (mc.player == null) return "";
            float a = mc.player.getAbsorptionAmount();
            return String.format("%.1f HP", mc.player.getHealth()) + (a > 0 ? String.format(" +%.0f", a) : "");
        });
        text("food", "Food", "Hunger and saturation", Category.INFO, 0.008f, 0.85f, mc ->
            mc.player == null ? "" : "Food " + mc.player.getFoodData().getFoodLevel() + " (" + String.format("%.1f", mc.player.getFoodData().getSaturationLevel()) + ")");
        text("xp", "XP Level", "Your experience level", Category.INFO, 0.008f, 0.9f, mc ->
            mc.player == null ? "" : "Level " + mc.player.experienceLevel);
        text("potions", "Potion Effects", "Active effects and time left", Category.PVP, 0.9f, 0.4f, mc -> {
            if (mc.player == null) return "";
            StringBuilder b = new StringBuilder();
            for (MobEffectInstance e : mc.player.getActiveEffects()) {
                int s = e.getDuration() / 20;
                b.append(e.getEffect().value().getDisplayName().getString()).append(' ').append(e.getAmplifier() + 1)
                 .append(e.isInfiniteDuration() ? " **" : String.format(" %d:%02d", s / 60, s % 60)).append('\n');
            }
            return b.length() == 0 ? "No effects" : b.substring(0, b.length() - 1);
        });
        text("blockinfo", "Block Info", "Name of the block you look at", Category.INFO, 0.42f, 0.012f, mc -> {
            if (mc.level == null || !(mc.hitResult instanceof BlockHitResult bh) || bh.getType() != HitResult.Type.BLOCK) return "";
            return mc.level.getBlockState(bh.getBlockPos()).getBlock().getName().getString();
        });
        text("iteminfo", "Item Info", "Held item and durability", Category.INFO, 0.42f, 0.9f, mc -> {
            if (mc.player == null) return "";
            var st = mc.player.getMainHandItem();
            if (st.isEmpty()) return "";
            String s = st.getHoverName().getString();
            if (st.isDamageableItem()) s += " " + (st.getMaxDamage() - st.getDamageValue()) + "/" + st.getMaxDamage();
            return s;
        });
        text("bowcharge", "Bow Charge", "How charged your bow is", Category.PVP, 0.42f, 0.55f, mc -> {
            if (mc.player == null || !mc.player.isUsingItem()) return "";
            return "Charge " + Math.min(100, mc.player.getTicksUsingItem() * 5) + "%";
        });
        text("nearby", "Nearby Players", "Players within 64 blocks", Category.PVP, 0.9f, 0.6f, mc -> {
            if (mc.level == null || mc.player == null) return "";
            StringBuilder b = new StringBuilder();
            int n = 0;
            for (Player p : mc.level.players()) {
                if (p == mc.player || n >= 8) continue;
                float d = mc.player.distanceTo(p);
                if (d > 64) continue;
                b.append(p.getName().getString()).append("  ").append(Math.round(d)).append("m\n");
                n++;
            }
            return n == 0 ? "No players nearby" : b.substring(0, b.length() - 1);
        });
        text("combo", "Combo", "Consecutive hits without being hit", Category.PVP, 0.42f, 0.7f, mc -> "Combo " + Hits.combo);
        text("reach", "Reach", "Distance of your last hit", Category.PVP, 0.42f, 0.75f, mc -> String.format("Reach %.2f", Hits.reach));
        text("totempops", "Totem Pops", "Totems you have popped this session (counted from your inventory)", Category.CRYSTAL, 0.008f, 0.45f, mc -> "Pops " + Hits.pops(mc));
        ALL.add(new CrystalCounterModule());
        ALL.add(new TargetHud());
        ALL.add(new ItemsHud());
        ALL.add(new InventoryHud());
        ALL.add(new Overlays.Crosshair());
        ALL.add(new Overlays.LowHealth());
        ALL.add(new Utility.Fullbright());
        ALL.add(new Utility.NoClouds());
        ALL.add(new Utility.ToggleSprint());
        ALL.add(new Utility.Zoom());
        for (Module m : ALL) m.load();
    }

    public static void tick() {
        for (Module m : ALL) if (m.enabled) m.onTick();
        Hits.tick(Minecraft.getInstance());
    }
}
