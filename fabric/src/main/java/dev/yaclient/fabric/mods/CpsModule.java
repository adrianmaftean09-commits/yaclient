package dev.yaclient.fabric.mods;

import dev.yaclient.fabric.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayDeque;

/** Clicks per second (left | right), counted from the attack and use keys. */
public class CpsModule extends HudModule {
    private final ArrayDeque<Long> left = new ArrayDeque<Long>(), right = new ArrayDeque<Long>();
    private boolean wasL, wasR;

    public CpsModule() { super("cps", "CPS", "Shows your clicks per second", Category.PVP, true, 0.008f, 0.064f); }

    private static int count(ArrayDeque<Long> q, long now) {
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
        return q.size();
    }

    private void sample(Minecraft mc) {
        long now = System.currentTimeMillis();
        boolean l = mc.options.keyAttack.isDown(), r = mc.options.keyUse.isDown();
        if (l && !wasL) left.addLast(now);
        if (r && !wasR) right.addLast(now);
        wasL = l;
        wasR = r;
    }

    private String text(Minecraft mc) {
        long now = System.currentTimeMillis();
        return count(left, now) + " | " + count(right, now) + " CPS";
    }

    @Override public int width(Minecraft mc) { return mc.font.width(text(mc)) + 10; }

    @Override public int height(Minecraft mc) { return 16; }

    @Override
    public void draw(GuiGraphicsExtractor g, Minecraft mc) {
        sample(mc);
        g.fill(0, 0, width(mc), height(mc), 0x90101014);
        g.text(mc.font, text(mc), 5, 4, 0xFFFFFFFF, true);
    }
}
