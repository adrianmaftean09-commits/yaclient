package dev.yaclient.modules;

import dev.yaclient.Hud;
import net.minecraft.util.MovingObjectPosition;

import java.util.Locale;

/** Mostra la distanza dal giocatore/entita' che stai mirando (solo informazione). */
public class ReachModule extends Module {
    private double last;
    private long lastTime;

    public ReachModule() { super("Reach", false); }

    @Override
    public void onRender() {
        if (mc.thePlayer == null) return;
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mop.hitVec != null) {
            last = mop.hitVec.distanceTo(mc.thePlayer.getPositionEyes(1.0F));
            lastTime = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - lastTime < 1500) {
            Hud.right(String.format(Locale.US, "Reach %.2f", last));
        } else {
            Hud.right("Reach --");
        }
    }
}
