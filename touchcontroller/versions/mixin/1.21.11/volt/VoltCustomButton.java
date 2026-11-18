/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported directly from Volt Launcher (entitybrian69-bit/Volt-launcher: custom.rs & presets.rs)
 */

package top.fifthlight.touchcontroller.volt.v1_21_11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class VoltCustomButton {
    public String key;
    public String label;
    public List<String> extraKeys = new ArrayList<>();
    public float centerX;
    public float centerY;
    public float size;
    public float widthRatio = 1.0f;
    public float opacity = 0.72f;
    public boolean toggle = false;
    public boolean swipeable = false;
    public boolean passthrough = false;
    public boolean inGame = true;
    public boolean inMenu = true;
    public int bgColor = 0x91101016; // default [16, 16, 22, 145]
    public int strokeColor = 0x69E1E6F0; // default [225, 230, 240, 105]
    public float cornerRadius = 4.0f;
    public float strokeWidth = 1.0f;

    // Runtime state
    public boolean isPressed = false;
    public boolean isToggled = false;

    public VoltCustomButton(String key, String label, float x, float y, float size,
                            float widthRatio, float opacity, boolean toggle, boolean swipeable,
                            boolean passthrough, boolean inGame, boolean inMenu,
                            int bgColor, int strokeColor, float cornerRadius) {
        this.key = key;
        this.label = label;
        this.centerX = x;
        this.centerY = y;
        this.size = size;
        this.widthRatio = widthRatio;
        this.opacity = opacity;
        this.toggle = toggle;
        this.swipeable = swipeable;
        this.passthrough = passthrough;
        this.inGame = inGame;
        this.inMenu = inMenu;
        this.bgColor = bgColor;
        this.strokeColor = strokeColor;
        this.cornerRadius = cornerRadius;
    }

    public float[] getRect(float screenWidth, float screenHeight) {
        float aspect = screenWidth / Math.max(1.0f, screenHeight);
        float h = (size * screenHeight);
        float w = (h * widthRatio);
        float cx = centerX * screenWidth;
        float cy = centerY * screenHeight;
        return new float[]{cx - w / 2.0f, cy - h / 2.0f, w, h};
    }

    public boolean contains(float px, float py, float screenWidth, float screenHeight) {
        float[] r = getRect(screenWidth, screenHeight);
        return px >= r[0] && px <= (r[0] + r[2]) && py >= r[1] && py <= (r[1] + r[3]);
    }

    public void render(GuiGraphics guiGraphics, Minecraft mc, float screenWidth, float screenHeight) {
        float[] r = getRect(screenWidth, screenHeight);
        int x = (int) r[0];
        int y = (int) r[1];
        int w = (int) r[2];
        int h = (int) r[3];

        boolean active = isPressed || isToggled;

        // Apply alpha scaling with opacity
        int alpha = (int) (opacity * 255.0f);
        if (alpha <= 5) return;

        int fill = bgColor;
        int border = strokeColor;

        if (active) {
            // Volt active highlight: emerald / crimson glow
            if (toggle) {
                fill = 0xDD2A7E24; // emerald green toggle
                border = 0xFF3CEB34;
            } else {
                fill = 0xDDA83244; // crimson combat pressed
                border = 0xFFFF4460;
            }
        }

        // Adjust fill with base opacity
        int fillAlpha = ((fill >> 24) & 0xFF) * alpha / 255;
        fill = (fill & 0x00FFFFFF) | (fillAlpha << 24);

        int borderAlpha = ((border >> 24) & 0xFF) * alpha / 255;
        border = (border & 0x00FFFFFF) | (borderAlpha << 24);

        // Render filled box
        guiGraphics.fill(x, y, x + w, y + h, fill);

        // Render border
        int sw = Math.max(1, (int) strokeWidth);
        guiGraphics.fill(x, y, x + w, y + sw, border); // top
        guiGraphics.fill(x, y + h - sw, x + w, y + h, border); // bottom
        guiGraphics.fill(x, y, x + sw, y + h, border); // left
        guiGraphics.fill(x + w - sw, y, x + w, y + h, border); // right

        // Render label text centered
        if (label != null && !label.isEmpty()) {
            Font font = mc.font;
            int textWidth = font.width(label);
            int textX = x + (w - textWidth) / 2;
            int textY = y + (h - 8) / 2;
            int textColor = active ? 0xFFFFFFFF : 0xFFE0E0E0;
            guiGraphics.drawString(font, label, textX, textY, textColor, true);
        }
    }
}
