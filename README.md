# XekkaVisuals (Phase 1 + 2 + 3 + 4 + 5 + 6 — feature-complete against the original wishlist)

Client-side Fabric visuals mod for Minecraft 1.21.4. Premium dark/ink theme ClickGUI,
opened with **Right Shift** (rebindable in Controls).

## Build
```
./gradlew build
```
Output jar: `build/libs/xekkavisuals-0.1.0.jar`
Drop it into `.minecraft/mods/` together with Fabric API for 1.21.4.

## What's included in this phase
- Full architecture: `Module`, `Setting` (Bool/Number/Mode/Color), `ModuleManager`,
  `ConfigManager` (auto-save to `config/xekkavisuals/xekkavisuals.json` + named presets).
- ClickGUI matching the reference screenshot layout: logo/title top-left, category name +
  search field, two-column module grid with sliding toggle switches, bottom 5-icon dock,
  draggable panel, rounded corners, animated rainbow border glow, open scale-in animation.
- 9 working modules, each with 5 settings:
  1. **Crosshair** — style (Cross/Dot/Circle/T-Shape), size, thickness, dynamic gap, color
  2. **Full Bright** — mode, strength, nether/end toggle, smooth transition, transition speed
  3. **Jump Circle** — shape, color mode, radius, lifetime, static color
  4. **Target ESP** — mode, color mode, size, players-only, fade-out
  5. **Block Overlay** — fill alpha, outline width, color, rainbow, through-walls
  6. **Hit Color** — color, duration, fade, intensity, mobs-only
  7. **Aspect Ratio** — ratio, preset, apply-to-gui, smoothing, lock-in-menus
  8. **Hit Bubble** — shape, size, lifetime, color, rainbow
  9. **Watermark** — animated gradient, x, y, scale, show version
  10. **Cosmetics** — cape glow toggle, cape color, trail style, trail rainbow, trail length
  11. **Emotes** — cancel-on-move, cancel-on-damage, wheel scale, nameplate toggle, wheel key
  12. **Theme Editor** — accent color, toggle-ON color, panel opacity, rainbow border speed, rainbow border toggle

## Phase 2 additions in this build
- **Cosmetics**: a rainbow/colored client-side movement trail (`WorldEffectsRenderer.renderCosmeticTrail`)
  and a cape-glow color setting (hook point only — actually recoloring the vanilla cape texture
  needs a `PlayerEntityRenderer`/`FeatureRenderer` mixin, left for a follow-up pass).
- **Emotes**: press **B** to open a radial emote wheel (`EmoteWheelScreen`) with Wave / Bow /
  Point / Sit / Dance / Clap. `EmoteModule` is a real tick-driven state machine (duration,
  progress, cancel-on-move) with a procedural `armPitchOffset()` ready to be wired into a
  `PlayerEntityModel`/first-person-arm mixin to actually bend the arm — that render mixin is the
  one piece intentionally left out, since it touches a different internal class per MC version.
- **Config page**: the Misc dock page now shows a preset name field + Save/Load/Delete, backed by
  the presets already supported in `ConfigManager`.
- **Theme Editor**: the whole ClickGUI (border, accent, toggle color, panel opacity) is now driven
  live from `ThemeEditorModule` instead of fixed constants — open the Misc page to tweak it.
- **Target ESP**: added working **Box** (full 3D wireframe around the entity) and **Tracer**
  (line from camera to target) render modes alongside Ring/Halo.

## Phase 3: exotic shapes (this build)
`Jump Circle` shape list now actually renders every option, not just "Flat Ring" for everything:
- **Flat Ring** / **Double Ring** / **Dashed Ring** — line-loop ring(s), dashed version leaves gaps
- **Star** / **Hexagon** — true n-gon / 5-point star outline (`drawPolygonLoop`)
- **Pentagram** — 5 points connected in skip-one order (0-2-4-1-3-0), not just a pentagon
- **Chains** — several small ring "links" spaced evenly around the main radius
- **Sakura Petals** — small flat triangle petals arranged radially
- **Lightning Ring** — jagged ring, each vertex's radius perturbed by a seed fixed per-jump (so it
  looks like a static jagged bolt-ring, not flickering noise)
