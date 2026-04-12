package play451.is.larping.gui.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.gui.api.Button;
import play451.is.larping.gui.api.Frame;
import play451.is.larping.module.setting.BooleanSetting;

import java.awt.*;

public class BooleanButton extends Button {
    private final BooleanSetting setting;

    public BooleanButton(BooleanSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var tr = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(getY());

        context.fill(getX() + getPadding() + 1, getY(), getX() + getWidth() - getPadding() - 1, getY() + getHeight() - 1,
                setting.getValue() ? withAlpha(accent, 80) : 0xAA0D0D0D);

        context.fill(getX() + getPadding() + 1, getY(), getX() + getPadding() + 2, getY() + getHeight() - 1,
                withAlpha(accent, 200));

        context.drawTextWithShadow(tr, setting.getTag(),
                getX() + getTextPadding() + 2, getY() + (getHeight() - 8) / 2,
                setting.getValue() ? 0xFFFFFFFF : 0xFFAAAAAA);

        String valText = setting.getValue() ? "ON" : "OFF";
        int valColor   = setting.getValue() ? accent.getRGB() | 0xFF000000 : 0xFF666666;
        context.drawTextWithShadow(tr, valText,
                getX() + getWidth() - getPadding() - 2 - tr.getWidth(valText),
                getY() + (getHeight() - 8) / 2, valColor);

        context.fill(getX() + getPadding() + 1, getY() + getHeight() - 1,
                getX() + getWidth() - getPadding() - 1, getY() + getHeight(), 0xFF060606);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(mouseX, mouseY) && button == 0) {
            setting.toggle();
        }
    }

    private int withAlpha(Color c, int alpha) {
        return (alpha << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }
}