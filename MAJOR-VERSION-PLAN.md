# TouchGrass — Major version plan: an adaptive native touch UI for MC Java (Controlify concepts → TouchController)

_Status: 2026-11-12. Workspace branch `touchgrass` = TouchController PR #397 (the inventory-exit fix). Nothing here has been compiled — see §5._

## 0. The reported bug (fixed by adoption)

**Bug:** With touch controls active, the inventory / crafting table / chest (any vanilla `Screen`) can't be closed by touch — a physical keyboard (Esc/E) is required. Java `Screen`s (unlike Bedrock) have no on-screen close button, and TouchController drew no overlay button while a GUI was open.

**Root cause (evidence):** `Hud.kt` only renders in-world controls and suppresses them when `inGui`; `RenderEvents` clears the internal pointer map when `inGui`; `GameAction` had no way to close a screen. So no touch affordance existed over a vanilla screen.

**Fix:** adopt **TouchController PR #397 "Touch Compatible GUI Exit Menu Button"** (mergeable / CI-green). It adds `GameAction.closeScreen()` (→ `screen.onClose()`, correct for containers too), a new `IN_GUI` **layer condition**, a `close_screen` **game action** bound to a themed exit widget, reworks `GameStateProviderImpl` so `inGui` = "a *closeable* GUI is open" (`AbstractContainerScreen` / advancements / social), and renders the widget over the screen via a per-version `ScreenMixin` + the Combine canvas.
An independent stop-gap I wrote (same `closeScreen()` core) was validated by #397 and is stashed, not used.

## 1. The two codebases

- **TouchController (TC)** — the mod we evolve. Kotlin, **Bazel**, multi-loader (Fabric/Forge/NeoForge), MC 1.21.1–26.3+ across **8 per-version source sets**. Compose-style **Combine** GUI lib + Voyager. A **GAL** abstraction (`GameAction`, `GameState`, …). Its controller layout is data-driven: **layer conditions → layout layers → widgets → actions** (`BuiltinLayerCondition`, `BuiltinLayers`, `BuiltinWidgets`, `GameActions`, `ButtonTrigger`). Rendering via `RenderEvents` + `DrawQueue`. Per-version mixins under `versions/mixin/<ver>`. Marked "early development."
- **Controlify (CF)** v3.5.3 — the reference for **adaptive, screen-to-screen GUI operation**. Java, Gradle/Stonecutter, Fabric+NeoForge, 1.21.1+. Key packages: **`screenop`** (one `*ScreenProcessor` / `*ComponentProcessor` per screen type = navigable GUIs), **`virtualmouse`** (container cursor + snapping + quick-move/drop), **`contextual`** (state-based **button guide** = the adaptive on-screen prompts), on-screen keyboard, radial menu, multi-controller, data-driven via resource packs.

We **port concepts, not code**: CF is Java/Gradle, TC is Kotlin/Bazel.

## 2. The "adaptive / screen-to-screen" concepts to port

