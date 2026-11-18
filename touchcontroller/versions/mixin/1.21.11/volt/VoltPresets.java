/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Ported directly from Volt Launcher (entitybrian69-bit/Volt-launcher: presets.rs)
 */

package top.fifthlight.touchcontroller.volt.v1_21_11;

import java.util.ArrayList;
import java.util.List;

public class VoltPresets {
    public enum PresetType {
        POJAV_CLASSIC("Pojav Classic", "Swipeable WASD + PRI/SEC/JUMP/INV/DEBUG/3RD"),
        ZALITH_MODERN("Zalith Layout", "Ergonomic action arc + sleek crimson buttons"),
        FOLD_CRAFT("Fold Craft (FCL)", "FCL quick-bar, hotbar scroll & combat diamond"),
        PVP_CLAW("4-Finger PvP Claw", "Top index triggers + thumb jump/crouch");

        private final String label;
        private final String description;

        PresetType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    private static VoltCustomButton btn(String key, String label, float x, float y, float size,
                                        float widthRatio, float opacity, boolean toggle, boolean swipeable,
                                        boolean passthrough, boolean inGame, boolean inMenu,
                                        int bg, int stroke, float radius) {
        return new VoltCustomButton(key, label, x, y, size, widthRatio, opacity, toggle, swipeable, passthrough, inGame, inMenu, bg, stroke, radius);
    }

    public static List<VoltCustomButton> buildPreset(PresetType type) {
        switch (type) {
            case ZALITH_MODERN:
                return buildZalithModern();
            case FOLD_CRAFT:
                return buildFoldCraft();
            case PVP_CLAW:
                return buildPvpClaw();
            case POJAV_CLASSIC:
            default:
                return buildPojavClassic();
        }
    }

