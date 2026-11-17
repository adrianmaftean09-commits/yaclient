package dev.yaclient.modules;

import dev.yaclient.Gfx;
import dev.yaclient.Theme;
import dev.yaclient.Ui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

import java.util.Locale;

/** Name and health bar of the enemy you are fighting. */
public class TargetHudModule extends HudModule {
    public TargetHudModule() { super("target_hud", Category.PVP, false, 0.40f, 0.70f); }

    @Override
    public int width() { return 124; }

    @Override
    public int height() { return 34; }

    private EntityLivingBase current() {
        if (mc == null || mc.thePlayer == null) return null;
        MovingObjectPosition m = mc.objectMouseOver;
        if (m != null && m.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && m.entityHit instanceof EntityLivingBase) {
            HitTracker.aimed((EntityLivingBase) m.entityHit);
        }
        return HitTracker.target();
    }

    @Override
    protected void draw() {
        EntityLivingBase t = current();
        String name;
        float hp, max;
        if (t != null) { name = t.getName(); hp = t.getHealth(); max = Math.max(1f, t.getMaxHealth()); }
        else if (preview) { name = "Player"; hp = 14f; max = 20f; }
        else return;
        Gfx.rr(0, 0, 124, 34, 8, Theme.HUD_BG);
        Gfx.rrOutline(0, 0, 124, 34, 8, 1, 0x1AFFFFFF);
        Gfx.text(Gfx.trim(name, 11, true, 104), 10, 6, 11, true, Theme.TEXT);
        float f = Ui.clamp(hp / max, 0f, 1f);
        Gfx.rr(10, 22, 104, 6, 3, 0x33FFFFFF);
        int col = Ui.mix(0xFFFF3B30, 0xFF34D27B, f);
        if (f > 0.02f) Gfx.rr(10, 22, Math.max(6, Math.round(104 * f)), 6, 3, col);
        String num = String.format(Locale.ROOT, "%.1f", hp / 2f);
        Gfx.text(num, 114 - Gfx.width(num, 9, false), 7, 9, false, Theme.MUTED);
    }
}
