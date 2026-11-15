package dev.yaclient.modules;

import dev.yaclient.Hud;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class PotionModule extends Module {
    public PotionModule() { super("Pozioni", false); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null) return;
        for (PotionEffect e : mc.thePlayer.getActivePotionEffects()) {
            Potion p = Potion.potionTypes[e.getPotionID()];
            if (p == null) continue;
            String n = I18n.format(p.getName(), new Object[0]);
            if (e.getAmplifier() > 0) n += " " + (e.getAmplifier() + 1);
            Hud.left(n + " " + Potion.getDurationString(e));
        }
    }
}
