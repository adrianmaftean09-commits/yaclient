package dev.yaclient.fabric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

/** Small key/value config in ~/.yaclient/fabric.properties (separate from the 1.8.9 client config). */
public final class Config {
    private static final Properties P = new Properties();

    private Config() {}

    private static File file() { return new File(System.getProperty("user.home"), ".yaclient" + File.separator + "fabric.properties"); }

    public static synchronized void load() {
        try {
            File f = file();
            if (!f.exists()) return;
            try (Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8")) { P.load(r); }
        } catch (Exception ignored) { }
    }

    public static synchronized void save() {
        try {
            File f = file();
            f.getParentFile().mkdirs();
            try (Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8")) { P.store(w, "YA CLIENT (Fabric)"); }
        } catch (Exception ignored) { }
    }

    public static synchronized boolean has(String key) { return P.containsKey(key); }

    public static synchronized boolean hasPrefix(String prefix) {
        for (Object k : P.keySet()) if (String.valueOf(k).startsWith(prefix)) return true;
        return false;
    }

    public static synchronized String get(String key, String def) { return P.getProperty(key, def); }

    public static synchronized void set(String key, String value) { P.setProperty(key, value); }

    public static boolean getBool(String key, boolean def) { String v = get(key, null); return v == null ? def : Boolean.parseBoolean(v); }

    public static void setBool(String key, boolean v) { set(key, String.valueOf(v)); }

    public static float getFloat(String key, float def) {
        try { String v = get(key, null); return v == null ? def : Float.parseFloat(v); } catch (NumberFormatException e) { return def; }
    }

    public static void setFloat(String key, float v) { set(key, String.valueOf(v)); }
}
