package play451.is.larping.gui.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.module.impl.core.ClickGuiModule;
import play451.is.larping.module.setting.BooleanSetting;
import play451.is.larping.module.setting.ColorSetting;
import play451.is.larping.module.setting.SliderSetting;
import play451.is.larping.module.setting.Setting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsPanel {
    private int     x, y;
    private boolean visible    = false;
    private boolean dragging   = false;
    private double  dragOffX, dragOffY;
    private int     dragChannel = -1;
    private ColorSetting dragColorSetting = null;

    private static final int WIDTH     = 160;
    private static final int HEADER_H  = 13;
    private static final int ROW_H     = 14;
    private static final int PAD       = 4;
    private static final int CH_H      = 11;

    private static final String[] CH_LABELS = {"R", "G", "B", "A"};
    private static final int[]    CH_COLS   = {0xFFCC4444, 0xFF44CC44, 0xFF4488CC, 0xFFAAAAAA};

    private final List<ColorSetting>   openColorPickers = new ArrayList<>();

    public SettingsPanel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void open(int px, int py) {
        this.x       = px;
        this.y       = py;
        this.visible = true;
        openColorPickers.clear();
    }

    public void close() {
        visible = false;
        openColorPickers.clear();
    }

    public boolean isVisible() { return visible; }

    public void render(DrawContext ctx, int mouseX, int mouseY) {
        if (!visible || ClickGuiModule.INSTANCE == null) return;

        var   tr     = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(y);
        int   aR     = accent.getRed();
        int   aG     = accent.getGreen();
        int   aB     = accent.getBlue();
        int   accentP = 0xFF000000 | (aR << 16) | (aG << 8) | aB;

        int totalH = getTotalHeight();

        ctx.fill(x, y, x + WIDTH, y + HEADER_H,
                (255 << 24) | (aR << 16) | (aG << 8) | aB);
        ctx.drawCenteredTextWithShadow(tr, "ClickGUI Settings",
                x + WIDTH / 2, y + (HEADER_H - 8) / 2, 0xFFFFFFFF);

        ctx.fill(x, y + HEADER_H, x + WIDTH, y + totalH, 0xEE0D0D0D);

        int rowY = y + HEADER_H + 2;

        for (Setting<?> s : ClickGuiModule.INSTANCE.getSettings()) {
            if (s instanceof BooleanSetting bs) {
                boolean val = bs.getValue();
                boolean hov = isHovering(mouseX, mouseY, x + PAD, rowY, WIDTH - PAD * 2, ROW_H);
                ctx.fill(x + PAD, rowY, x + WIDTH - PAD, rowY + ROW_H - 1,
                        val ? withAlpha(accent, 80) : (hov ? 0xAA1C1C1C : 0xAA111111));
                ctx.drawTextWithShadow(tr, bs.getTag(), x + PAD + 3, rowY + (ROW_H - 8) / 2,
                        val ? 0xFFFFFFFF : 0xFF999999);
                String valStr = val ? "ON" : "OFF";
                ctx.drawTextWithShadow(tr, valStr,
                        x + WIDTH - PAD - 2 - tr.getWidth(valStr),
                        rowY + (ROW_H - 8) / 2,
                        val ? accentP : 0xFF555555);
                ctx.fill(x + PAD, rowY + ROW_H - 1, x + WIDTH - PAD, rowY + ROW_H, 0xFF050505);
                rowY += ROW_H;

            } else if (s instanceof SliderSetting ss) {
                int x1 = x + PAD;
                int x2 = x + WIDTH - PAD;
                int iW = x2 - x1;
                double ratio = (ss.getValue() - ss.getMin()) / (ss.getMax() - ss.getMin());
                ctx.fill(x1, rowY, x2, rowY + ROW_H - 1, 0xAA111111);
                ctx.fill(x1, rowY + ROW_H - 3, x1 + (int)(ratio * iW), rowY + ROW_H - 1,
                        withAlpha(accent, 200));
                ctx.drawTextWithShadow(tr, ss.getTag(), x1 + 3, rowY + (ROW_H - 8) / 2, 0xFFCCCCCC);
                String val = ss.getDisplayValue();
                ctx.drawTextWithShadow(tr, val, x2 - 2 - tr.getWidth(val), rowY + (ROW_H - 8) / 2, 0xFFFFFFFF);
                ctx.fill(x1, rowY + ROW_H - 1, x2, rowY + ROW_H, 0xFF050505);
                rowY += ROW_H;

            } else if (s instanceof ColorSetting cs) {
                boolean pickerOpen = openColorPickers.contains(cs);
                int x1 = x + PAD;
                int x2 = x + WIDTH - PAD;
                ctx.fill(x1, rowY, x2, rowY + ROW_H - 1, 0xAA111111);
                ctx.drawTextWithShadow(tr, cs.getTag(), x1 + 3, rowY + (ROW_H - 8) / 2, 0xFFCCCCCC);
                int px2 = x2 - 2;
                int px1 = px2 - 14;
                ctx.fill(px1, rowY + 2, px2, rowY + ROW_H - 3, 0xFF222222);
                ctx.fill(px1, rowY + 2, px2, rowY + ROW_H - 3,
                        (cs.getA() << 24) | (cs.getR() << 16) | (cs.getG() << 8) | cs.getB());
                String arrow = pickerOpen ? "\u25be" : "\u25b8";
                ctx.drawTextWithShadow(tr, arrow, px1 - 4 - tr.getWidth(arrow),
                        rowY + (ROW_H - 8) / 2, 0xFF888888);
                ctx.fill(x1, rowY + ROW_H - 1, x2, rowY + ROW_H, 0xFF050505);
                rowY += ROW_H;

                if (pickerOpen) {
                    int[] channels = {cs.getR(), cs.getG(), cs.getB(), cs.getA()};
                    int sliderX = x1 + 12;
                    int sliderW = x2 - sliderX - 22;
                    for (int i = 0; i < 4; i++) {
                        ctx.fill(x1, rowY, x2, rowY + CH_H - 1, 0xAA161616);
                        ctx.fill(x1, rowY, x1 + 1, rowY + CH_H - 1, CH_COLS[i]);
                        ctx.drawTextWithShadow(tr, CH_LABELS[i], x1 + 3, rowY + (CH_H - 8) / 2, CH_COLS[i]);
                        ctx.fill(sliderX, rowY + 2, sliderX + sliderW, rowY + CH_H - 2, 0xFF1A1A1A);
                        int fillW = (int)((channels[i] / 255.0) * sliderW);
                        ctx.fill(sliderX, rowY + 2, sliderX + fillW, rowY + CH_H - 2, CH_COLS[i]);
                        ctx.fill(sliderX + fillW - 1, rowY + 1, sliderX + fillW + 1, rowY + CH_H - 1, 0xFFFFFFFF);
                        String vs = String.valueOf(channels[i]);
                        ctx.drawTextWithShadow(tr, vs, x2 - 2 - tr.getWidth(vs), rowY + (CH_H - 8) / 2, 0xFFCCCCCC);
                        ctx.fill(x1, rowY + CH_H - 1, x2, rowY + CH_H, 0xFF050505);
                        rowY += CH_H;
                    }
                }
            }
        }

        ctx.fill(x, y + totalH, x + WIDTH, y + totalH + 1, accentP);
    }

    private int getTotalHeight() {
        if (ClickGuiModule.INSTANCE == null) return HEADER_H;
        int h = HEADER_H + 2;
        for (Setting<?> s : ClickGuiModule.INSTANCE.getSettings()) {
            h += ROW_H;
            if (s instanceof ColorSetting cs && openColorPickers.contains(cs)) {
                h += CH_H * 4;
            }
        }
        return h;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || ClickGuiModule.INSTANCE == null) return false;

        if (button == 0 && isHovering(mouseX, mouseY, x, y, WIDTH, HEADER_H)) {
            dragging = true;
            dragOffX = mouseX - x;
            dragOffY = mouseY - y;
            return true;
        }

        if (button == 1 && !isHovering(mouseX, mouseY, x, y, WIDTH, getTotalHeight())) {
            close();
            return true;
        }

        int rowY = y + HEADER_H + 2;
        for (Setting<?> s : ClickGuiModule.INSTANCE.getSettings()) {
            if (s instanceof BooleanSetting bs) {
                if (button == 0 && isHovering(mouseX, mouseY, x + PAD, rowY, WIDTH - PAD * 2, ROW_H)) {
                    bs.toggle();
                    return true;
                }
                rowY += ROW_H;

            } else if (s instanceof SliderSetting ss) {
                if (button == 0 && isHovering(mouseX, mouseY, x + PAD, rowY, WIDTH - PAD * 2, ROW_H)) {
                    double ratio = Math.max(0, Math.min(1, (mouseX - (x + PAD)) / (WIDTH - PAD * 2)));
                    ss.setValue(ss.getMin() + ratio * (ss.getMax() - ss.getMin()));
                    dragColorSetting = null;
                    dragChannel = 99;
                    return true;
                }
                rowY += ROW_H;

            } else if (s instanceof ColorSetting cs) {
                if (button == 0 && isHovering(mouseX, mouseY, x + PAD, rowY, WIDTH - PAD * 2, ROW_H)) {
                    if (openColorPickers.contains(cs)) openColorPickers.remove(cs);
                    else openColorPickers.add(cs);
                    return true;
                }
                rowY += ROW_H;

                if (openColorPickers.contains(cs)) {
                    int sliderX = x + PAD + 12;
                    int sliderW = (x + WIDTH - PAD) - sliderX - 22;
                    for (int i = 0; i < 4; i++) {
                        if (button == 0 && isHovering(mouseX, mouseY, sliderX, rowY + 2, sliderW, CH_H - 4)) {
                            dragColorSetting = cs;
                            dragChannel      = i;
                            applyColorChannel(mouseX, sliderX, sliderW);
                            return true;
                        }
                        rowY += CH_H;
                    }
                }
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        if (!visible) return false;
        if (dragging && button == 0) {
            x = (int)(mouseX - dragOffX);
            y = (int)(mouseY - dragOffY);
            return true;
        }
        if (dragChannel >= 0 && dragColorSetting != null && button == 0) {
            int sliderX = x + PAD + 12;
            int sliderW = (x + WIDTH - PAD) - sliderX - 22;
            if (dragChannel == 99) {
                return true;
            }
            applyColorChannel(mouseX, sliderX, sliderW);
            return true;
        }
        if (dragChannel == 99 && button == 0) {
            for (Setting<?> s : ClickGuiModule.INSTANCE.getSettings()) {
                if (s instanceof SliderSetting ss) {
                    double ratio = Math.max(0, Math.min(1, (mouseX - (x + PAD)) / (WIDTH - PAD * 2)));
                    ss.setValue(ss.getMin() + ratio * (ss.getMax() - ss.getMin()));
                }
            }
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging        = false;
            dragChannel     = -1;
            dragColorSetting = null;
        }
        return false;
    }

    private void applyColorChannel(double mx, int sliderX, int sliderW) {
        int val = (int) Math.max(0, Math.min(255, ((mx - sliderX) / sliderW) * 255));
        switch (dragChannel) {
            case 0 -> dragColorSetting.setR(val);
            case 1 -> dragColorSetting.setG(val);
            case 2 -> dragColorSetting.setB(val);
            case 3 -> dragColorSetting.setA(val);
        }
    }

    private boolean isHovering(double mx, double my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx < rx + rw && my >= ry && my < ry + rh;
    }

    private int withAlpha(Color c, int a) {
        return (a << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }
}