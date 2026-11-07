package com.xekka.visuals.core;

import java.util.List;
import java.util.function.Supplier;

/** Base class for a configurable module setting. */
public abstract class Setting<T> {
    private final String name;
    protected T value;
    private final T defaultValue;
    private Supplier<Boolean> visiblePredicate = () -> true;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public T getValue() { return value; }
    public void setValue(T value) { this.value = value; }
    public T getDefault() { return defaultValue; }
    public void reset() { this.value = defaultValue; }

    /** Allows a setting row to be hidden unless another setting (e.g. a mode) matches. */
    public Setting<T> visibleIf(Supplier<Boolean> predicate) {
        this.visiblePredicate = predicate;
        return this;
    }
    public boolean isVisible() { return visiblePredicate.get(); }

    public static class BoolSetting extends Setting<Boolean> {
        public BoolSetting(String name, boolean def) { super(name, def); }
        public void toggle() { value = !value; }
    }

    public static class NumberSetting extends Setting<Double> {
        public final double min, max, step;
        public NumberSetting(String name, double def, double min, double max, double step) {
            super(name, def);
            this.min = min; this.max = max; this.step = step;
        }
        @Override
        public void setValue(Double v) { super.setValue(Math.max(min, Math.min(max, v))); }
    }

    public static class ModeSetting extends Setting<String> {
        public final List<String> options;
        public ModeSetting(String name, String def, List<String> options) {
            super(name, def);
            this.options = options;
        }
        public void cycle(int dir) {
            int idx = options.indexOf(value);
            idx = (idx + dir + options.size()) % options.size();
            value = options.get(idx);
        }
    }

    /** ARGB packed color. */
    public static class ColorSetting extends Setting<Integer> {
        public ColorSetting(String name, int argbDefault) { super(name, argbDefault); }
    }
}
