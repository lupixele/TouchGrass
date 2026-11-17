/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported directly from Volt Launcher (entitybrian69-bit/Volt-launcher: touch.rs & custom.rs)
 */

package top.fifthlight.touchcontroller.mixin.v1_21_11;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import top.fifthlight.touchcontroller.extension.v1_21_11.ClickableKeyBinding;
import top.fifthlight.touchcontroller.extension.v1_21_11.GameModeWithBreakingProgress;

import java.util.*;

public class VoltTouchEngine {
    private static final VoltTouchEngine INSTANCE = new VoltTouchEngine();

    public static VoltTouchEngine getInstance() {
        return INSTANCE;
    }

    public static class FingerState {
        public int id;
        public float startX;
        public float startY;
        public float currentX;
        public float currentY;
        public VoltCustomButton boundButton;
        public boolean isPassthrough;
        public boolean isSwipeable;
        public long downTime;

        public FingerState(int id, float x, float y, VoltCustomButton boundButton) {
            this.id = id;
            this.startX = x;
            this.startY = y;
            this.currentX = x;
            this.currentY = y;
            this.boundButton = boundButton;
            this.isPassthrough = boundButton != null && boundButton.passthrough;
            this.isSwipeable = boundButton != null && boundButton.swipeable;
            this.downTime = System.currentTimeMillis();
        }
    }

    private VoltPresets.PresetType currentPresetType = VoltPresets.PresetType.POJAV_CLASSIC;
    private List<VoltCustomButton> activeButtons = new ArrayList<>();
    private final Map<Integer, FingerState> activeFingers = new HashMap<>();

    // Settings
    private boolean enabled = true;
    private float globalOpacity = 1.0f;
    private float touchSensitivity = 1.0f;

