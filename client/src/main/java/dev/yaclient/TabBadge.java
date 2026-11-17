package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;

/** Marks every YA CLIENT player in the tab list with a small "YA" logo (white Y, red A) next to the name. */
final class TabBadge {
    private TabBadge() {}

    private static final String PREFIX = "\u00a7f\u00a7lY\u00a7c\u00a7lA\u00a7r ";
    private static int ticks;

    static void tick() {
        if (++ticks % 10 != 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;
        NetHandlerPlayClient net = mc.getNetHandler();
        if (net == null) return;
        boolean on = Config.getBool("tab.badge", true);
        for (NetworkPlayerInfo info : net.getPlayerInfoMap()) {
            if (info == null || info.getGameProfile() == null || info.getGameProfile().getId() == null) continue;
            String cur = info.getDisplayName() != null ? info.getDisplayName().getFormattedText() : null;
            boolean has = cur != null && cur.startsWith(PREFIX);
            boolean want = on && CosmeticsApi.usesClient(info.getGameProfile().getId());
            if (want && !has) {
                String base = cur != null ? cur : ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(), info.getGameProfile().getName());
                info.setDisplayName(new ChatComponentText(PREFIX + base));
            } else if (!want && has) {
                info.setDisplayName(null);
            }
        }
    }
}
