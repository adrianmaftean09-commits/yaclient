package dev.yaclient;

/** A configurable option of a module. Values are persisted in the config file. */
public abstract class Setting {
    public final String id;
    /** Id of the module that owns this setting (set by Module.add). */
    public String owner = "";

    protected Setting(String id) { this.id = id; }

    protected String key() { return "set." + owner + "." + id; }

    /** Localized label: "set.<module>.<id>" first, then the shared "set.<id>". */
    public String label() {
        String specific = "set." + owner + "." + id;
        return Lang.has(specific) ? Lang.t(specific) : Lang.t("set." + id);
    }

    public abstract void load();

    public abstract void save();

    public static final class Bool extends Setting {
        public boolean value;
        private final boolean def;

        public Bool(String id, boolean def) { super(id); this.def = def; this.value = def; }

        @Override public void load() { value = Config.getBool(key(), def); }

        @Override public void save() { Config.setBool(key(), value); }
    }

    public static final class Num extends Setting {
        public float value;
        public final float min, max, step, def;
        /** Optional suffix shown after the value, e.g. "%" or "x". */
        public final String unit;

        public Num(String id, float min, float max, float step, float def, String unit) {
            super(id);
            this.min = min; this.max = max; this.step = step; this.def = def; this.unit = unit;
            this.value = def;
        }

        public void setFraction(float f) {
            float v = min + Ui.clamp(f, 0f, 1f) * (max - min);
            if (step > 0f) v = Math.round(v / step) * step;
            value = Ui.clamp(v, min, max);
        }

        public float fraction() { return max == min ? 0f : (value - min) / (max - min); }

        public String text() {
            String n = (step >= 1f) ? String.valueOf(Math.round(value)) : String.format(java.util.Locale.ROOT, "%.1f", value);
            return n + unit;
        }

        @Override public void load() { value = Ui.clamp(Config.getFloat(key(), def), min, max); }

        @Override public void save() { Config.setFloat(key(), value); }
    }

    public static final class Choice extends Setting {
        public int index;
        public final String[] options; // option ids; label key is "opt.<id>"
        private final int def;

        public Choice(String id, int def, String... options) {
            super(id);
            this.options = options; this.def = def; this.index = def;
        }

        public String current() { return options[Ui.clamp(index, 0, options.length - 1)]; }

        @Override public void load() { index = Ui.clamp(Config.getInt(key(), def), 0, options.length - 1); }

        @Override public void save() { Config.setInt(key(), index); }
    }
}
