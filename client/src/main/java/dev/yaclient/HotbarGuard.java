package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Built-in hotbar protection (not a module, always on).
 *
 * Slow switch: the hotbar moves at most one slot per 2 ticks, so a fast flick of the mouse wheel cannot jump over the
 * item you wanted.
 * Item switch prevention: while you are using an item (eating, drawing a bow, blocking with a sword) the hotbar does
 * not change, so a stray scroll cannot make you drop the sword or cancel the bow.
 *
 * It runs at the end of every client tick, before the game tells the server about a new slot, so the server never
 * sees the blocked switch.
 */
public final class HotbarGuard {
    private static int prev = -1;
    private static int tick;
    private static int lastChange = -100;

    private HotbarGuard() {}

    public static void tick(Minecraft mc) {
        tick++;
        EntityPlayer p = mc == null ? null : mc.thePlayer;
        if (p == null || p.isDead || !Config.getBool("hotbar.guard", true)) { prev = -1; return; }
        int cur = p.inventory.currentItem;
        if (prev < 0 || mc.currentScreen != null || p.capabilities.isCreativeMode && mc.currentScreen != null) { prev = cur; return; }
        if (cur != prev) {
            boolean busy = p.isUsingItem();
            boolean tooFast = tick - lastChange < 2;
            if (busy || tooFast) {
                p.inventory.currentItem = prev;   // refuse the switch
                return;
            }
            // one slot at a time: a flick over several slots lands on the next one only
            int d = ((cur - prev) % 9 + 9) % 9;      // 0..8 steps forward
            int step = d <= 4 ? 1 : -1;
            if (d != 1 && d != 8) {
                // keyboard number keys jump on purpose; only wheel-like moves (2-3 slots) are clamped
                if (d <= 3 || d >= 6) { cur = ((prev + step) % 9 + 9) % 9; p.inventory.currentItem = cur; }
            }
            lastChange = tick;
        }
        prev = cur;
    }
}
