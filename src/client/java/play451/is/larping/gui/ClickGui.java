package play451.is.larping.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import play451.is.larping.module.Category;
import play451.is.larping.module.Module;
import play451.is.larping.module.ModuleManager;
import play451.is.larping.module.impl.core.ClickGuiModule;
import play451.is.larping.module.setting.ColorSetting;
import play451.is.larping.module.setting.Setting;

import java.util.List;

public class ClickGui extends Screen {

    private static final int PANEL_WIDTH    = 90;
    private static final int HEADER_HEIGHT  = 13;
    private static final int ROW_HEIGHT     = 11;
    private static final int MAX_VISIBLE    = 12;
    private static final int START_Y        = 2;
    private static final int START_X_PAD   = 4;

    private static final int SETTINGS_W       = 140;
    private static final int SETTINGS_TITLE_H = 13;
    private static final int SETTINGS_PAD     = 4;
    private static final int SLIDER_H         = 7;
    private static final int SLIDER_GAP       = 2;
    private static final int LABEL_W          = 8;
    private static final int VALUE_W          = 18;
    private static final int SECTION_GAP      = 5;
    private static final int SECTION_LABEL_H  = 9;

    private static int col(ColorSetting s) {
        if (ClickGuiModule.INSTANCE == null) return 0;
        return s.getPacked();
    }

    private int headerColor()  { return ClickGuiModule.INSTANCE != null ? ClickGuiModule.INSTANCE.headerColor.getPacked()  : 0xFF0F7070; }
    private int moduleColor()  { return ClickGuiModule.INSTANCE != null ? ClickGuiModule.INSTANCE.moduleColor.getPacked()  : 0xEE0A0A0A; }
    private int enabledColor() { return ClickGuiModule.INSTANCE != null ? ClickGuiModule.INSTANCE.enabledColor.getPacked() : 0xEE0F5050; }
    private int borderColor()  { return ClickGuiModule.INSTANCE != null ? ClickGuiModule.INSTANCE.borderColor.getPacked()  : 0xFF0A4040; }
    private int overlayColor() { return ClickGuiModule.INSTANCE != null ? ClickGuiModule.INSTANCE.overlayColor.getPacked() : 0x55000000; }

    private static class Panel {
        int x, y;
        int scrollOffset = 0;
        boolean dragging = false;
        int dragOffX, dragOffY;
        Category category;

        Panel(Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }
    }

    private Panel[] panels;

    private Module settingsModule = null;
    private int    settingsX, settingsY;
    private boolean settingsDragging = false;
    private int    settingsDragOffX, settingsDragOffY;

    private ColorSetting dragSetting    = null;
    private int          dragComponent  = -1;
    private int          dragSliderX, dragSliderW;

    public ClickGui() {
        super(Text.literal("ClickGUI"));
    }

    @Override
    protected void init() {
        Category[] cats = Category.values();
        panels = new Panel[cats.length];
        int x = START_X_PAD;
        for (int i = 0; i < cats.length; i++) {
            panels[i] = new Panel(cats[i], x, START_Y);
            x += PANEL_WIDTH + 3;
        }
        settingsX = 200;
        settingsY = 40;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, overlayColor());
        for (Panel p : panels) renderPanel(ctx, mouseX, mouseY, p);
        if (settingsModule != null) renderSettingsPanel(ctx, mouseX, mouseY);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderPanel(DrawContext ctx, int mx, int my, Panel p) {
        List<Module> mods = ModuleManager.getModulesForCategory(p.category);
        int visible = Math.min(mods.size(), MAX_VISIBLE);
        int bodyH   = visible * ROW_HEIGHT;
        int totalH  = HEADER_HEIGHT + bodyH;
        boolean scrollable = mods.size() > MAX_VISIBLE;
        int scrollW = scrollable ? 3 : 0;

        ctx.fill(p.x - 1, p.y - 1, p.x + PANEL_WIDTH + 1, p.y + totalH + 1, borderColor());
        ctx.fill(p.x, p.y, p.x + PANEL_WIDTH, p.y + HEADER_HEIGHT, headerColor());
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(p.category.getName()),
                p.x + PANEL_WIDTH / 2, p.y + (HEADER_HEIGHT - 8) / 2, 0xFFFFFFFF);

        int rowY = p.y + HEADER_HEIGHT;
        for (int i = p.scrollOffset; i < p.scrollOffset + visible && i < mods.size(); i++) {
            Module mod  = mods.get(i);
            boolean hov = mx >= p.x && mx < p.x + PANEL_WIDTH - scrollW && my >= rowY && my < rowY + ROW_HEIGHT;
            boolean on  = mod.isEnabled();
            int bg = on ? enabledColor() : (hov ? blendHover(moduleColor()) : moduleColor());
            ctx.fill(p.x, rowY, p.x + PANEL_WIDTH - scrollW, rowY + ROW_HEIGHT, bg);
            ctx.fill(p.x, rowY + ROW_HEIGHT - 1, p.x + PANEL_WIDTH - scrollW, rowY + ROW_HEIGHT, 0xFF080808);

            boolean hasSets = !mod.getSettings().isEmpty();
            String label = mod.getName() + (hasSets ? " \u25b8" : "");
            ctx.drawTextWithShadow(textRenderer, Text.literal(label),
                    p.x + 3, rowY + (ROW_HEIGHT - 8) / 2, on ? 0xFFFFFFFF : 0xFFAAAAAA);
            rowY += ROW_HEIGHT;
        }

