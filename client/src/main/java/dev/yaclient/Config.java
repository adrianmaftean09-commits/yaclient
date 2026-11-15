package dev.yaclient;

import dev.yaclient.modules.Module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

/** Salva quali moduli sono attivi, cosi' restano uguali al prossimo avvio. */
public final class Config {
    private Config() {}

    private static File file() {
        return new File(System.getProperty("user.home"), ".yaclient" + File.separator + "modules.properties");
    }

    public static void load() {
        try {
            File f = file();
            if (!f.exists()) return;
            Properties p = new Properties();
            Reader r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            try { p.load(r); } finally { r.close(); }
            for (Module m : YAClient.MODULES) {
                String v = p.getProperty(m.name);
                if (v != null) m.enabled = Boolean.parseBoolean(v);
            }
        } catch (Exception ignored) { }
    }

    public static void save() {
        try {
            File f = file();
            f.getParentFile().mkdirs();
            Properties p = new Properties();
            for (Module m : YAClient.MODULES) p.setProperty(m.name, String.valueOf(m.enabled));
            Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            try { p.store(w, "YA CLIENT"); } finally { w.close(); }
        } catch (Exception ignored) { }
    }
}
