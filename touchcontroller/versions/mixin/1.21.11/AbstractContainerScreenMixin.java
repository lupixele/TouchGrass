/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v1_21_11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.touchcontroller.common.config.data.StatusConfig;
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder;
import top.fifthlight.touchcontroller.common.event.render.RenderEvents;

import java.util.Set;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> {
    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected T menu;

    @Shadow
    protected Font font;

    @Shadow
    protected boolean isQuickCrafting;

    @Shadow
    protected Set<Slot> quickCraftSlots;

    @Shadow
    protected abstract boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY);

    @Shadow
    protected abstract boolean hasClickedOutside(double mouseX, double mouseY, int left, int top);

    @Shadow
    protected abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType);

    @Unique
    private static final long HOLD_THRESHOLD_MS = 380L;

    @Unique
    private static final long DOUBLE_TAP_THRESHOLD_MS = 350L;

    @Unique
    private Slot touchcontroller$heldSlot = null;

    @Unique
    private long touchcontroller$pressStartTime = 0L;

    @Unique
    private boolean touchcontroller$splitTriggered = false;

    @Unique
    private boolean touchcontroller$isPointerDown = false;

    @Unique
    private Slot touchcontroller$lastClickedSlot = null;

    @Unique
    private long touchcontroller$lastClickTime = 0L;

    @Unique
    private Slot touchcontroller$lastDragSlot = null;

    @Unique
    private Slot touchcontroller$selectedSlot = null;

    @Unique
    private boolean touchcontroller$shiftActive = false;

    @Unique
    private boolean touchcontroller$isTouchActive() {
        var config = GlobalConfigHolder.INSTANCE.getConfig().getValue();
        return config.getStatus().getStatus() != StatusConfig.Status.DISABLED;
    }

    @Unique
    private int touchcontroller$getTouchX(double fallback) {
        float[] coords = RenderEvents.getPrimaryPointerCoords();
        if (coords != null) {
            var window = Minecraft.getInstance().getWindow();
            return (int) (coords[0] * window.getGuiScaledWidth());
        }
        return (int) fallback;
    }

    @Unique
    private int touchcontroller$getTouchY(double fallback) {
        float[] coords = RenderEvents.getPrimaryPointerCoords();
        if (coords != null) {
            var window = Minecraft.getInstance().getWindow();
            return (int) (coords[1] * window.getGuiScaledHeight());
        }
        return (int) fallback;
    }

    @Unique
    private Slot touchcontroller$findSlot(double mouseX, double mouseY) {
        if (this.menu == null || this.menu.slots == null) {
            return null;
        }
        for (Slot slot : this.menu.slots) {
            if (slot.isActive() && this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    @Unique
    private int touchcontroller$getButtonX() {
        return Math.max(6, this.leftPos - 54);
    }

    @Unique
    private int touchcontroller$getShiftY() {
        return Math.max(20, this.topPos + 16);
    }

    @Unique
    private int touchcontroller$getDropY() {
        return Math.max(46, this.topPos + 44);
    }

    @Unique
    private boolean touchcontroller$checkButtonClicks(double x, double y) {
        int btnX = touchcontroller$getButtonX();
        int shiftY = touchcontroller$getShiftY();
        int dropY = touchcontroller$getDropY();
        int btnWidth = 48;
        int btnHeight = 22;

        if (x >= btnX && x <= btnX + btnWidth) {
            if (y >= shiftY && y <= shiftY + btnHeight) {
                this.touchcontroller$shiftActive = !this.touchcontroller$shiftActive;
                return true;
            }
            if (y >= dropY && y <= dropY + btnHeight) {
                if (!this.menu.getCarried().isEmpty()) {
                    this.slotClicked(null, -999, 0, ClickType.PICKUP);
                } else if (this.touchcontroller$selectedSlot != null && this.touchcontroller$selectedSlot.hasItem()) {
                    this.slotClicked(this.touchcontroller$selectedSlot, this.touchcontroller$selectedSlot.index, 0, ClickType.THROW);
                }
                return true;
            }
        }
        return false;
    }

    @Unique
    private void touchcontroller$resetState() {
        this.touchcontroller$heldSlot = null;
        this.touchcontroller$pressStartTime = 0L;
        this.touchcontroller$splitTriggered = false;
        this.touchcontroller$isPointerDown = false;
        this.touchcontroller$lastDragSlot = null;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!touchcontroller$isTouchActive()) {
            return;
        }

        double mouseX = touchcontroller$getTouchX(event.x());
        double mouseY = touchcontroller$getTouchY(event.y());

        // Check on-screen modifier button clicks (Shift & Drop)
        if (touchcontroller$checkButtonClicks(mouseX, mouseY)) {
            cir.setReturnValue(true);
            return;
        }

        Slot slot = touchcontroller$findSlot(mouseX, mouseY);
        boolean outside = hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos);
        long now = System.currentTimeMillis();

        boolean hasCarried = !this.menu.getCarried().isEmpty();

        // 1. Shift mode active: immediate quick-move on slot tap
        if (this.touchcontroller$shiftActive && slot != null && slot.hasItem() && !hasCarried) {
            this.slotClicked(slot, slot.index, 0, ClickType.QUICK_MOVE);
            cir.setReturnValue(true);
            return;
        }

        // 2. When carrying an item: Tap-to-Place or Drop outside
        if (hasCarried) {
            if (slot != null) {
                // If tapping the exact slot we picked up from within double-tap threshold: quick-move it
                if (this.touchcontroller$lastClickedSlot == slot && (now - this.touchcontroller$lastClickTime) < DOUBLE_TAP_THRESHOLD_MS) {
                    this.slotClicked(slot, slot.index, 0, ClickType.PICKUP);
                    this.slotClicked(slot, slot.index, 0, ClickType.QUICK_MOVE);
                    this.touchcontroller$selectedSlot = null;
                    this.touchcontroller$lastClickedSlot = null;
                    this.touchcontroller$lastClickTime = 0L;
                    this.isQuickCrafting = false;
                    this.quickCraftSlots.clear();
                    cir.setReturnValue(true);
                    return;
                }

                // Normal tap-to-place / swap
                this.slotClicked(slot, slot.index, event.button(), ClickType.PICKUP);
                if (this.menu.getCarried().isEmpty()) {
                    this.touchcontroller$selectedSlot = null;
                    this.touchcontroller$lastClickedSlot = null;
                } else {
                    this.touchcontroller$selectedSlot = slot;
                    this.touchcontroller$lastClickedSlot = slot;
                }
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                cir.setReturnValue(true);
                return;
            } else if (outside) {
                this.slotClicked(null, -999, event.button(), ClickType.PICKUP);
                if (this.menu.getCarried().isEmpty()) {
                    this.touchcontroller$selectedSlot = null;
                    this.touchcontroller$lastClickedSlot = null;
                }
                cir.setReturnValue(true);
                return;
            }
            return;
        }

        // 3. Carried is EMPTY: Tap on slot to select / pick up / quick-move
        if (slot != null) {
            // Check double-tap quick-move
            if (this.touchcontroller$lastClickedSlot == slot && (now - this.touchcontroller$lastClickTime) < DOUBLE_TAP_THRESHOLD_MS) {
                this.slotClicked(slot, slot.index, 0, ClickType.QUICK_MOVE);
                this.touchcontroller$lastClickedSlot = null;
                this.touchcontroller$lastClickTime = 0L;
                touchcontroller$resetState();
                this.touchcontroller$splitTriggered = true;
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                cir.setReturnValue(true);
                return;
            }

            if (slot.hasItem()) {
                this.touchcontroller$heldSlot = slot;
                this.touchcontroller$pressStartTime = now;
                this.touchcontroller$splitTriggered = false;
                this.touchcontroller$isPointerDown = true;
                this.touchcontroller$lastClickedSlot = slot;
                this.touchcontroller$lastClickTime = now;
                this.touchcontroller$selectedSlot = slot;
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void onMouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!touchcontroller$isTouchActive()) {
            return;
        }

        this.touchcontroller$isPointerDown = false;
        this.touchcontroller$lastDragSlot = null;

        // If a slot was held and split was NOT triggered, it was a quick tap -> pick up full stack
        if (this.touchcontroller$heldSlot != null && !this.touchcontroller$splitTriggered && this.menu.getCarried().isEmpty()) {
            this.slotClicked(this.touchcontroller$heldSlot, this.touchcontroller$heldSlot.index, 0, ClickType.PICKUP);
            this.touchcontroller$selectedSlot = this.touchcontroller$heldSlot;
            this.touchcontroller$heldSlot = null;
            this.touchcontroller$splitTriggered = false;
            this.isQuickCrafting = false;
            this.quickCraftSlots.clear();
            cir.setReturnValue(true);
            return;
        }

        this.touchcontroller$heldSlot = null;
        this.touchcontroller$splitTriggered = false;
    }

    @Inject(method = "renderCarriedItem", at = @At("HEAD"), cancellable = true)
    private void onRenderCarriedItem(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (!touchcontroller$isTouchActive()) {
            return;
        }
        float[] coords = RenderEvents.getPrimaryPointerCoords();
        if (coords != null) {
            // Cancel vanilla's renderCarriedItem because we render it directly at the live touch position in onRender
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!touchcontroller$isTouchActive()) {
            return;
        }

        long now = System.currentTimeMillis();
        float[] coords = RenderEvents.getPrimaryPointerCoords();
        int touchX = mouseX;
        int touchY = mouseY;
        if (coords != null) {
            var window = Minecraft.getInstance().getWindow();
            touchX = (int) (coords[0] * window.getGuiScaledWidth());
            touchY = (int) (coords[1] * window.getGuiScaledHeight());
        }

        // 1. Render on-screen native modifier buttons (SHIFT & DROP)
        touchcontroller$renderButtons(guiGraphics);

        // 2. Real-time hold-to-split check & green progress bar
        if (this.touchcontroller$isPointerDown && this.touchcontroller$heldSlot != null && !this.touchcontroller$splitTriggered && this.menu.getCarried().isEmpty()) {
            long elapsed = now - this.touchcontroller$pressStartTime;
            if (elapsed >= HOLD_THRESHOLD_MS) {
                this.touchcontroller$splitTriggered = true;
                this.slotClicked(this.touchcontroller$heldSlot, this.touchcontroller$heldSlot.index, 1, ClickType.PICKUP);
                this.touchcontroller$selectedSlot = this.touchcontroller$heldSlot;
            } else if (elapsed > 50L) {
                float progress = Math.min(1.0f, (float) elapsed / (float) HOLD_THRESHOLD_MS);
                touchcontroller$renderProgressBar(guiGraphics, this.touchcontroller$heldSlot, progress);
            }
        }

        // 3. Sliding item distribution across slots while dragging
        if (coords != null && !this.menu.getCarried().isEmpty()) {
            Slot hovered = touchcontroller$findSlot(touchX, touchY);
            if (hovered != null && hovered != this.touchcontroller$lastDragSlot) {
                this.touchcontroller$lastDragSlot = hovered;
                this.slotClicked(hovered, hovered.index, 1, ClickType.PICKUP);
            }
        } else if (coords == null) {
            this.touchcontroller$lastDragSlot = null;
        }

        // 4. Render live carried item icon following the finger smoothly
        if (coords != null && !this.menu.getCarried().isEmpty()) {
            ItemStack carried = this.menu.getCarried();
            guiGraphics.nextStratum();
            guiGraphics.renderItem(carried, touchX - 8, touchY - 8);
            guiGraphics.renderItemDecorations(this.font, carried, touchX - 8, touchY - 8);
        }

        // 5. Bedrock Green Selection Frame around active / selected slot
        if (!this.menu.getCarried().isEmpty()) {
            Slot slotToHighlight = this.touchcontroller$selectedSlot;
            if (slotToHighlight != null && slotToHighlight.isActive()) {
                touchcontroller$renderSelectionFrame(guiGraphics, slotToHighlight);
            }
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        touchcontroller$resetState();
        this.touchcontroller$lastClickedSlot = null;
        this.touchcontroller$selectedSlot = null;
        this.touchcontroller$shiftActive = false;
    }

    @Unique
    private void touchcontroller$renderButtons(GuiGraphics guiGraphics) {
        int btnX = touchcontroller$getButtonX();
        int shiftY = touchcontroller$getShiftY();
        int dropY = touchcontroller$getDropY();
        int btnWidth = 48;
        int btnHeight = 22;

        // --- SHIFT BUTTON ---
        int shiftBg = this.touchcontroller$shiftActive ? 0xDD2A7E24 : 0xBB1E1E1E;
        int shiftBorder = this.touchcontroller$shiftActive ? 0xFF3CEB34 : 0xFF666666;
        int shiftText = this.touchcontroller$shiftActive ? 0xFFFFFFFF : 0xFFCCCCCC;

        guiGraphics.fill(btnX, shiftY, btnX + btnWidth, shiftY + btnHeight, shiftBg);
        guiGraphics.fill(btnX, shiftY, btnX + btnWidth, shiftY + 1, shiftBorder);
        guiGraphics.fill(btnX, shiftY + btnHeight - 1, btnX + btnWidth, shiftY + btnHeight, shiftBorder);
        guiGraphics.fill(btnX, shiftY + 1, btnX + 1, shiftY + btnHeight - 1, shiftBorder);
        guiGraphics.fill(btnX + btnWidth - 1, shiftY + 1, btnX + btnWidth, shiftY + btnHeight - 1, shiftBorder);
        guiGraphics.drawCenteredString(this.font, this.touchcontroller$shiftActive ? "§l⇧ SHIFT" : "⇧ SHIFT", btnX + btnWidth / 2, shiftY + 7, shiftText);

        // --- DROP BUTTON ---
        int dropBg = 0xBB1E1E1E;
        int dropBorder = 0xFF666666;

        guiGraphics.fill(btnX, dropY, btnX + btnWidth, dropY + btnHeight, dropBg);
        guiGraphics.fill(btnX, dropY, btnX + btnWidth, dropY + 1, dropBorder);
        guiGraphics.fill(btnX, dropY + btnHeight - 1, btnX + btnWidth, dropY + btnHeight, dropBorder);
        guiGraphics.fill(btnX, dropY + 1, btnX + 1, dropY + btnHeight - 1, dropBorder);
        guiGraphics.fill(btnX + btnWidth - 1, dropY + 1, btnX + btnWidth, dropY + btnHeight - 1, dropBorder);
        guiGraphics.drawCenteredString(this.font, "⎋ DROP", btnX + btnWidth / 2, dropY + 7, 0xFFCCCCCC);
    }

    @Unique
    private void touchcontroller$renderSelectionFrame(GuiGraphics guiGraphics, Slot slot) {
        int x = this.leftPos + slot.x;
        int y = this.topPos + slot.y;
        int color = 0xFF3CEB34; // Bedrock Vibrant Green

        guiGraphics.fill(x - 1, y - 1, x + 17, y, color);
        guiGraphics.fill(x - 1, y + 16, x + 17, y + 17, color);
        guiGraphics.fill(x - 1, y, x, y + 16, color);
        guiGraphics.fill(x + 16, y, x + 17, y + 16, color);

        guiGraphics.fill(x, y, x + 16, y + 16, 0x263CEB34);
    }

    @Unique
    private void touchcontroller$renderProgressBar(GuiGraphics guiGraphics, Slot slot, float progress) {
        int x = this.leftPos + slot.x;
        int y = this.topPos + slot.y + 12;
        int barWidth = 16;
        int barHeight = 4;

        guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xDD111111);

        guiGraphics.fill(x, y, x + barWidth, y + 1, 0xFFE0E0E0);
        guiGraphics.fill(x, y + barHeight - 1, x + barWidth, y + barHeight, 0xFFE0E0E0);
        guiGraphics.fill(x, y + 1, x + 1, y + barHeight - 1, 0xFFE0E0E0);
        guiGraphics.fill(x + barWidth - 1, y + 1, x + barWidth, y + barHeight - 1, 0xFFE0E0E0);

        int fillWidth = Math.max(1, (int) ((barWidth - 2) * progress));
        guiGraphics.fill(x + 1, y + 1, x + 1 + fillWidth, y + barHeight - 1, 0xFF3CEB34);
    }
}