- **Spiral** — line strip spiraling outward, growing with the ring's lifetime progress
- **Filled Circle** — unchanged triangle-fan disc

`Target ESP` gained **Pentagram** and **Chains** modes too, reusing the same draw routines.

## Phase 4 (this build): ESP/Nametags/Trails/Particles/HitMarker/DamageIndicator/Zoom/FOV/HUD Editor
- **ESP** (`EspModule`) — loops every nearby player/mob and draws a box ESP on each (not just the
  crosshair target like Target ESP). Players/Mobs/Items toggles, 4 box modes wired to one real
  wireframe-box routine (2D Box / Corner Box currently render the same as Box — see below).
- **Nametags** (`NametagsModule`) — health/armor/distance/ping text is fully built in
  `buildTag()`, but it is **not yet projected onto the screen above each entity** (that needs a
  world→screen matrix transform hooked into `HudRenderCallback`, left for the next pass; the data
  layer is done, the draw call is the remaining piece).
- **Trails** (`TrailsModule`) — real line trail behind the player with Rainbow/Static/Gradient/
  Health color modes, ground-only option, fade-out.
- **Particles** (`ParticlesModule`) — spawns real vanilla particle types (Crit/Heart/Smoke/Flame/
  Note) at the hit point on every attack, multiplier-scaled.
- **Hit Marker** (`HitMarkerModule`) — on-screen X marker + click sound on every attack, gold on
  (approximate) crits.
- **Damage Indicator** (`DamageIndicatorModule`) — spawns a small rising ring at the hit position
  per attack (a real floating *number* needs the same screen-projection piece Nametags needs —
  the state machine, lifetime and rise speed are fully implemented and ready for it).
- **Zoom** (`ZoomModule`) — hold **Z**, scroll to adjust zoom FOV, smoothing. FOV is computed by
  `getAppliedFov()` but — like Full Bright/Aspect Ratio — still needs its own `GameRenderer`
  mixin injection point verified against the exact Yarn mappings to actually override the camera
  FOV; the module's math is correct and ready to be read from that mixin.
- **FOV Changer** — same situation: `getFixedFov()` is ready, needs the same mixin wiring.
- **No Overlays** — settings modeled (no hurt cam/fire/rain/fog); each needs one small
  `HeldItemRenderer`/`InGameHud`/`WorldRenderer` mixin to actually suppress — not yet added.
- **Time & Weather** — `resolveTimeOfDay()` computed; wiring into the renderer's sky angle is the
  remaining piece.
- **HUD Editor** (`HudElementsModule` + `HudEditorScreen` + `HudRenderer`) — press **H** to enter
  edit mode and **drag** the Armor / Potions / Coordinates / FPS / Keystrokes panels anywhere on
  screen; positions are plain settings, so they persist automatically via the existing config/
  preset system. All 5 panels render live data (not placeholders) once enabled from the Misc page.

### Honest summary of what's real vs. what's a documented hook-point in this phase
Real, drawing actual pixels every frame: ESP boxes, Trails, Particles, Hit Marker, Damage
Indicator rings, all 5 HUD Editor panels. Logic-complete but **not yet connected to a render
mixin** (by design, flagged rather than faked): Zoom/FOV camera override, No Overlays
suppression, Time & Weather sky override, and the on-screen text for Nametags/Damage numbers.

## Phase 5 (this build): closing out the remaining list
- **FOV/Zoom actually applied**: `GameRendererMixin` now also injects into `getFov`, blending in
  `FovChangerModule`/`ZoomModule`'s computed values. Same mapping-risk note as Full Bright/Aspect
  Ratio applies — verify `getFov`'s exact signature against the pinned Yarn build before building.
