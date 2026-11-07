package com.xekka.visuals.modules;

import com.xekka.visuals.core.Module;
import com.xekka.visuals.core.Setting;

import java.util.List;

/**
 * Client-side emote state machine. Playing an emote drives the player's arm/body pose via
 * simple procedural offsets read by {@link com.xekka.visuals.gui.EmoteWheelScreen} consumers and
 * by the (optional) rendering layer; no animation file format is required because every emote
 * here is a short parametric motion (wave, bow, point, etc.) computed from elapsed ticks.
 */
public class EmoteModule extends Module {
    public enum Emote {
        WAVE("Wave", 30), BOW("Bow", 40), POINT("Point", 24), SIT("Sit", 999999),
        DANCE("Dance", 60), CLAP("Clap", 28);

        public final String display;
        public final int durationTicks;
        Emote(String display, int durationTicks) { this.display = display; this.durationTicks = durationTicks; }
    }

    public final Setting.BoolSetting cancelOnMove = register(new Setting.BoolSetting("Cancel On Move", true));
    public final Setting.BoolSetting cancelOnDamage = register(new Setting.BoolSetting("Cancel On Damage", true));
    public final Setting.NumberSetting wheelScale = register(new Setting.NumberSetting("Wheel Scale", 1.0, 0.6, 1.6, 0.05));
    public final Setting.BoolSetting showNameplate = register(new Setting.BoolSetting("Show Emote Nameplate", true));
    public final Setting.ModeSetting wheelKey = register(new Setting.ModeSetting(
            "Wheel Key", "B", List.of("B", "G", "N", "Right Alt")));

    private Emote current = null;
    private int ticks = 0;

    public EmoteModule() { super("Emotes", Category.COSMETICS, true); }

    public void play(Emote emote) {
        current = emote;
        ticks = 0;
    }

    public void cancel() { current = null; ticks = 0; }

    @Override
    public void onTick() {
        if (current == null) return;
        ticks++;
        if (current != Emote.SIT && ticks >= current.durationTicks) {
            current = null;
            ticks = 0;
        }
    }

    public boolean isPlaying() { return current != null; }
    public Emote getCurrent() { return current; }
    public int getTicks() { return ticks; }

    /** 0..1 progress for finite emotes, 0 for the looping SIT emote. */
    public float progress() {
        if (current == null || current == Emote.SIT) return 0f;
        return Math.min(1f, ticks / (float) current.durationTicks);
    }

    /** Simple procedural right-arm pitch offset in degrees for the current emote/tick, usable by
     *  a future FirstPersonRenderer/PlayerEntityModel mixin to actually bend the arm. */
    public float armPitchOffset() {
        if (current == null) return 0f;
        float t = ticks / 20f; // seconds
        return switch (current) {
            case WAVE -> (float) (30 + 20 * Math.sin(t * 10));
            case BOW -> (float) Math.min(40, ticks * 2.0);
            case CLAP -> (float) (20 * Math.abs(Math.sin(t * 14)));
            default -> 0f;
        };
    }
}
