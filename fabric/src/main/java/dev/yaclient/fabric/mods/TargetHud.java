package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;

/** Name and health of the entity you aim at (or the last one you hit for a few seconds). */
public class TargetHud extends HudModule {
    private LivingEntity last;
    private long seen;

    public TargetHud() { super("targethud", "Target HUD", "Health of the player you are aiming at", Category.CRYSTAL, false, 0.42f, 0.62f); }

    private LivingEntity target(Minecraft mc) {
        if (mc.crosshairPickEntity instanceof LivingEntity le) { last = le; seen = System.currentTimeMillis(); }
        if (last != null && (last.isRemoved() || System.currentTimeMillis() - seen > 3000)) last = null;
        return last;
    }

    @Override public int width(Minecraft mc) { return 110; }

    @Override public int height(Minecraft mc) { return 28; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        LivingEntity t = target(mc);
        if (t == null) return;
        g.fill(0, 0, width(mc), height(mc), 0xA0101014);
        g.text(mc.font, t.getName().getString(), 5, 4, 0xFFFFFFFF, true);
        float hp = t.getHealth(), max = Math.max(1f, t.getMaxHealth());
        float f = Math.max(0f, Math.min(1f, hp / max));
        g.fill(5, 17, 105, 22, 0xFF2A2A30);
        g.fill(5, 17, 5 + Math.round(100 * f), 22, f > 0.5f ? 0xFF55FF55 : f > 0.25f ? 0xFFFFFF55 : 0xFFFF5555);
        String s = String.format("%.1f", hp);
        g.text(mc.font, s, 105 - mc.font.width(s), 4, 0xFFB0B0B8, true);
    }
}
