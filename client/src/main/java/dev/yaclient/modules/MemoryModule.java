package dev.yaclient.modules;

public class MemoryModule extends PillModule {
    public MemoryModule() { super("memory", Category.INFO, false, 0.008f, 0.22f); }

    @Override
    protected String text() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        return "RAM " + used + "/" + max + " MB";
    }
}