        if (scrollable) {
            ctx.fill(p.x + PANEL_WIDTH - scrollW, p.y + HEADER_HEIGHT,
                     p.x + PANEL_WIDTH, p.y + HEADER_HEIGHT + bodyH, 0xFF080808);
            float ratio = (float) p.scrollOffset / Math.max(1, mods.size() - visible);
            int   barH  = Math.max(4, (int)((float) visible / mods.size() * bodyH));
            int   barY  = p.y + HEADER_HEIGHT + (int)(ratio * (bodyH - barH));
            ctx.fill(p.x + PANEL_WIDTH - scrollW, barY, p.x + PANEL_WIDTH, barY + barH, 0xFF0F6060);
        }
    }

    private void renderSettingsPanel(DrawContext ctx, int mx, int my) {
        List<Setting<?>> sets = settingsModule.getSettings();
        int colorSettings = 0;
        for (Setting<?> s : sets) if (s instanceof ColorSetting) colorSettings++;

        int sliderW  = SETTINGS_W - SETTINGS_PAD * 2 - LABEL_W - 2 - VALUE_W - 2;
        int blockH   = SECTION_LABEL_H + 4 * (SLIDER_H + SLIDER_GAP);
        int bodyH    = colorSettings * (blockH + SECTION_GAP);
        int totalH   = SETTINGS_TITLE_H + bodyH + SETTINGS_PAD;

        ctx.fill(settingsX - 1, settingsY - 1, settingsX + SETTINGS_W + 1, settingsY + totalH + 1, borderColor());
        ctx.fill(settingsX, settingsY, settingsX + SETTINGS_W, settingsY + totalH, 0xF0101010);
        ctx.fill(settingsX, settingsY, settingsX + SETTINGS_W, settingsY + SETTINGS_TITLE_H, headerColor());
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(settingsModule.getName() + " Settings"),
                settingsX + SETTINGS_W / 2, settingsY + (SETTINGS_TITLE_H - 8) / 2, 0xFFFFFFFF);

        int y = settingsY + SETTINGS_TITLE_H + SETTINGS_PAD;
        for (Setting<?> s : sets) {
            if (!(s instanceof ColorSetting cs)) continue;

            ctx.drawTextWithShadow(textRenderer, Text.literal(cs.getName()),
                    settingsX + SETTINGS_PAD, y, 0xFF0FAAAA);
            y += SECTION_LABEL_H;

            int[] components = { cs.getR(), cs.getG(), cs.getB(), cs.getA() };
            String[] labels  = { "R", "G", "B", "A" };
            int[] colors     = { 0xFFCC3333, 0xFF33CC33, 0xFF3366CC, 0xFFAAAAAA };

            for (int c = 0; c < 4; c++) {
                int lx = settingsX + SETTINGS_PAD;
                int sx = lx + LABEL_W + 2;
                int vx = sx + sliderW + 2;
                float frac = components[c] / 255f;

                ctx.drawTextWithShadow(textRenderer, Text.literal(labels[c]), lx, y, colors[c]);
                ctx.fill(sx, y, sx + sliderW, y + SLIDER_H, 0xFF1A1A1A);
                ctx.fill(sx, y, sx + (int)(frac * sliderW), y + SLIDER_H, colors[c]);
                ctx.fill(sx + (int)(frac * sliderW) - 1, y - 1, sx + (int)(frac * sliderW) + 1, y + SLIDER_H + 1, 0xFFFFFFFF);

                String val = String.valueOf(components[c]);
                ctx.drawTextWithShadow(textRenderer, Text.literal(val), vx, y, 0xFFCCCCCC);

                y += SLIDER_H + SLIDER_GAP;
            }
            y += SECTION_GAP;
        }
    }

    private int blendHover(int color) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + 15);
        int g = Math.min(255, ((color >> 8)  & 0xFF) + 15);
        int b = Math.min(255, (color & 0xFF) + 15);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (settingsModule != null) {
            List<Setting<?>> sets = settingsModule.getSettings();
            int sliderW = SETTINGS_W - SETTINGS_PAD * 2 - LABEL_W - 2 - VALUE_W - 2;
            int colorSettings = 0;
            for (Setting<?> s : sets) if (s instanceof ColorSetting) colorSettings++;
            int blockH = SECTION_LABEL_H + 4 * (SLIDER_H + SLIDER_GAP);
            int bodyH  = colorSettings * (blockH + SECTION_GAP);
            int totalH = SETTINGS_TITLE_H + bodyH + SETTINGS_PAD;

            if (button == 0 && mx >= settingsX && mx < settingsX + SETTINGS_W
                    && my >= settingsY && my < settingsY + SETTINGS_TITLE_H) {
                settingsDragging  = true;
                settingsDragOffX  = (int) mx - settingsX;
                settingsDragOffY  = (int) my - settingsY;
                return true;
            }

            if (button == 0 && mx >= settingsX && mx < settingsX + SETTINGS_W
                    && my >= settingsY && my < settingsY + totalH) {
                int y = settingsY + SETTINGS_TITLE_H + SETTINGS_PAD;
                for (Setting<?> s : sets) {
                    if (!(s instanceof ColorSetting cs)) continue;
                    y += SECTION_LABEL_H;
                    int sx = settingsX + SETTINGS_PAD + LABEL_W + 2;
                    for (int c = 0; c < 4; c++) {
                        if (my >= y && my < y + SLIDER_H && mx >= sx && mx < sx + sliderW) {
                            dragSetting   = cs;
                            dragComponent = c;
                            dragSliderX   = sx;
                            dragSliderW   = sliderW;
                            applySlider((int) mx);
                            return true;
                        }
                        y += SLIDER_H + SLIDER_GAP;
                    }
                    y += SECTION_GAP;
                }
            }

            if (button == 1 && (mx < settingsX || mx >= settingsX + SETTINGS_W
                    || my < settingsY || my >= settingsY + totalH)) {
                settingsModule = null;
                return true;
            }
        }

        for (int i = panels.length - 1; i >= 0; i--) {
            Panel p    = panels[i];
            List<Module> mods = ModuleManager.getModulesForCategory(p.category);
            int totalH = HEADER_HEIGHT + Math.min(mods.size(), MAX_VISIBLE) * ROW_HEIGHT;

            if (mx >= p.x && mx < p.x + PANEL_WIDTH && my >= p.y && my < p.y + HEADER_HEIGHT) {
                if (button == 0) {
                    p.dragging   = true;
                    p.dragOffX   = (int) mx - p.x;
                    p.dragOffY   = (int) my - p.y;
                    bringToFront(i);
                    return true;
                }
            }

            int rowY = p.y + HEADER_HEIGHT;
            for (int j = p.scrollOffset; j < p.scrollOffset + Math.min(mods.size(), MAX_VISIBLE) && j < mods.size(); j++) {
                Module mod = mods.get(j);
                if (mx >= p.x && mx < p.x + PANEL_WIDTH && my >= rowY && my < rowY + ROW_HEIGHT) {
                    if (button == 0) {
                        mod.toggle();
                        return true;
                    }
                    if (button == 1 && !mod.getSettings().isEmpty()) {
                        settingsModule = mod;
                        settingsX      = (int) mx + 4;
                        settingsY      = (int) my;
                        return true;
                    }
                }
                rowY += ROW_HEIGHT;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private void applySlider(int mouseX) {
        if (dragSetting == null) return;
        float frac = Math.max(0f, Math.min(1f, (float)(mouseX - dragSliderX) / dragSliderW));
        int val = Math.round(frac * 255);
        switch (dragComponent) {
            case 0 -> dragSetting.setR(val);
            case 1 -> dragSetting.setG(val);
            case 2 -> dragSetting.setB(val);
            case 3 -> dragSetting.setA(val);
        }
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragSetting != null && button == 0) {
            applySlider((int) mx);
            return true;
        }
        if (settingsDragging && button == 0) {
            settingsX = (int) mx - settingsDragOffX;
            settingsY = (int) my - settingsDragOffY;
            return true;
        }
        for (Panel p : panels) {
            if (p.dragging && button == 0) {
                p.x = (int) mx - p.dragOffX;
                p.y = (int) my - p.dragOffY;
                return true;
            }
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            dragSetting      = null;
            dragComponent    = -1;
            settingsDragging = false;
            for (Panel p : panels) p.dragging = false;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hx, double hy) {
        for (Panel p : panels) {
            List<Module> mods = ModuleManager.getModulesForCategory(p.category);
            int totalH = HEADER_HEIGHT + Math.min(mods.size(), MAX_VISIBLE) * ROW_HEIGHT;
            if (mx >= p.x && mx < p.x + PANEL_WIDTH && my >= p.y && my < p.y + totalH) {
                int maxScroll = Math.max(0, mods.size() - MAX_VISIBLE);
                p.scrollOffset = Math.max(0, Math.min(maxScroll, p.scrollOffset - (int) Math.signum(hy)));
                return true;
            }
        }
        return super.mouseScrolled(mx, my, hx, hy);
    }

    private void bringToFront(int index) {
        Panel p = panels[index];
        System.arraycopy(panels, index + 1, panels, index, panels.length - 1 - index);
        panels[panels.length - 1] = p;
    }

    @Override
    public boolean shouldPause() { return false; }
}