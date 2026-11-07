package com.xekka.visuals.config;

import com.google.gson.*;
import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.ModuleManager;
import com.xekka.visuals.core.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Loads/saves module+setting state to config/xekkavisuals.json, plus named presets. */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("xekkavisuals");
    private static final Path MAIN_FILE = CONFIG_DIR.resolve("xekkavisuals.json");
    private static final Path PRESETS_DIR = CONFIG_DIR.resolve("presets");

    private ConfigManager() {}

    public static void init() {
        try {
            Files.createDirectories(PRESETS_DIR);
        } catch (IOException ignored) {}
        load(MAIN_FILE);
    }

    public static void save() { save(MAIN_FILE); }

    private static void save(Path file) {
        JsonObject root = new JsonObject();
        for (Module m : ModuleManager.getModules()) {
            root.add(m.getName(), serializeModule(m));
        }
        try {
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[XekkaVisuals] Failed saving config: " + e.getMessage());
        }
    }

    private static JsonObject serializeModule(Module m) {
        JsonObject obj = new JsonObject();
        obj.addProperty("enabled", m.isEnabled());
        JsonObject settings = new JsonObject();
        for (Setting<?> s : m.getSettings()) {
            Object v = s.getValue();
            if (v instanceof Boolean b) settings.addProperty(s.getName(), b);
            else if (v instanceof Double d) settings.addProperty(s.getName(), d);
            else if (v instanceof Integer i) settings.addProperty(s.getName(), i);
            else if (v instanceof String str) settings.addProperty(s.getName(), str);
        }
        obj.add("settings", settings);
        return obj;
    }

    private static void load(Path file) {
        if (!Files.exists(file)) return;
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            applyRoot(root);
        } catch (Exception e) {
            System.err.println("[XekkaVisuals] Failed loading config, using defaults: " + e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applyRoot(JsonObject root) {
        for (Module m : ModuleManager.getModules()) {
            if (!root.has(m.getName())) continue;
            JsonObject obj = root.getAsJsonObject(m.getName());
            if (obj.has("enabled")) m.setEnabled(obj.get("enabled").getAsBoolean());
            if (!obj.has("settings")) continue;
            JsonObject settings = obj.getAsJsonObject("settings");
            for (Setting<?> s : m.getSettings()) {
                if (!settings.has(s.getName())) continue;
                JsonElement el = settings.get(s.getName());
                try {
                    if (s instanceof Setting.BoolSetting bs) bs.setValue(el.getAsBoolean());
                    else if (s instanceof Setting.NumberSetting ns) ns.setValue(el.getAsDouble());
                    else if (s instanceof Setting.ModeSetting ms) ms.setValue(el.getAsString());
                    else if (s instanceof Setting.ColorSetting cs) cs.setValue(el.getAsInt());
                } catch (Exception ignored) {}
            }
        }
    }

    // ---- Presets ----
    public static List<String> listPresets() {
        List<String> names = new ArrayList<>();
        File:
        {
            var files = PRESETS_DIR.toFile().listFiles((d, n) -> n.endsWith(".json"));
            if (files == null) break File;
            for (var f : files) names.add(f.getName().substring(0, f.getName().length() - 5));
        }
        Collections.sort(names);
        return names;
    }

    public static void savePreset(String name) { save(presetPath(name)); }

    public static void loadPreset(String name) { load(presetPath(name)); }

    public static boolean deletePreset(String name) {
        try { return Files.deleteIfExists(presetPath(name)); } catch (IOException e) { return false; }
    }

    private static Path presetPath(String name) {
        String safe = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return PRESETS_DIR.resolve(safe + ".json");
    }
}
