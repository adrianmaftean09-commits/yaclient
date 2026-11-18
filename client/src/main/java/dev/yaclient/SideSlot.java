package dev.yaclient;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import org.lwjgl.input.Mouse;

/**
 * Built-in: with a sword in hand the side buttons of the mouse change the hotbar slot (back = previous, forward = next).
 * Not while eating / blocking / in a menu.
 */
public final class SideSlot {
    private static boolean back, fwd;

    private SideSlot() {}

    public static void tick(Minecraft mc) {
        boolean b, f;
        try { b = Mouse.isButtonDown(3); f = Mouse.isButtonDown(4); } catch (Throwable t) { return; }
        boolean nb = b && !back, nf = f && !fwd;
        back = b; fwd = f;
        if (!nb && !nf) return;
        EntityPlayer p = mc.thePlayer;
        if (p == null || p.isDead || mc.currentScreen != null || p.isUsingItem() || !Config.getBool("side.slots", true)) return;
        ItemStack s = p.getHeldItem();
        if (s == null || !(s.getItem() instanceof ItemSword)) return;
        int cur = p.inventory.currentItem;
        int next = ((cur + (nf ? 1 : -1)) % 9 + 9) % 9;
        p.inventory.currentItem = next;
        HotbarGuard.accept(next);
    }
}
