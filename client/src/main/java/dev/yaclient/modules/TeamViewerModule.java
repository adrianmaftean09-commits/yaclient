package dev.yaclient.modules;

import dev.yaclient.FriendsApi;
import dev.yaclient.Gfx;
import dev.yaclient.Setting;
import dev.yaclient.Theme;
import dev.yaclient.Ui;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Your teammates at a glance: health bar, hearts and distance. Teammates = same scoreboard team, your online YA friends, and names you list. */
public class TeamViewerModule extends HudModule {
    private final Setting.Bool team = add(new Setting.Bool("team", true));
    private final Setting.Bool friends = add(new Setting.Bool("friends", true));
    private final Setting.Text names = add(new Setting.Text("names", "", 80));
    private final Setting.Num range = add(new Setting.Num("range", 16f, 256f, 16f, 128f, ""));

    public TeamViewerModule() { super("team_viewer", Category.INFO, false, 0.008f, 0.45f); }

    private final List<EntityPlayer> rows = new ArrayList<EntityPlayer>();
    private long built;

    private List<EntityPlayer> members() {
        long now = System.currentTimeMillis();
        if (now - built < 250) return rows;
        built = now;
        rows.clear();
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) return rows;
        Map<String, Boolean> manual = new LinkedHashMap<String, Boolean>();
        if (names.value != null) for (String s : names.value.split(",")) if (!s.trim().isEmpty()) manual.put(s.trim().toLowerCase(Locale.ROOT), true);
        if (friends.value) {
            try { for (FriendsApi.Friend f : FriendsApi.friends()) if (f.online && f.name != null) manual.put(f.name.toLowerCase(Locale.ROOT), true); } catch (Throwable ignored) { }
        }
        for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (p == mc.thePlayer || rows.size() >= 6) continue;
            if (mc.thePlayer.getDistanceToEntity(p) > range.value) continue;
            boolean mate = (team.value && mc.thePlayer.getTeam() != null && p.isOnSameTeam(mc.thePlayer))
                    || manual.containsKey(p.getName().toLowerCase(Locale.ROOT));
            if (mate) rows.add(p);
        }
        return rows;
    }

    @Override
    public int width() { return members().isEmpty() && !preview ? 0 : 124; }

    @Override
    public int height() { int n = members().size(); return (n == 0 ? 2 : n) * 22 + 6; }

    @Override
    protected void draw() {
        List<EntityPlayer> l = members();
        int n = l.isEmpty() ? 2 : l.size();
        Gfx.rr(0, 0, 124, n * 22 + 6, 8, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, 124, n * 22 + 6, 8, 1, 0x1AFFFFFF);
        for (int i = 0; i < n; i++) {
            int y = 5 + i * 22;
            String name; float hp, max, dist;
            if (l.isEmpty()) { name = i == 0 ? "Mate1" : "Mate2"; hp = i == 0 ? 20f : 9f; max = 20f; dist = i == 0 ? 6f : 23f; }
            else { EntityPlayer p = l.get(i); name = p.getName(); hp = p.getHealth() + p.getAbsorptionAmount(); max = Math.max(1f, p.getMaxHealth()); dist = mc.thePlayer.getDistanceToEntity(p); }
            Gfx.text(Gfx.trim(name, 9, true, 70), 8, y, 9, true, Theme.TEXT);
            String d = Math.round(dist) + "m";
            Gfx.text(d, 116 - Gfx.width(d, 8, false), y + 1, 8, false, Theme.MUTED);
            float f = Ui.clamp(hp / max, 0f, 1f);
            Gfx.rr(8, y + 12, 86, 4, 2, 0x33FFFFFF);
            if (f > 0.02f) Gfx.rr(8, y + 12, Math.max(4, Math.round(86 * f)), 4, 2, Ui.mix(0xFFFF3B30, 0xFF34D27B, f));
            String h = String.format(Locale.ROOT, "%.1f", hp / 2f);
            Gfx.text(h, 116 - Gfx.width(h, 8, false), y + 11, 8, false, Theme.MUTED);
        }
    }
}
