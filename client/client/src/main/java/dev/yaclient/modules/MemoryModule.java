package dev.yaclient.modules;

public class MemoryModule extends Module {
    public MemoryModule() { super("Memoria", false); }

    @Override
    public void onRender() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        boxRight(68, "RAM " + used + "/" + max + " MB");
    }
}
