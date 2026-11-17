package dev.yaclient.fabric;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

/** Combat statistics shared by the combo, reach and totem pop modules. */
public final class Hits {
    public static int combo;
    public static double reach;
    private static int pops, lastTotems = -1, lastHurt;

    private Hits() {}

    public static void init() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide()) { combo++; reach = player.distanceTo(entity); }
            return InteractionResult.PASS;
        });
    }

    public static int pops(Minecraft mc) { return pops; }

    public static void tick(Minecraft mc) {
        Player p = mc.player;
        if (p == null) { lastTotems = -1; return; }
        if (p.hurtTime > 0 && lastHurt == 0) combo = 0;
        lastHurt = p.hurtTime;
        int n = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            var s = p.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(Items.TOTEM_OF_UNDYING)) n += s.getCount();
        }
        if (lastTotems >= 0 && n < lastTotems && p.hurtTime > 0) pops += lastTotems - n;
        lastTotems = n;
    }
}
