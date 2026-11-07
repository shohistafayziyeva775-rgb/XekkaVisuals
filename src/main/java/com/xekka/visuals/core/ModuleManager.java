package com.xekka.visuals.core;

import com.xekka.visuals.modules.*;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleManager() {}

    public static void init() {
        register(new CrosshairModule());
        register(new FullBrightModule());
        register(new JumpCircleModule());
        register(new TargetEspModule());
        register(new BlockOverlayModule());
        register(new HitColorModule());
        register(new AspectRatioModule());
        register(new HitBubbleModule());
        register(new WatermarkModule());
        register(new CosmeticsModule());
        register(new EmoteModule());
        register(new ThemeEditorModule());
        register(new HudElementsModule());
        register(new EspModule());
        register(new NametagsModule());
        register(new TrailsModule());
        register(new ParticlesModule());
        register(new HitMarkerModule());
        register(new DamageIndicatorModule());
        register(new ZoomModule());
        register(new FovChangerModule());
        register(new NoOverlaysModule());
        register(new TimeWeatherModule());
    }

    private static void register(Module m) { MODULES.add(m); }

    public static List<Module> getModules() { return MODULES; }

    public static List<Module> getByCategory(Module.Category category) {
        List<Module> out = new ArrayList<>();
        for (Module m : MODULES) if (m.getCategory() == category) out.add(m);
        return out;
    }

    public static Module getByName(String name) {
        for (Module m : MODULES) if (m.getName().equalsIgnoreCase(name)) return m;
        return null;
    }

    public static void tickAll() {
        for (Module m : MODULES) if (m.isEnabled()) m.onTick();
    }
}
