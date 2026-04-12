package play451.is.larping.gui.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.gui.api.Button;
import play451.is.larping.gui.api.Frame;
import play451.is.larping.module.setting.SliderSetting;

import java.awt.*;

public class SliderButton extends Button {
    private final SliderSetting setting;
    private boolean sliding = false;

    public SliderButton(SliderSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var   tr  = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(getY());
        int   ap  = (255 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue();
        int   h   = getHeight();
        int   x1  = getX() + getPadding() + 1;
        int   x2  = getX() + getWidth() - getPadding() - 1;
        int   iW  = x2 - x1;

        context.fill(x1, getY(), x2, getY() + h - 1, 0xAA0C0C0C);
        context.fill(x1, getY(), x1 + 1, getY() + h - 1, ap);

        double ratio = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        context.fill(x1, getY() + h - 3, x1 + (int)(ratio * iW), getY() + h - 1,
                withAlpha(accent, 200));

        context.drawTextWithShadow(tr, setting.getTag(),
                getX() + getTextPadding() + 2, getY() + (h - 8) / 2, 0xFFCCCCCC);

        String val = setting.getDisplayValue();
        context.drawTextWithShadow(tr, val,
                x2 - 2 - tr.getWidth(val), getY() + (h - 8) / 2, 0xFFFFFFFF);

        context.fill(x1, getY() + h - 1, x2, getY() + h, 0xFF050505);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(mouseX, mouseY) && button == 0) {
            sliding = true;
            applySlide(mouseX);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) sliding = false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        if (sliding && button == 0) applySlide(mouseX);
    }

    private void applySlide(double mx) {
        int    x1    = getX() + getPadding() + 1;
        int    iW    = getWidth() - getPadding() * 2 - 2;
        double ratio = Math.max(0, Math.min(1, (mx - x1) / iW));
        setting.setValue(setting.getMin() + ratio * (setting.getMax() - setting.getMin()));
    }

    private int withAlpha(Color c, int a) {
        return (a << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }
}