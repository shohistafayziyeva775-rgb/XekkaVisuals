package com.xekka.visuals.core;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final Category category;
    private boolean enabled;
    protected final List<Setting<?>> settings = new ArrayList<>();
    private int keyCode = -1; // GLFW key, -1 = unbound

    protected Module(String name, Category category, boolean defaultEnabled) {
        this.name = name;
        this.category = category;
        this.enabled = defaultEnabled;
    }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public List<Setting<?>> getSettings() { return settings; }
    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }

    protected <T extends Setting<?>> T register(T setting) {
        settings.add(setting);
        return setting;
    }

    public final void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (value) onEnable(); else onDisable();
    }
    public final void toggle() { setEnabled(!enabled); }

    /** Called every client tick while enabled. Override as needed. */
    public void onTick() {}

    protected void onEnable() {}
    protected void onDisable() {}

    public enum Category {
        VISUALS("Visuals"),
        COMBAT_ESP("Combat & ESP"),
        PLAYERS("Players"),
        COSMETICS("Cosmetics"),
        MISC("Misc");

        public final String display;
        Category(String display) { this.display = display; }
    }
}
