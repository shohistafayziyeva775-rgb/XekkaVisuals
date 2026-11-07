package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;

import java.util.LinkedHashMap;
import java.util.Map;

/** Holds enable + x/y for each draggable HUD panel. Positions are plain NumberSettings so they
 *  ride along with the existing ConfigManager save/load and preset system for free. */
public class HudElementsModule extends Module {
    public static class Element {
        public final String id, display;
        public final Setting.BoolSetting enabled;
        public final Setting.NumberSetting x, y;
        Element(Module owner, String id, String display, int defX, int defY) {
            this.id = id; this.display = display;
            this.enabled = owner.register(new Setting.BoolSetting(display + " Enabled", false));
            this.x = owner.register(new Setting.NumberSetting(display + " X", defX, 0, 4000, 1));
            this.y = owner.register(new Setting.NumberSetting(display + " Y", defY, 0, 4000, 1));
        }
    }

    public final Map<String, Element> elements = new LinkedHashMap<>();

    public HudElementsModule() {
        super("HUD Editor", Category.MISC, true);
        add("armor", "Armor HUD", 4, 30);
        add("potions", "Potion HUD", 4, 60);
        add("coords", "Coordinates HUD", 4, 90);
        add("perf", "FPS/Ping/CPS HUD", 4, 120);
        add("keystrokes", "Keystrokes HUD", 4, 150);
    }

    private void add(String id, String display, int defX, int defY) {
        elements.put(id, new Element(this, id, display, defX, defY));
    }

    public Element get(String id) { return elements.get(id); }
}
