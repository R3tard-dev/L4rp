package play451.is.larping.gui.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import play451.is.larping.gui.ClickGui;
import play451.is.larping.gui.api.Button;
import play451.is.larping.gui.api.GuiFrame;
import play451.is.larping.module.setting.BooleanSetting;

import java.awt.*;

public class BooleanButton extends Button {
    private final BooleanSetting setting;

    public BooleanButton(BooleanSetting setting, GuiFrame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var   tr     = MinecraftClient.getInstance().textRenderer;
        Color accent = ClickGui.getHeaderColor(getY());
        int   ap     = (255 << 24) | (accent.getRed() << 16) | (accent.getGreen() << 8) | accent.getBlue();
        int   h      = getHeight();
        int   x1     = getX() + getPadding() + 1;
        int   x2     = getX() + getWidth() - getPadding() - 1;

        context.fill(x1, getY(), x2, getY() + h - 1,
                setting.getValue() ? withAlpha(accent, 70) : 0xAA0C0C0C);
        context.fill(x1, getY(), x1 + 1, getY() + h - 1, ap);

        context.drawTextWithShadow(tr, setting.getTag(),
                getX() + getTextPadding() + 2, getY() + (h - 8) / 2,
                setting.getValue() ? 0xFFFFFFFF : 0xFF999999);

        String val    = setting.getValue() ? "ON" : "OFF";
        int    valCol = setting.getValue() ? ap : 0xFF555555;
        context.drawTextWithShadow(tr, val,
                x2 - 2 - tr.getWidth(val), getY() + (h - 8) / 2, valCol);

        context.fill(x1, getY() + h - 1, x2, getY() + h, 0xFF050505);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(mouseX, mouseY) && button == 0) setting.toggle();
    }

    private int withAlpha(Color c, int a) {
        return (a << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }
}