    public static List<VoltCustomButton> buildPojavClassic() {
        int bg = 0x91101016;
        int stroke = 0x69E1E6F0;
        float rad = 4.0f;
        float topY = 0.065f;
        float topH = 0.082f;

        List<VoltCustomButton> list = new ArrayList<>();
        // Top utility strip (no virtual mouse pointer, no virtual keyboard per user spec)
        list.add(btn("special:menu", "ESC", 0.055f, topY, topH, 1.35f, 0.72f, false, false, false, true, true, bg, stroke, rad));
        list.add(btn("special:gui_toggle", "GUI", 0.145f, topY, topH, 1.35f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.f3", "DEBUG", 0.245f, topY, topH, 1.55f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.f5", "3RD", 0.345f, topY, topH, 1.35f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.t", "CHAT", 0.438f, topY, topH, 1.45f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.tab", "TAB", 0.532f, topY, topH, 1.35f, 0.72f, false, false, false, true, false, bg, stroke, rad));

        // Bottom-left swipeable WASD + center Sneak + Sprint
        list.add(btn("key.keyboard.w", "▲", 0.145f, 0.585f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.a", "◀", 0.072f, 0.725f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.left.shift", "◇", 0.145f, 0.725f, 0.120f, 1.0f, 0.72f, true, false, false, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.d", "▶", 0.218f, 0.725f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.s", "▼", 0.145f, 0.865f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.left.control", "CTRL", 0.062f, 0.445f, 0.088f, 1.35f, 0.70f, true, false, false, true, false, bg, stroke, rad));

        // Right-hand combat & inventory cluster
        list.add(btn("special:mouse_pri", "PRI", 0.785f, 0.635f, 0.145f, 1.15f, 0.76f, false, false, true, true, true, bg, stroke, rad));
        list.add(btn("special:mouse_sec", "SEC", 0.915f, 0.525f, 0.145f, 1.15f, 0.76f, false, false, true, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.space", "JUMP", 0.908f, 0.795f, 0.150f, 1.15f, 0.76f, false, false, true, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.e", "INV", 0.765f, 0.885f, 0.095f, 1.35f, 0.74f, false, false, false, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.f", "OFF", 0.675f, 0.765f, 0.092f, 1.15f, 0.70f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.q", "DROP", 0.935f, 0.335f, 0.088f, 1.30f, 0.68f, false, false, false, true, true, bg, stroke, rad));

        return list;
    }

    public static List<VoltCustomButton> buildZalithModern() {
        int bg = 0x9816121A;
        int stroke = 0x91E13052;
        int accentBg = 0xA82E121C;
        float rad = 10.0f;
        float topY = 0.065f;
        float topH = 0.078f;

        List<VoltCustomButton> list = new ArrayList<>();
        list.add(btn("special:menu", "ESC", 0.055f, topY, topH, 1.35f, 0.74f, false, false, false, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.f3", "F3", 0.135f, topY, topH, 1.20f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.f5", "F5", 0.208f, topY, topH, 1.20f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.t", "CHAT", 0.292f, topY, topH, 1.45f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.tab", "TAB", 0.382f, topY, topH, 1.30f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("special:gui_toggle", "HUD", 0.845f, topY, topH, 1.30f, 0.72f, false, false, false, true, false, bg, stroke, rad));

        // Movement
        list.add(btn("key.keyboard.w", "▲", 0.145f, 0.625f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.a", "◀", 0.072f, 0.745f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.d", "▶", 0.218f, 0.745f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.s", "▼", 0.145f, 0.865f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));

        // Right ergonomic arc
        list.add(btn("special:mouse_pri", "ATK", 0.790f, 0.625f, 0.150f, 1.05f, 0.80f, false, false, true, true, true, accentBg, stroke, rad));
        list.add(btn("special:mouse_sec", "USE", 0.915f, 0.505f, 0.145f, 1.05f, 0.80f, false, false, true, true, true, accentBg, stroke, rad));
        list.add(btn("key.keyboard.space", "JUMP", 0.910f, 0.785f, 0.155f, 1.05f, 0.80f, false, false, true, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.left.shift", "SNEAK", 0.785f, 0.845f, 0.112f, 1.22f, 0.76f, true, false, false, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.e", "BAG", 0.680f, 0.895f, 0.092f, 1.30f, 0.75f, false, false, false, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.f", "SWAP", 0.695f, 0.715f, 0.095f, 1.18f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.q", "DROP", 0.930f, 0.315f, 0.086f, 1.25f, 0.70f, false, false, false, true, true, bg, stroke, rad));

        return list;
    }

    public static List<VoltCustomButton> buildFoldCraft() {
        int bg = 0x9414181E;
        int stroke = 0x78B4C3DC;
        float rad = 8.0f;
        float topY = 0.065f;
        float topH = 0.078f;

        List<VoltCustomButton> list = new ArrayList<>();
        list.add(btn("special:menu", "ESC", 0.055f, topY, topH, 1.30f, 0.72f, false, false, false, true, true, bg, stroke, rad));
        list.add(btn("special:gui_toggle", "GUI", 0.135f, topY, topH, 1.30f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.f3", "F3", 0.210f, topY, topH, 1.18f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.f5", "F5", 0.278f, topY, topH, 1.18f, 0.72f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.t", "CHAT", 0.358f, topY, topH, 1.40f, 0.72f, false, false, false, true, false, bg, stroke, rad));

        // Movement & hotbar wheel buttons
        list.add(btn("key.keyboard.left.control", "RUN", 0.068f, 0.455f, 0.090f, 1.25f, 0.70f, true, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.w", "▲", 0.145f, 0.625f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.a", "◀", 0.072f, 0.745f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.d", "▶", 0.218f, 0.745f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.s", "▼", 0.145f, 0.865f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));

        list.add(btn("special:scroll_up", "▲SL", 0.855f, 0.315f, 0.082f, 1.20f, 0.68f, false, false, false, true, true, bg, stroke, rad));
        list.add(btn("special:scroll_down", "▼SL", 0.935f, 0.315f, 0.082f, 1.20f, 0.68f, false, false, false, true, true, bg, stroke, rad));

        // Right combat & utility diamond
        list.add(btn("special:mouse_pri", "LMB", 0.785f, 0.640f, 0.145f, 1.10f, 0.76f, false, false, true, true, true, bg, stroke, rad));
        list.add(btn("special:mouse_sec", "RMB", 0.915f, 0.520f, 0.145f, 1.10f, 0.76f, false, false, true, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.space", "JUMP", 0.910f, 0.795f, 0.148f, 1.10f, 0.76f, false, false, true, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.left.shift", "SNK", 0.785f, 0.845f, 0.108f, 1.15f, 0.74f, true, false, false, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.e", "INV", 0.680f, 0.895f, 0.092f, 1.30f, 0.74f, false, false, false, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.f", "OFF", 0.690f, 0.720f, 0.090f, 1.15f, 0.70f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.q", "Q", 0.775f, 0.475f, 0.086f, 1.05f, 0.68f, false, false, false, true, true, bg, stroke, rad));

        return list;
    }

    public static List<VoltCustomButton> buildPvpClaw() {
        int bg = 0x94140E14;
        int triggerBg = 0xAC3E101C;
        int stroke = 0xA0F53A5C;
        float rad = 12.0f;

        List<VoltCustomButton> list = new ArrayList<>();
        // Left index trigger (top-left) — Attack with aim passthrough
        list.add(btn("special:mouse_pri", "ATTACK", 0.115f, 0.205f, 0.185f, 1.45f, 0.82f, false, false, true, true, true, triggerBg, stroke, rad));
        list.add(btn("key.keyboard.left.control", "SPRINT", 0.265f, 0.185f, 0.098f, 1.30f, 0.75f, true, false, false, true, false, bg, stroke, rad));

        // Movement
        list.add(btn("key.keyboard.w", "▲", 0.145f, 0.625f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.a", "◀", 0.072f, 0.745f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.d", "▶", 0.218f, 0.745f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.s", "▼", 0.145f, 0.865f, 0.125f, 1.0f, 0.72f, false, true, false, true, false, bg, stroke, rad));

        // Right index trigger (top-right) — Use / Place with aim passthrough
        list.add(btn("special:mouse_sec", "USE / PLACE", 0.885f, 0.205f, 0.185f, 1.45f, 0.82f, false, false, true, true, true, triggerBg, stroke, rad));
        list.add(btn("key.keyboard.f", "OFFHAND", 0.730f, 0.185f, 0.098f, 1.35f, 0.75f, false, false, false, true, false, bg, stroke, rad));

        // Top center compact utility strip
        list.add(btn("special:menu", "ESC", 0.405f, 0.062f, 0.072f, 1.25f, 0.70f, false, false, false, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.f5", "F5", 0.478f, 0.062f, 0.072f, 1.15f, 0.70f, false, false, false, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.t", "CHAT", 0.552f, 0.062f, 0.072f, 1.30f, 0.70f, false, false, false, true, false, bg, stroke, rad));

        // Right thumb (bottom-right) — Jump, Crouch, Inventory, Drop
        list.add(btn("key.keyboard.space", "JUMP", 0.885f, 0.735f, 0.175f, 1.15f, 0.80f, false, false, true, true, false, bg, stroke, rad));
        list.add(btn("key.keyboard.left.shift", "CROUCH", 0.755f, 0.835f, 0.125f, 1.22f, 0.76f, true, false, false, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.e", "INV", 0.755f, 0.645f, 0.105f, 1.20f, 0.74f, false, false, false, true, true, bg, stroke, rad));
        list.add(btn("key.keyboard.q", "DROP", 0.915f, 0.485f, 0.090f, 1.20f, 0.70f, false, false, false, true, true, bg, stroke, rad));

        return list;
    }
}
