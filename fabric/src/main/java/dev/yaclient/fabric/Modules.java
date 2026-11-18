package dev.yaclient.fabric;

import dev.yaclient.fabric.mods.*;
import dev.yaclient.fabric.Module.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
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

    private static TextHud text(String id, String name, String desc, Category c, float x, float y, java.util.function.Function<Minecraft, String> f) {
        TextHud t = new TextHud(id, name, desc, c, false, x, y, f);
        ALL.add(t);
        return t;
    }

    private static TextHud textOn(String id, String name, String desc, Category c, float x, float y, java.util.function.Function<Minecraft, String> f) {
        TextHud t = new TextHud(id, name, desc, c, true, x, y, f);
        ALL.add(t);
        return t;
    }

    public static void init() {
        final Setting.Bool[] fl = new Setting.Bool[1], fc = new Setting.Bool[1];
        final Setting.Num[] fw = new Setting.Num[1];
        TextHud fps = textOn("fps", "FPS", "Shows your frames per second", Category.INFO, 0.008f, 0.012f, mc -> mc.getFps() + (fl[0].value ? " FPS" : ""));
        fl[0] = fps.add("general", new Setting.Bool("label", true));
        fc[0] = fps.add("general", new Setting.Bool("colorize", false));
        fw[0] = fps.add("general", new Setting.Num("warn", 20f, 144f, 5f, 60f, ""));
        fps.autoColor = mc -> {
            if (!fc[0].value) return 0;
            int f = mc.getFps();
            return f >= fw[0].value ? 0xFF22C55E : (f >= fw[0].value / 2f ? 0xFFFFC21A : 0xFFEF4444);
        };
        final Setting.Bool[] cl = new Setting.Bool[1];
        TextHud cps = textOn("cps", "CPS", "Shows your clicks per second", Category.PVP, 0.008f, 0.064f, mc -> Hits.cpsText(mc, cl[0].value));
        cl[0] = cps.add("general", new Setting.Bool("label", true));
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
        final Setting.Bool[] h24 = new Setting.Bool[1];
        TextHud clk = text("clock", "Clock", "Real time", Category.INFO, 0.9f, 0.012f, mc -> h24[0].value ? String.format("%tR", Calendar.getInstance()) : String.format("%tl:%tM %tp", Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance()));
        h24[0] = clk.add("general", new Setting.Bool("h24", true));
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
        // ---- SMP / BoxPvP / SkyBlock helpers ----
        text("netherxyz", "Nether Coords", "[SMP] Matching Nether / Overworld coordinates for portals", Category.INFO, 0.008f, 0.32f, Extra::netherCoords);
        text("deathpos", "Death Location", "[SMP] Where you died last, so you can go back for your items", Category.INFO, 0.008f, 0.37f, mc -> Extra.deathText());
        text("daycount", "Day Counter", "[SMP] World day and in-game time", Category.INFO, 0.9f, 0.36f, Extra::day);
        text("lightlevel", "Light Level", "[SMP / SkyBlock] Light at your feet, below 8 mobs can spawn", Category.INFO, 0.008f, 0.42f, Extra::light);
        text("freeslots", "Free Slots", "[SkyBlock / SMP] Empty inventory slots left", Category.INFO, 0.9f, 0.46f, mc -> mc.player == null ? "" : Extra.freeSlots(mc) + " free slots");
        text("potcount", "Potion Count", "[BoxPvP] Splash potions in your inventory", Category.PVP, 0.008f, 0.55f, mc -> mc.player == null ? "" : "Pots " + Extra.count(mc, Items.SPLASH_POTION));
        text("gapcount", "Gapple Count", "[BoxPvP] Golden apples in your inventory", Category.PVP, 0.008f, 0.6f, mc -> mc.player == null ? "" : "Gapples " + Extra.count(mc, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE));
        text("pearlcount", "Pearl Count", "[BoxPvP] Ender pearls in your inventory", Category.PVP, 0.008f, 0.65f, mc -> mc.player == null ? "" : "Pearls " + Extra.count(mc, Extra.PEARL));
        text("xpbottles", "XP Bottles", "[SkyBlock / SMP] Bottles o' enchanting in your inventory", Category.INFO, 0.9f, 0.51f, mc -> mc.player == null ? "" : "XP bottles " + Extra.count(mc, Items.EXPERIENCE_BOTTLE));
        text("handcount", "Held Item Count", "[SkyBlock] How many of the item in your hand you carry in total", Category.INFO, 0.42f, 0.84f, mc -> {
            if (mc.player == null) return "";
            var st = mc.player.getMainHandItem();
            if (st.isEmpty()) return "";
            return st.getHoverName().getString() + " x" + Extra.count(mc, st.getItem());
        });
        text("armorwarn", "Armor Warning", "[BoxPvP] Warns when a piece of armor is almost broken", Category.PVP, 0.42f, 0.3f, mc -> {
            if (mc.player == null) return "";
            StringBuilder b = new StringBuilder();
            for (var st : new net.minecraft.world.item.ItemStack[] {mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD), mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST), mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS), mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET)}) {
                if (st.isEmpty() || !st.isDamageableItem()) continue;
                int left = st.getMaxDamage() - st.getDamageValue();
                if (left * 10 < st.getMaxDamage()) b.append(st.getHoverName().getString()).append(" low (").append(left).append(")\n");
            }
            return b.length() == 0 ? "" : "! " + b.substring(0, b.length() - 1);
        });
        ALL.add(new Utility.NoFovEffects());
        ALL.add(new Utility.NoScreenEffects());
        ALL.add(new Utility.NoBobbing());
        ALL.add(new CrystalCounterModule());
        ALL.add(new TargetHud());
        ALL.add(new ItemsHud());
        ALL.add(new InventoryHud());
        ALL.add(new Overlays.Crosshair());
        ALL.add(new Overlays.LowHealth());
        ALL.add(new Utility.Fullbright());
        ALL.add(new Utility.NoClouds());
        ALL.add(new Utility.FpsBoost());
        ALL.add(new Utility.ToggleSprint());
        ALL.add(new Utility.Zoom());
        ALL.add(new Utility.Fov());
        for (Module m : ALL) m.load();
    }

    public static Module byId(String id) {
        for (Module m : ALL) if (m.id.equals(id)) return m;
        return null;
    }

    public static void tick() {
        for (Module m : ALL) if (m.enabled) m.onTick();
        Hits.tick(Minecraft.getInstance());
        Extra.tick(Minecraft.getInstance());
    }
}
