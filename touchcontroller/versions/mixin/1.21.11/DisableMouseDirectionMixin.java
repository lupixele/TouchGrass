/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v1_21_11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.common.config.data.StatusConfig;
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder;

@Mixin(MouseHandler.class)
abstract class DisableMouseDirectionMixin {
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;

    @Inject(
            method = "onButton",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onMouseButton(long window, net.minecraft.client.input.MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.getWindow() == null) return;
        float screenW = (float) this.minecraft.getWindow().getGuiScaledWidth();
        float screenH = (float) this.minecraft.getWindow().getGuiScaledHeight();
        float mouseX = (float) (this.xpos * (double) screenW / (double) this.minecraft.getWindow().getScreenWidth());
        float mouseY = (float) (this.ypos * (double) screenH / (double) this.minecraft.getWindow().getScreenHeight());

        if (action == 1) {
            if (VoltTouchEngine.getInstance().onTouchDown(-1, mouseX, mouseY, screenW, screenH)) {
                ci.cancel();
            }
        } else if (action == 0) {
            if (VoltTouchEngine.getInstance().onTouchUp(-1)) {
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "onMove",
            at = @At("HEAD")
    )
    private void onMouseMove(long window, double xpos, double ypos, CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.getWindow() == null) return;
        float screenW = (float) this.minecraft.getWindow().getGuiScaledWidth();
        float screenH = (float) this.minecraft.getWindow().getGuiScaledHeight();
        float mouseX = (float) (xpos * (double) screenW / (double) this.minecraft.getWindow().getScreenWidth());
        float mouseY = (float) (ypos * (double) screenH / (double) this.minecraft.getWindow().getScreenHeight());

        VoltTouchEngine.getInstance().onTouchMove(-1, mouseX, mouseY, screenW, screenH);
    }
    @Inject(
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;",
                    ordinal = 1
            ),
            method = "turnPlayer",
            cancellable = true
    )
    private void turnPlayer(CallbackInfo ci) {
        var configHolder = GlobalConfigHolder.INSTANCE;
        var config = configHolder.getConfig().getValue();
        if (config.getStatus().getStatus() == StatusConfig.Status.DISABLED) {
            return;
        }
        if (config.getRegular().getDisableMouseMove()) {
            ci.cancel();
        }
    }
}