    private VoltTouchEngine() {
        loadPreset(VoltPresets.PresetType.POJAV_CLASSIC);
        top.fifthlight.touchcontroller.common.event.render.RenderEvents.setPointerListener((type, id, nx, ny) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getWindow() == null) return;
            float sw = (float) mc.getWindow().getGuiScaledWidth();
            float sh = (float) mc.getWindow().getGuiScaledHeight();
            float x = nx * sw;
            float y = ny * sh;
            if (type == 0) {
                onTouchDown(id, x, y, sw, sh);
            } else if (type == 1) {
                onTouchMove(id, x, y, sw, sh);
            } else if (type == 2) {
                onTouchUp(id);
            } else if (type == 3) {
                releaseAllKeys();
                activeFingers.clear();
            }
        });
    }

    public void loadPreset(VoltPresets.PresetType type) {
        this.currentPresetType = type;
        this.activeButtons = VoltPresets.buildPreset(type);
        this.activeFingers.clear();
    }

    public VoltPresets.PresetType getCurrentPresetType() {
        return currentPresetType;
    }

    public List<VoltCustomButton> getActiveButtons() {
        return activeButtons;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            releaseAllKeys();
            activeFingers.clear();
        }
    }

    public boolean onTouchDown(int id, float x, float y, float screenW, float screenH) {
        if (!enabled) return false;
        Minecraft mc = Minecraft.getInstance();
        boolean inGame = (mc.screen == null);
        boolean inMenu = (mc.screen != null);

        // Check buttons from top to bottom
        for (int i = activeButtons.size() - 1; i >= 0; i--) {
            VoltCustomButton btn = activeButtons.get(i);
            if ((inGame && btn.inGame) || (inMenu && btn.inMenu)) {
                if (btn.contains(x, y, screenW, screenH)) {
                    if (btn.toggle) {
                        btn.isToggled = !btn.isToggled;
                        dispatchToken(btn.key, btn.isToggled);
                    } else {
                        btn.isPressed = true;
                        dispatchToken(btn.key, true);
                    }
                    activeFingers.put(id, new FingerState(id, x, y, btn));
                    return true;
                }
            }
        }

        // If touching right half of screen in game, register as camera look finger
        if (inGame && x >= (screenW * 0.35f)) {
            FingerState lookFinger = new FingerState(id, x, y, null);
            lookFinger.isPassthrough = true;
            activeFingers.put(id, lookFinger);
            return true;
        }

        return false;
    }

    public boolean onTouchMove(int id, float x, float y, float screenW, float screenH) {
        if (!enabled) return false;
        FingerState finger = activeFingers.get(id);
        if (finger == null) return false;

        float deltaX = x - finger.currentX;
        float deltaY = y - finger.currentY;
        finger.currentX = x;
        finger.currentY = y;

        // Swipeable button transition (WASD D-pad sliding)
        if (finger.isSwipeable) {
            Minecraft mc = Minecraft.getInstance();
            boolean inGame = (mc.screen == null);
            boolean inMenu = (mc.screen != null);

            for (VoltCustomButton btn : activeButtons) {
                if (((inGame && btn.inGame) || (inMenu && btn.inMenu)) && btn.swipeable) {
                    if (btn.contains(x, y, screenW, screenH)) {
                        if (finger.boundButton != btn) {
                            if (finger.boundButton != null && !finger.boundButton.toggle) {
                                finger.boundButton.isPressed = false;
                                dispatchToken(finger.boundButton.key, false);
                            }
                            btn.isPressed = true;
                            dispatchToken(btn.key, true);
                            finger.boundButton = btn;
                        }
                        break;
                    }
                }
            }
        }

        // Camera passthrough rotation (aiming while holding attack/use/jump/crouch or dragging screen)
        if (finger.isPassthrough || finger.boundButton == null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.screen == null) {
                double sens = mc.options.sensitivity().get() * 0.6 + 0.2;
                double mult = sens * sens * sens * 8.0 * touchSensitivity;
                double dx = (deltaX * 0.15) * mult;
                double dy = (deltaY * 0.15) * mult;
                mc.player.turn(dx, dy);
            }
        }

        return true;
    }

    public boolean onTouchUp(int id) {
        FingerState finger = activeFingers.remove(id);
        if (finger != null && finger.boundButton != null) {
            if (!finger.boundButton.toggle) {
                finger.boundButton.isPressed = false;
                dispatchToken(finger.boundButton.key, false);
            }
            return true;
        }
        return false;
    }

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            if (!activeFingers.isEmpty()) {
                releaseAllKeys();
                activeFingers.clear();
            }
        }
    }

    public void releaseAllKeys() {
        for (VoltCustomButton btn : activeButtons) {
            if (btn.isPressed || btn.isToggled) {
                btn.isPressed = false;
                btn.isToggled = false;
                dispatchToken(btn.key, false);
            }
        }
    }

    public void dispatchToken(String token, boolean pressed) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        if ("special:mouse_pri".equals(token)) {
            mc.options.keyAttack.setDown(pressed);
            if (pressed && mc.options.keyAttack instanceof ClickableKeyBinding clickable) {
                clickable.touchController$click();
            }
        } else if ("special:mouse_sec".equals(token)) {
            mc.options.keyUse.setDown(pressed);
            if (pressed && mc.options.keyUse instanceof ClickableKeyBinding clickable) {
                clickable.touchController$click();
            }
        } else if ("special:menu".equals(token)) {
            if (pressed) {
                if (mc.screen != null) {
                    mc.screen.onClose();
                } else if (mc.player != null) {
                    mc.pauseGame(false);
                }
            }
        } else if ("special:gui_toggle".equals(token)) {
            if (pressed) {
                mc.options.hideGui = !mc.options.hideGui;
            }
        } else if ("special:scroll_up".equals(token)) {
            if (pressed && mc.player != null) {
                int cur = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot((cur - 1 + 9) % 9);
            }
        } else if ("special:scroll_down".equals(token)) {
            if (pressed && mc.player != null) {
                int cur = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot((cur + 1) % 9);
            }
        } else if ("key.keyboard.space".equals(token)) {
            mc.options.keyJump.setDown(pressed);
        } else if ("key.keyboard.left.shift".equals(token)) {
            mc.options.keyShift.setDown(pressed);
        } else if ("key.keyboard.left.control".equals(token)) {
            mc.options.keySprint.setDown(pressed);
        } else if ("key.keyboard.w".equals(token)) {
            mc.options.keyUp.setDown(pressed);
        } else if ("key.keyboard.s".equals(token)) {
            mc.options.keyDown.setDown(pressed);
        } else if ("key.keyboard.a".equals(token)) {
            mc.options.keyLeft.setDown(pressed);
        } else if ("key.keyboard.d".equals(token)) {
            mc.options.keyRight.setDown(pressed);
        } else if ("key.keyboard.e".equals(token)) {
            if (pressed) {
                if (mc.screen != null) {
                    mc.screen.onClose();
                } else if (mc.player != null) {
                    mc.options.keyInventory.setDown(true);
                    if (mc.options.keyInventory instanceof ClickableKeyBinding clickable) {
                        clickable.touchController$click();
                    }
                }
            } else {
                mc.options.keyInventory.setDown(false);
            }
        } else if ("key.keyboard.f".equals(token)) {
            mc.options.keySwapOffhand.setDown(pressed);
            if (pressed && mc.options.keySwapOffhand instanceof ClickableKeyBinding clickable) {
                clickable.touchController$click();
            }
        } else if ("key.keyboard.q".equals(token)) {
            mc.options.keyDrop.setDown(pressed);
            if (pressed && mc.options.keyDrop instanceof ClickableKeyBinding clickable) {
                clickable.touchController$click();
            }
        } else if ("key.keyboard.t".equals(token)) {
            if (pressed && mc.screen == null) {
                mc.setScreen(new net.minecraft.client.gui.screens.ChatScreen("", false));
            }
        } else if ("key.keyboard.tab".equals(token)) {
            mc.options.keyPlayerList.setDown(pressed);
        } else if ("key.keyboard.f3".equals(token)) {
            if (pressed) {
                if (mc.options.keyDebugOverlay instanceof ClickableKeyBinding clickable) {
                    clickable.touchController$click();
                } else {
                    mc.options.keyDebugOverlay.setDown(true);
                }
            } else {
                mc.options.keyDebugOverlay.setDown(false);
            }
        } else if ("key.keyboard.f5".equals(token)) {
            if (pressed) {
                mc.options.setCameraType(mc.options.getCameraType().cycle());
            }
        } else {
            // Fallback: look up key in KeyMapping registry
            Map<String, KeyMapping> all = KeyMappingAccessor.touchcontroller$getAllKeyMappings();
            if (all != null) {
                KeyMapping mapping = all.get(token);
                if (mapping != null) {
                    mapping.setDown(pressed);
                    if (pressed && mapping instanceof ClickableKeyBinding clickable) {
                        clickable.touchController$click();
                    }
                }
            }
        }
    }

    public void renderHud(GuiGraphics guiGraphics) {
        if (!enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        boolean inGame = (mc.screen == null);
        boolean inMenu = (mc.screen != null);

        if (inGame && mc.options.hideGui) {
            return;
        }

        float screenW = (float) mc.getWindow().getGuiScaledWidth();
        float screenH = (float) mc.getWindow().getGuiScaledHeight();

        // Render Volt touch buttons
        for (VoltCustomButton btn : activeButtons) {
            if ((inGame && btn.inGame) || (inMenu && btn.inMenu)) {
                btn.render(guiGraphics, mc, screenW, screenH);
            }
        }

        // In-game Bedrock circular breaking progress ring
        if (inGame && mc.gameMode instanceof GameModeWithBreakingProgress breaking) {
            float progress = breaking.touchcontroller$getBreakingProgress();
            if (progress > 0.0f && progress <= 1.0f) {
                renderBreakingRing(guiGraphics, screenW / 2.0f, screenH / 2.0f, progress);
            }
        }
    }

    private void renderBreakingRing(GuiGraphics guiGraphics, float cx, float cy, float progress) {
        int radius = 16;
        int thickness = 3;
        int segments = 24;
        int filledSegments = (int) (progress * segments);

        // Draw circular track
        for (int i = 0; i < segments; i++) {
            double angle = (2.0 * Math.PI * i) / segments - Math.PI / 2.0;
            int px = (int) (cx + Math.cos(angle) * radius);
            int py = (int) (cy + Math.sin(angle) * radius);
            int color = (i <= filledSegments) ? 0xFF3CEB34 : 0x66202020; // Bedrock green fill / dark bg
            guiGraphics.fill(px - thickness / 2, py - thickness / 2, px + thickness / 2 + 1, py + thickness / 2 + 1, color);
        }
    }
}