| Controlify concept | Where it lands in TouchController |
|---|---|
| Screen-specific navigation (`screenop`) | New `screenop`-style processors per screen type on top of `RenderEvents.onScreenRender` (PR #397 already hooks this). |
| Container cursor + snapping (`virtualmouse`) | A **VirtualMouse pointer** in TC's `PointerState` model: move to slot, tap=interact, long-press=drop, buttons for quick-move/split. |
| Adaptive button guide by state (`contextual`) | TC already has built-in **layer conditions** (flying/swimming/…). Generalize to **screen/context conditions** (container open, creative open, chest open) so layouts adapt screen-to-screen. |
| On-screen keyboard | TC has `InputManager` + Compose `ChatScreen` already; add **data-driven keyboard layouts** (resource-pack JSON, like CF) for text fields in any screen. |
| Multi-input (CF #913) | Enable **touch + controller simultaneously** (hybrid), relevant for Steam Deck / Android + gamepad. |
| Action/gesture pipeline (CF v4, #1002) | Unify touch gestures + keybinds + button widgets under one action system — TC already has the `GameActions` registry to build on. |

## 3. Target architecture (TC v-next)

Add an **Adaptive GUI / Screen Operations** subsystem in `common/`, mirroring CF `screenop` but expressed in TC's condition/widget/action model:

1. **Screen-type detection** generalizing `GameStateProviderImpl.inGui` → an enum/state: `NONE / CONTAINER / CREATIVE / SIGN / CHEST_TYPE / …`.
2. **Context conditions** built from that state, so `LayoutLayer`s auto-swap screen-to-screen (in-world layer ↔ inventory layer ↔ creative layer). This is TC's natural path to "adaptive."
3. **`screenop`-style processors** for the big screens (container, creative, sign, anvil, merchant, modded via API) describing their widgets, so navigation and the cursor know where to go.
4. **VirtualMouse** as a first-class pointer that works inside `AbstractContainerScreen`, with snapping + quick actions — Bedrock-parity item manipulation without a physical pointer.
5. Keep TC's Bedrock in-world controls as-is; only swap in the adaptive layer when a screen is open, sharing the same `ScreenMixin`/render hook as #397.
6. Expose an **API** for modded screens (CF does this) so third-party GUIs get navigation/cursor for free.

## 4. Phased, verifiable slices

- **P0 — Toolchain baseline.** Get Bazel + Android SDK/NDK building `//touchcontroller/versions/fabric/1.21.11`. *Blocked in this environment (no Bazel).*
- **P1 — Land PR #397 (exit button).** Adopted on branch `touchgrass`. Verify: build, then close the inventory by touch. *(build-verify pending Bazel)*
- **P2 — Context/screen conditions + adaptive layouts.** Generalize conditions; ship default per-screen layouts (inventory/creative/chest).
- **P3 — Container VirtualMouse.** Cursor + snapping + tap/long-press/quick-move/drop (CF `virtualmouse` parity).
- **P4 — On-screen keyboard + contextual button guide in screens.**
- **P5 — Multi-input (touch + controller hybrid).** Port CF #913 idea.
- **P6 — Unified action/gesture pipeline.** Study CF v4 (#1002); fold into TC `GameActions`.
- **P7 — Resource-pack data-driven layouts/conditions** (CF parity).

Each phase = one buildable target + one in-game acceptance check.

## 5. Constraints, risks, unknowns

- **Build/verify:** TC builds with **Bazel + Android SDK/NDK** (Android SDK is present; **Bazel is not**, here). Every change must be verified with `bazel build //touchcontroller/versions/<loader>/<ver>`. **No code in this plan has been compiled locally.** PR #397 being marked mergeable is our current build evidence.
- **Per-version divergence:** 26.2+ moved screen/HUD onto `Gui` (`client.gui.screen()`, `client.gui.hud.…`); there are 8 version source sets × 3 loaders. GUI-overlay + input features are implemented per version (mixins) and per loader packaging.
- **Input-path coordination:** new cursor/`screenop` and PR #397's `ScreenMixin` share the same render/click hook — avoid double rendering or double input handling.
- **CF v4 (#1002)** is draft/in-flux — treat as direction, don't hard-depend yet.
- **Licenses:** TC is LGPL-3.0-or-later; CF is LGPL-3.0-or-later — compatible for porting concepts, but attribution/notices must be kept.

## 6. PRs / issues to track

- **TC #397** — exit button (adopted). **TC #25** — the menu-exit bug this resolves.
- **CF #1002** "Controlify v4" — action/gesture + input pipeline (study for P6).
- **CF #913** — multiple/compound input (P5). **CF #714** — `screenop` for the mods list (screenop extensibility). **CF #706** — public API docs (authoring the screenop-style API).

## 7. Open questions (see wrap-up prompt)

- Priority loader + MC version to build/verify first (e.g. Fabric 1.21.1 vs 26.x).
- "Adaptive" scope: container-cursor Bedrock parity only, or also creative / typed GUIs (sign, anvil, merchant)?
- Touch + controller hybrid now (P5) or later?
