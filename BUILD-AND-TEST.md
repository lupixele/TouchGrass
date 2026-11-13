# Build & test: TouchController + PR #397 (touch exit button) → Fabric 1.21.11

**Why not on this box:** Bazel's JVM/coursier here can't reach FabricMC/Modrinth Maven (so the required re‑pin can't run), and the `+<hash>` Fabric‑API pins have rotted upstream. `curl` works, but Bazel/coursier don't. Run the below on a machine with normal Maven internet.

## Prerequisites (build machine)
- **Bazelisk**, or Bazel `9.2.0` (repo pins it in `.bazelversion`).
- **JDK 21** (needed to *run* 1.21.11; the 1.21.11 slice compiles with `--release=21`; MC `26.x` need JDK 25).
- **Android SDK + NDK** with `ANDROID_HOME` set (see the repo's `ENVIRONMENT_SETUP.md`); NDK r26+.
- A **Fabric Loader 1.21.11** instance to install into, with **Fabric API** and **Fabric Language Kotlin** already present.

## 1) Get the PR #397 code (the exit-button fix)
```bash
git clone https://github.com/TouchController/TouchController.git TouchController
cd TouchController
git fetch origin pull/397/head:touchgrass && git checkout touchgrass   # or: gh pr checkout 397
```

## 2) Re‑pin dependencies  ← the step that can't run on the broken box
This regenerates the pins against *currently resolvable* artifacts (heals the rot). Needs working access to `maven.fabricmc.net`, `repo1.maven.org`, `maven.terraformersmc.com`, `maven.nucleoid.xyz`, `dl.google.com`, and Mojang.
```bash
bash repin_all.sh
```
Or, Fabric/1.21.11 only:
```bash
REPIN=1 bazel run @maven//:pin
REPIN=1 bazel run @maven_fabric_1_21_11//:pin
```

## 3) Build the multi-version Fabric jar
```bash
bazel build //touchcontroller/fabric
```
Output (covers 1.21.11): `bazel-bin/touchcontroller/fabric/fabric.jar`
(Single-version slice if ever needed: `bazel build //touchcontroller/versions/fabric/1.21.11`.)

> If the build machine hits the same Java-downloader 404s we saw, append: `--host_jvmopt=-Djava.net.preferIPv4Stack=true` to the build.

## 4) Install into the 1.21.11 Fabric instance
```bash
cp bazel-bin/touchcontroller/fabric/fabric.jar "/c/Users/lupixele/AppData/Roaming/.minecraft/versions/1.21.11 Fabric/mods/"
```
Confirm the folder also has **Fabric API** and **Fabric Language Kotlin**. (First-party mods like combine/lang are bundled inside the jar; Mod Menu is optional.)

## 5) Test the exit button
Launch 1.21.11 Fabric with touch control enabled → open the **inventory / crafting table / chest** → an **exit button** should appear on the container GUI (top area, themed). Tapping it closes the screen without a keyboard. If no exit button shows, check TouchController's control-layout config (the widget is built-in, gated to container/GUI screens via the `IN_GUI` condition).

---
Repro of what PR #397 changes: `GameAction.closeScreen()` (per-version), an `IN_GUI` layer condition, a `close_screen` action bound to a themed exit widget, `GameState.inGui` redefined to "a closeable GUI is open", and a per-version `ScreenMixin` that renders the widget over the screen. See `PROGRESS.md` / `MAJOR-VERSION-PLAN.md`.

## Standalone companion mod (`exit-button-mod/`)
Source is complete (Mojang-mapped mixin, Loom default). It does NOT build on this box: this machine's JVM build toolchain (Gradle/Loom) can't fetch Minecraft + Mojang mappings from Mojang's servers (curl can, the JVM can't), so Loom can't provision mapped classes here. On any normally-connected box: `cd exit-button-mod && ./gradlew build` → build/libs/touchgrass-exit-1.0.0.jar. Machine-specific, not a mod defect.
