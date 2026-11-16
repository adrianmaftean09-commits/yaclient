package dev.yaclient.modules;

import org.lwjgl.input.Mouse;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsModule extends PillModule {
    private final Deque<Long> left = new ArrayDeque<Long>();
    private final Deque<Long> right = new ArrayDeque<Long>();
    private boolean lastL, lastR;

    public CpsModule() { super("cps", Category.PVP, true, 0.008f, 0.064f); }

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
    }

    @Override
    protected String text() { return count(left) + " | " + count(right) + " CPS"; }
}