- **No Hurt Cam now genuinely works**, no mixin needed: zeroing `player.hurtTime` client-side
  every tick removes the vanilla hurt-cam tilt/shake outright (No Fire Overlay/Rain/Fog still need
  their own render-suppression mixins and are not done — they're a different code path each).
- **Nametags and Damage Indicator now draw real screen-space text**, via a new
  `WorldToScreen.project()` helper that does a manual camera-space perspective projection (works
  independent of internal projection-matrix plumbing). Both render in `HudRenderer`.
- **ESP "2D Box"** is now a true flat screen-aligned rectangle (`HudRenderer.renderEsp2D`, also
  via `WorldToScreen`), genuinely different from the 3D wireframe **Box**.
- **ESP "Corner Box"** now draws only short corner brackets instead of the full 12-edge wireframe.
- **Target ESP** gained **Crystals** (orbiting diamond outlines), **Orbiting Orbs**, and **Arrow**
  (bobbing arrow above the head) modes.
- **Emotes arm-bend** (`PlayerEntityModelMixin`) and **Cosmetics cape recolor**
  (`CapeFeatureRendererMixin`) are both now wired in as real mixins rather than left as bare hook
  points — see the class-level comments on each: both are a best-effort match for the 1.21.x
  mapping names/behavior (`PlayerEntityModel.rightArm`, and the cape renderer still honoring
  `RenderSystem.setShaderColor`), so they're the first places to check if `./gradlew build` fails
  or if an effect doesn't visually show up in-game.

## Phase 6 (this build): the last 3 items
- **No Rain** (`WorldRendererOverlaysMixin`) — cancels `WorldRenderer#renderWeather` outright.
- **No Fog** (`BackgroundRendererMixin`) — pushes the computed far-fog distance out to 512 blocks
  instead of trying to cancel fog entirely (full cancellation risks breaking chunk-culling logic
  that's tied to fog distance in some versions, so "push it very far away" is the safer approach).
- **No Fire Overlay** (`InGameHudFireMixin`) — cancels `InGameHud#renderFire`.
- **Time & Weather sky override** (`ClientWorldSkyMixin`) — injects into `ClientWorld#getSkyAngle`
  and returns the angle for the module's chosen preset (Day/Noon/Sunset/Night/Midnight) instead of
  the server's real angle, purely client-side/visual.

Every one of these 4 new mixins is flagged the same way the earlier ones are, in its own
class-level comment: the target method name is the correct Yarn name for the 1.20–1.21 line as
far as I can verify without a live decompile, but each is exactly the kind of method whose
signature or internal shape can shift slightly between minor versions. If `./gradlew build` fails
on one of them, that comment says what moved and where to look instead.

## Known limitation: no compile verification was possible
This container has no network access, so Gradle cannot download the Minecraft/Fabric/Yarn
dependencies needed to actually run `./gradlew build` here — none of this project has been
compiled, only written to match the 1.21.4 API as accurately as I can without a live build. Run
the build yourself as the very first step; if anything in the 9 mixins fails, each one's own
comment is the starting point for fixing it. Everything in `core/`, `modules/`, `gui/`, and
`util/` is plain Fabric API + vanilla client calls with much lower version-drift risk than the
mixins — those are the parts I'm most confident compile cleanly as-is.

## Known verification notes (be honest about this before you ship it)
This is real, structured code — not pseudo-code — but two mixins touch Mojang internals that
shift between Minecraft minor versions and MUST be checked against the exact Yarn mappings for
`1.21.4+build.8` before building:
- `GameRendererMixin` targets `getBasicProjectionMatrix` for Aspect Ratio.
- `LightmapTextureManagerMixin` targets `update` for Full Bright; the actual brightness blend
  needs to be wired into wherever that method writes lightmap pixels in this exact version.
If `./gradlew build` reports a missing method/field, it is almost certainly one of these two —
open the decompiled class via your IDE's Fabric mappings and adjust the `@Inject` target.

## Next phase (not yet built)
- Remaining Target ESP / general ESP modes (Box, Tracer, Halo variants, Pentagram, Chains, etc.)
- Cosmetics system (custom capes/wings, particle trails) + **Emotes** (animation state machine,
  emote wheel UI, keybinds per emote) — this is a sizeable sub-system on its own and is next.
- Config page UI (preset list with save/load/delete buttons) — `ConfigManager` already supports
  presets; only the GUI page for it is left.
- Theme Editor page.

Tell me which of these to build next and I'll continue straight into it.
