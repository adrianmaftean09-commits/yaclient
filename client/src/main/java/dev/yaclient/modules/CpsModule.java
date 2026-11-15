package dev.yaclient.modules;

import dev.yaclient.YAClient;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsModule extends Module {
    private final Deque<Long> left = new ArrayDeque<>();
    private final Deque<Long> right = new ArrayDeque<>();
    private boolean lastL, lastR;

    public CpsModule() { super("CPS", true); }

    private static int count(Deque<Long> d) {
        long now = System.currentTimeMillis();
        while (!d.isEmpty() && now - d.peekFirst() > 1000) d.pollFirst();
        return d.size();
    }

    @Override
    public void onRender() {
        boolean l = Mouse.isButtonDown(0), r = Mouse.isButtonDown(1);
        if (l && !lastL) left.addLast(System.currentTimeMillis());
        if (r && !lastR) right.addLast(System.currentTimeMillis());
        lastL = l; lastR = r;

        String t = count(left) + " | " + count(right) + " CPS";
        int w = mc.fontRendererObj.getStringWidth(t);
        Gui.drawRect(4, 20, 10 + w, 32, YAClient.BLACK_BG);
        Gui.drawRect(4, 20, 6, 32, YAClient.RED);
        mc.fontRendererObj.drawStringWithShadow(t, 8, 22, YAClient.WHITE);
    }
}
