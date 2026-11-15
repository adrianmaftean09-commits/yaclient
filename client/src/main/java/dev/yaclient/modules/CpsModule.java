package dev.yaclient.modules;

import dev.yaclient.Hud;
import org.lwjgl.input.Mouse;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsModule extends Module {
    private final Deque<Long> left = new ArrayDeque<Long>();
    private final Deque<Long> right = new ArrayDeque<Long>();
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
        lastL = l;
        lastR = r;
        Hud.left(count(left) + " | " + count(right) + " CPS");
    }
}
