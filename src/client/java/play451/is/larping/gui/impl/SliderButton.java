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
        var tr = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(getY());

        int innerX1 = getX() + getPadding() + 1;
        int innerX2 = getX() + getWidth() - getPadding() - 1;
        int innerW  = innerX2 - innerX1;

        context.fill(innerX1, getY(), innerX2, getY() + getHeight() - 1, 0xAA0D0D0D);
        context.fill(innerX1, getY(), innerX1 + 1, getY() + getHeight() - 1,
                (255 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue());

        double ratio  = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        int    fillW  = (int)(ratio * innerW);
        context.fill(innerX1, getY() + getHeight() - 2, innerX1 + fillW, getY() + getHeight() - 1,
                (200 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue());

        context.drawTextWithShadow(tr, setting.getTag(),
                getX() + getTextPadding() + 2, getY() + (getHeight() - 8) / 2, 0xFFCCCCCC);

        String val = setting.getDisplayValue();
        context.drawTextWithShadow(tr, val,
                innerX2 - 2 - tr.getWidth(val), getY() + (getHeight() - 8) / 2, 0xFFFFFFFF);

        context.fill(innerX1, getY() + getHeight() - 1, innerX2, getY() + getHeight(), 0xFF060606);
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

    private void applySlide(double mouseX) {
        int innerX1 = getX() + getPadding() + 1;
        int innerW  = getWidth() - getPadding() * 2 - 2;
        double ratio = Math.max(0, Math.min(1, (mouseX - innerX1) / innerW));
        setting.setValue(setting.getMin() + ratio * (setting.getMax() - setting.getMin()));
    }
}