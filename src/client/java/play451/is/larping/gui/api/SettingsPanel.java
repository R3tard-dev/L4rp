package play451.is.larping.gui.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.module.Module;
import play451.is.larping.module.setting.*;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class SettingsPanel {
    private int x, y;
    private boolean visible  = false;
    private boolean dragging = false;
    private double  dragOffX, dragOffY;

    private Module currentModule = null;

    private static final int WIDTH    = 160;
    private static final int HEADER_H = 13;
    private static final int ROW_H    = 14;
    private static final int PAD      = 4;
    private static final int CH_H     = 12;

    private static final String[] CH_LABELS = {"R", "G", "B", "A"};
    private static final int[]    CH_COLS   = {0xFFCC4444, 0xFF44CC44, 0xFF4488CC, 0xFFAAAAAA};

    private final Set<ColorSetting> openPickers = new HashSet<>();

    private ColorSetting  activeDragColor   = null;
    private int           activeDragChannel = -1;
    private SliderSetting activeDragSlider  = null;

    public SettingsPanel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void open(Module module) {
        if (currentModule == module && visible) {
            visible = false;
            currentModule = null;
        } else {
            currentModule = module;
            openPickers.clear();
            visible = true;
        }
    }

    public void toggle() { visible = !visible; }
    public boolean isVisible() { return visible; }

    public void render(DrawContext ctx, int mouseX, int mouseY) {
        if (!visible || currentModule == null) return;

        var   tr     = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(0);
        int   aR     = accent.getRed();
        int   aG     = accent.getGreen();
        int   aB     = accent.getBlue();
        int   ap     = 0xFF000000 | (aR << 16) | (aG << 8) | aB;

        int totalH = computeTotalHeight();

        ctx.fill(x, y, x + WIDTH, y + HEADER_H, ap);
        ctx.drawCenteredTextWithShadow(tr, currentModule.getName(),
                x + WIDTH / 2, y + (HEADER_H - 8) / 2, 0xFFFFFFFF);

        ctx.fill(x, y + HEADER_H, x + WIDTH, y + totalH, 0xF0101010);

        int rowY = y + HEADER_H + 2;
        for (Setting<?> s : currentModule.getSettings()) {
            if (!s.getVisibility().isVisible()) continue;

            if (s instanceof BooleanSetting bs) {
                rowY = renderBoolean(ctx, tr, accent, ap, bs, rowY, mouseX, mouseY);
            } else if (s instanceof SliderSetting ss) {
                rowY = renderSlider(ctx, tr, accent, ss, rowY);
            } else if (s instanceof ColorSetting cs) {
                rowY = renderColor(ctx, tr, ap, cs, rowY);
            }
        }
    }

    private int renderBoolean(DrawContext ctx, net.minecraft.client.font.TextRenderer tr,
                              Color accent, int ap, BooleanSetting bs, int rowY, int mx, int my) {
        int x1 = x + PAD;
        int x2 = x + WIDTH - PAD;
        boolean val = bs.getValue();
        boolean hov = mx >= x1 && mx < x2 && my >= rowY && my < rowY + ROW_H;
        ctx.fill(x1, rowY, x2, rowY + ROW_H - 1,
                val ? withAlpha(accent, 80) : (hov ? 0xAA1E1E1E : 0xAA111111));
        ctx.drawTextWithShadow(tr, bs.getTag(), x1 + 3, rowY + (ROW_H - 8) / 2,
                val ? 0xFFFFFFFF : 0xFF999999);
        String vs = val ? "ON" : "OFF";
        ctx.drawTextWithShadow(tr, vs, x2 - 2 - tr.getWidth(vs),
                rowY + (ROW_H - 8) / 2, val ? ap : 0xFF555555);
        ctx.fill(x1, rowY + ROW_H - 1, x2, rowY + ROW_H, 0xFF050505);
        return rowY + ROW_H;
    }

    private int renderSlider(DrawContext ctx, net.minecraft.client.font.TextRenderer tr,
                             Color accent, SliderSetting ss, int rowY) {
        int x1 = x + PAD;
        int x2 = x + WIDTH - PAD;
        int iW = x2 - x1;
        double ratio = (ss.getValue() - ss.getMin()) / (ss.getMax() - ss.getMin());
        ctx.fill(x1, rowY, x2, rowY + ROW_H - 1, 0xAA111111);
        ctx.fill(x1, rowY + ROW_H - 3, x1 + (int)(ratio * iW), rowY + ROW_H - 1,
                withAlpha(accent, 210));
        ctx.drawTextWithShadow(tr, ss.getTag(), x1 + 3, rowY + (ROW_H - 8) / 2, 0xFFCCCCCC);
        String val = ss.getDisplayValue();
        ctx.drawTextWithShadow(tr, val, x2 - 2 - tr.getWidth(val),
                rowY + (ROW_H - 8) / 2, 0xFFFFFFFF);
        ctx.fill(x1, rowY + ROW_H - 1, x2, rowY + ROW_H, 0xFF050505);
        return rowY + ROW_H;
    }

    private int renderColor(DrawContext ctx, net.minecraft.client.font.TextRenderer tr,
                            int ap, ColorSetting cs, int rowY) {
        int x1 = x + PAD;
        int x2 = x + WIDTH - PAD;
        ctx.fill(x1, rowY, x2, rowY + ROW_H - 1, 0xAA111111);
        ctx.drawTextWithShadow(tr, cs.getTag(), x1 + 3, rowY + (ROW_H - 8) / 2, 0xFFCCCCCC);
        int px2 = x2 - 2;
        int px1 = px2 - 14;
        ctx.fill(px1, rowY + 2, px2, rowY + ROW_H - 3, 0xFF222222);
        ctx.fill(px1, rowY + 2, px2, rowY + ROW_H - 3,
                (cs.getA() << 24) | (cs.getR() << 16) | (cs.getG() << 8) | cs.getB());
        boolean open = openPickers.contains(cs);
        ctx.drawTextWithShadow(tr, open ? "\u25be" : "\u25b8",
                px1 - 5 - tr.getWidth(open ? "\u25be" : "\u25b8"),
                rowY + (ROW_H - 8) / 2, 0xFF888888);
        ctx.fill(x1, rowY + ROW_H - 1, x2, rowY + ROW_H, 0xFF050505);
        rowY += ROW_H;

        if (open) {
            int[] chs     = {cs.getR(), cs.getG(), cs.getB(), cs.getA()};
            int   sliderX = x1 + 13;
            int   sliderW = x2 - sliderX - 22;
            for (int i = 0; i < 4; i++) {
                ctx.fill(x1, rowY, x2, rowY + CH_H - 1, 0xAA161616);
                ctx.fill(x1, rowY, x1 + 1, rowY + CH_H - 1, CH_COLS[i]);
                ctx.drawTextWithShadow(tr, CH_LABELS[i], x1 + 3, rowY + (CH_H - 8) / 2, CH_COLS[i]);
                ctx.fill(sliderX, rowY + 2, sliderX + sliderW, rowY + CH_H - 2, 0xFF1A1A1A);
                int fw = (int)((chs[i] / 255.0) * sliderW);
                ctx.fill(sliderX, rowY + 2, sliderX + fw, rowY + CH_H - 2, CH_COLS[i]);
                ctx.fill(sliderX + fw - 1, rowY + 1, sliderX + fw + 1, rowY + CH_H - 1, 0xFFFFFFFF);
                String vs = String.valueOf(chs[i]);
                ctx.drawTextWithShadow(tr, vs, x2 - 2 - tr.getWidth(vs),
                        rowY + (CH_H - 8) / 2, 0xFFCCCCCC);
                ctx.fill(x1, rowY + CH_H - 1, x2, rowY + CH_H, 0xFF050505);
                rowY += CH_H;
            }
        }
        return rowY;
    }

    private int computeTotalHeight() {
        if (currentModule == null) return HEADER_H;
        int h = HEADER_H + 2;
        for (Setting<?> s : currentModule.getSettings()) {
            if (!s.getVisibility().isVisible()) continue;
            h += ROW_H;
            if (s instanceof ColorSetting cs && openPickers.contains(cs)) h += CH_H * 4;
        }
        return h;
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!visible || currentModule == null) return false;

        int totalH = computeTotalHeight();
        boolean inside = mx >= x && mx < x + WIDTH && my >= y && my < y + totalH;

        if (!inside) {
            if (button == 1) { visible = false; }
            return false;
        }

        if (button == 0 && my >= y && my < y + HEADER_H) {
            dragging = true;
            dragOffX = mx - x;
            dragOffY = my - y;
            return true;
        }

        int rowY = y + HEADER_H + 2;
        for (Setting<?> s : currentModule.getSettings()) {
            if (!s.getVisibility().isVisible()) continue;

            if (s instanceof BooleanSetting bs) {
                if (button == 0 && mx >= x + PAD && mx < x + WIDTH - PAD
                 && my >= rowY && my < rowY + ROW_H) {
                    bs.toggle();
                    return true;
                }
                rowY += ROW_H;

            } else if (s instanceof SliderSetting ss) {
                if (button == 0 && mx >= x + PAD && mx < x + WIDTH - PAD
                 && my >= rowY && my < rowY + ROW_H) {
                    activeDragSlider = ss;
                    applySlider(ss, mx);
                    return true;
                }
                rowY += ROW_H;

            } else if (s instanceof ColorSetting cs) {
                if (button == 0 && mx >= x + PAD && mx < x + WIDTH - PAD
                 && my >= rowY && my < rowY + ROW_H) {
                    if (openPickers.contains(cs)) openPickers.remove(cs);
                    else openPickers.add(cs);
                    return true;
                }
                rowY += ROW_H;

                if (openPickers.contains(cs)) {
                    int sliderX = x + PAD + 13;
                    int sliderW = (x + WIDTH - PAD) - sliderX - 22;
                    for (int i = 0; i < 4; i++) {
                        if (button == 0 && mx >= sliderX && mx < sliderX + sliderW
                         && my >= rowY + 2 && my < rowY + CH_H - 2) {
                            activeDragColor   = cs;
                            activeDragChannel = i;
                            applyColorChannel(mx, sliderX, sliderW);
                            return true;
                        }
                        rowY += CH_H;
                    }
                }
            }
        }
        return inside;
    }

    public boolean mouseDragged(double mx, double my, int button, double dX, double dY) {
        if (!visible) return false;
        if (button != 0) return false;

        if (dragging) {
            x = (int)(mx - dragOffX);
            y = (int)(my - dragOffY);
            return true;
        }

        if (activeDragSlider != null) {
            applySlider(activeDragSlider, mx);
            return true;
        }

        if (activeDragColor != null && activeDragChannel >= 0) {
            int sliderX = x + PAD + 13;
            int sliderW = (x + WIDTH - PAD) - sliderX - 22;
            applyColorChannel(mx, sliderX, sliderW);
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0) {
            dragging          = false;
            activeDragSlider  = null;
            activeDragColor   = null;
            activeDragChannel = -1;
        }
        return false;
    }

    public boolean mouseScrolled(double mx, double my, double vAmt) {
        if (!visible || currentModule == null) return false;
        int totalH = computeTotalHeight();
        return mx >= x && mx < x + WIDTH && my >= y && my < y + totalH;
    }

    private void applySlider(SliderSetting ss, double mx) {
        int    x1    = x + PAD;
        int    iW    = WIDTH - PAD * 2;
        double ratio = Math.max(0, Math.min(1, (mx - x1) / iW));
        ss.setValue(ss.getMin() + ratio * (ss.getMax() - ss.getMin()));
    }

    private void applyColorChannel(double mx, int sliderX, int sliderW) {
        int val = (int) Math.max(0, Math.min(255, ((mx - sliderX) / sliderW) * 255));
        switch (activeDragChannel) {
            case 0 -> activeDragColor.setR(val);
            case 1 -> activeDragColor.setG(val);
            case 2 -> activeDragColor.setB(val);
            case 3 -> activeDragColor.setA(val);
        }
    }

    private int withAlpha(Color c, int a) {
        return (a << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }
}