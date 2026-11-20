package dev.yaclient.modules;

import dev.yaclient.FriendsApi;
import dev.yaclient.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.weavemc.api.event.EventBus;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** A green arrow above your teammates' heads. Teammates = same scoreboard team, your online YA friends, and names you list. */
public class TeamViewerModule extends Module {
    private final Setting.Bool team = add(new Setting.Bool("team", true));
    private final Setting.Bool friends = add(new Setting.Bool("friends", true));
    private final Setting.Text names = add(new Setting.Text("names", "", 80));
    private final Setting.Num range = add(new Setting.Num("range", 16f, 256f, 16f, 128f, ""));

    private static TeamViewerModule inst;
    private static Method pt;
    private static boolean ptTried;

    public TeamViewerModule() { super("team_viewer", Category.INFO, false); inst = this; }

    /** Green arrow above each teammate's head (world render event, looked up by name). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        String[] names = {"net.weavemc.api.event.RenderWorldEvent", "net.weavemc.api.event.RenderWorldEvent$Post"};
        for (String n : names) {
            try {
                Class c = Class.forName(n);
                EventBus.subscribe(c, (Consumer) new Consumer<Object>() {
                    @Override public void accept(Object e) { if (inst != null) inst.drawArrows(e); }
                });
                return;
            } catch (Throwable ignored) { }
        }
    }

    private static float partial(Object e) {
        try {
            if (!ptTried) { ptTried = true; pt = e.getClass().getMethod("getPartialTicks"); }
            if (pt != null) return ((Number) pt.invoke(e)).floatValue();
        } catch (Throwable ignored) { }
        return 1f;
    }

    private void drawArrows(Object event) {
        if (!enabled) return;
        Minecraft m = Minecraft.getMinecraft();
        if (m == null || m.theWorld == null || m.thePlayer == null || m.gameSettings.hideGUI) return;
        Entity v = m.getRenderViewEntity();
        if (v == null) return;
        List<EntityPlayer> l = members();
        if (l.isEmpty()) return;
        try {
            float pt = partial(event);
            double vx = v.lastTickPosX + (v.posX - v.lastTickPosX) * pt;
            double vy = v.lastTickPosY + (v.posY - v.lastTickPosY) * pt;
            double vz = v.lastTickPosZ + (v.posZ - v.lastTickPosZ) * pt;
            float yaw = m.getRenderManager().playerViewY, pitch = m.getRenderManager().playerViewX;
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            for (EntityPlayer p : l) {
                if (p.isInvisible() || p.isDead) continue;
                double x = p.lastTickPosX + (p.posX - p.lastTickPosX) * pt - vx;
                double y = p.lastTickPosY + (p.posY - p.lastTickPosY) * pt - vy + p.height + 0.75 + Math.sin(System.currentTimeMillis() / 260.0) * 0.06;
                double z = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * pt - vz;
                float dist = (float) Math.sqrt(x * x + y * y + z * z);
                float sc = 0.42f * Math.max(1f, dist / 8f);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                GlStateManager.rotate(-yaw, 0f, 1f, 0f);
                GlStateManager.rotate(pitch, 1f, 0f, 0f);
                GlStateManager.scale(sc, sc, sc);
                downArrow(0x34D27B, 235);
                GlStateManager.popMatrix();
            }
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        } catch (Throwable ignored) { }
    }

    /** A plain arrow pointing down: shaft on top, head below. Unit is about one block wide. */
    private static void downArrow(int rgb, int a) {
        int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        // dark outline (slightly larger), then the green shape
        for (int pass = 0; pass < 2; pass++) {
            float o = pass == 0 ? 0.08f : 0f;
            int cr = pass == 0 ? 0 : r, cg = pass == 0 ? 0 : g, cb = pass == 0 ? 0 : b, ca = pass == 0 ? a / 2 : a;
            w.begin(7, DefaultVertexFormats.POSITION_COLOR);
            w.pos(-0.16 - o, 1.0 + o, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(-0.16 - o, 0.45, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(0.16 + o, 0.45, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(0.16 + o, 1.0 + o, 0).color(cr, cg, cb, ca).endVertex();
            t.draw();
            w.begin(4, DefaultVertexFormats.POSITION_COLOR);
            w.pos(-0.42 - o, 0.45, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(0, -0.05 - o, 0).color(cr, cg, cb, ca).endVertex();
            w.pos(0.42 + o, 0.45, 0).color(cr, cg, cb, ca).endVertex();
            t.draw();
        }
    }

    private final List<EntityPlayer> rows = new ArrayList<EntityPlayer>();
    private long built;

    /** Same scoreboard team: the same team object, the same team name, or the same colored prefix (servers that build teams per viewer). */
    private boolean sameTeam(EntityPlayer p) {
        try {
            net.minecraft.scoreboard.Team a = mc.thePlayer.getTeam(), b = p.getTeam();
            if (a == null || b == null) return false;
            if (a == b || a.getRegisteredName().equals(b.getRegisteredName())) return true;
            if (a instanceof net.minecraft.scoreboard.ScorePlayerTeam && b instanceof net.minecraft.scoreboard.ScorePlayerTeam) {
                String pa = ((net.minecraft.scoreboard.ScorePlayerTeam) a).getColorPrefix(), pb = ((net.minecraft.scoreboard.ScorePlayerTeam) b).getColorPrefix();
                return pa != null && !pa.trim().isEmpty() && pa.equals(pb);
            }
        } catch (Throwable ignored) { }
        return false;
    }

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
            boolean mate = (team.value && sameTeam(p))
                    || manual.containsKey(p.getName().toLowerCase(Locale.ROOT));
            if (mate) rows.add(p);
        }
        return rows;
    }
}
